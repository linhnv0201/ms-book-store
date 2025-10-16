package payment_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import common_dto.dto.OrderPayRequest;
import common_dto.dto.PaymentResponseStatusEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import payment_service.dto.response.ApiResponse;
import payment_service.entity.CustomerPayment;
import payment_service.exception.AppException;
import payment_service.exception.ErrorCode;
import payment_service.dto.response.PaymentDTO;
import payment_service.config.VNPAYConfig;
import payment_service.dto.response.VNPayQueryResponse;
import payment_service.config.VNPayUtil;
import payment_service.repo.CustomerPaymentRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {
    VNPAYConfig vnPayConfig;
    CustomerPaymentRepository customerPaymentRepository;
    PaymentKafkaProducer paymentKafkaProducer;

    public OrderPayRequest fetchOrderInfo(Long orderId) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8084/orders/" + orderId + "/pay";

        ResponseEntity<ApiResponse<OrderPayRequest>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null, // không có body
                new ParameterizedTypeReference<ApiResponse<OrderPayRequest>>() {}
        );

        ApiResponse<OrderPayRequest> apiResponse = response.getBody();
        if (apiResponse == null || apiResponse.getResult() == null) {
            throw new RuntimeException(
                    "Cannot fetch order info or order not ready for payment (orderId=" + orderId + ")"
            );
        }

        return apiResponse.getResult();
    }

    public PaymentDTO.VNPayResponse createVnPayPayment(HttpServletRequest request) {
        Long orderId = Long.parseLong(request.getParameter("orderId"));

        OrderPayRequest orderPayRequest = fetchOrderInfo(orderId);

        BigDecimal total = orderPayRequest.getTotalAmount();

        long amount = total.multiply(new BigDecimal(100)).longValue();

        String bankCode = request.getParameter("bankCode");
        Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig();

        String vnpCreateDate = vnpParamsMap.get("vnp_CreateDate");
        CustomerPayment customerPayment = new CustomerPayment();
        customerPayment.setOrderId(orderId);
        customerPayment.setVnpCreateDate(vnpCreateDate);
        customerPayment.setAmount(total);
        customerPayment.setStatus(CustomerPayment.Status.PENDING);
        customerPayment.setPaymentMethod(CustomerPayment.PaymentMethod.BANK_TRANSFER);

        vnpParamsMap.put("vnp_Amount", String.valueOf(amount));
//        txnRef là viết tắt của Transaction Reference — tức là mã tham chiếu giao dịch.
        String txnRef = VNPayUtil.generateTxnRef(orderId);
        customerPayment.setVnpTxnRef(txnRef);
        customerPaymentRepository.save(customerPayment);
        vnpParamsMap.put("vnp_TxnRef", txnRef);
        vnpParamsMap.put("vnp_OrderInfo", "Thanh toan don hang:" + customerPayment.getOrderId());
        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParamsMap.put("vnp_BankCode", bankCode);
        }
        vnpParamsMap.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));
        //build query url
        String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
        String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;
        return PaymentDTO.VNPayResponse.builder()
                .code("ok")
                .message("success")
                .paymentUrl(paymentUrl).build();
    }

    public ApiResponse<PaymentDTO.VNPayResponse> handleVnPayCallback(HttpServletRequest request) {
        ApiResponse<PaymentDTO.VNPayResponse> apiResponse = new ApiResponse<>();

        String vnpResponseCode = request.getParameter("vnp_ResponseCode");
        String txnRef = request.getParameter("vnp_TxnRef");

        CustomerPayment customerPayment = customerPaymentRepository.findByVnpTxnRef(txnRef);
        if ("00".equals(vnpResponseCode) && txnRef != null) {

            // ✅ Chặn duplicate
            if (customerPayment.getStatus() == CustomerPayment.Status.SUCCESS){
                apiResponse.setResult(
                        new PaymentDTO.VNPayResponse("00", "Order already paid", null));
                return apiResponse;
            }

            // Update order
            customerPayment.setStatus(CustomerPayment.Status.SUCCESS);
            customerPayment.setPaidAt(LocalDateTime.now());

            apiResponse.setResult(new PaymentDTO.VNPayResponse("00", "Success", null));
        } else {
            customerPayment.setStatus(CustomerPayment.Status.FAILED);
            apiResponse.setMessage("Failed");
            apiResponse.setResult(null);
        }
        customerPaymentRepository.save(customerPayment);

        PaymentResponseStatusEvent event = new PaymentResponseStatusEvent();
        event.setOrderId(customerPayment.getOrderId());
        event.setStatus(customerPayment.getStatus().toString());
        paymentKafkaProducer.sendPaymentCompletedEvent(event);
        return apiResponse;
    }

    public VNPayQueryResponse queryOrderStatus(HttpServletRequest request) {
        // 1. Lấy orderId từ JSON body
        Long orderId;
        try {
            String jsonBody = request.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);
            Map<String, Object> bodyMap = new ObjectMapper().readValue(jsonBody, Map.class);
            orderId = Long.valueOf(bodyMap.get("orderId").toString());
        } catch (IOException e) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // 2. Tìm order
        List<CustomerPayment> customerPayments = customerPaymentRepository.findByOrderId(orderId);
        CustomerPayment latestcustomerPayment = customerPayments.stream()
                .max(Comparator.comparing(CustomerPayment::getId))
                .orElse(null); // trả về null nếu danh sách rỗng

        String vnpTxnRef = latestcustomerPayment.getVnpTxnRef();
        String vnpTransactionDate = latestcustomerPayment.getVnpCreateDate();

        // 3. Sinh tham số
        String requestId = System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "");
        String createDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String ipAddr = VNPayUtil.getIpAddress(request);

        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_RequestId", requestId);
        params.put("vnp_Version", vnPayConfig.getVnp_Version());
        params.put("vnp_Command", "querydr");
        params.put("vnp_TmnCode", vnPayConfig.getVnp_TmnCode());
        params.put("vnp_TxnRef", vnpTxnRef);
        params.put("vnp_TransactionDate", vnpTransactionDate);
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_IpAddr", ipAddr);
        params.put("vnp_OrderInfo", "Thong tin don hang:" + latestcustomerPayment.getOrderId());
        // 4. Ký hash
        String vnpSecureHash = VNPayUtil.hashAllFields(params, vnPayConfig.getSecretKey());
        params.put("vnp_SecureHash", vnpSecureHash);

        // 5. Gửi request JSON
        RestTemplate restTemplate = new RestTemplate();
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String bodyJson;
        try {
            bodyJson = new ObjectMapper().writeValueAsString(params);
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR);
        }

        HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);

        ResponseEntity<VNPayQueryResponse> response = restTemplate.exchange(
                vnPayConfig.getVnp_ApiUrl(),
                HttpMethod.POST,
                entity,
                VNPayQueryResponse.class
        );

        return response.getBody();
    }

    public void updatePaymentFromQueryDr(VNPayQueryResponse response) {
        if (!"00".equals(response.getVnp_ResponseCode()) || !"00".equals(response.getVnp_TransactionStatus())) {
            // Payment không thành công hoặc query lỗi
            return;
        }

        String txnRef = response.getVnp_TxnRef();
        CustomerPayment customerPayment = customerPaymentRepository.findByVnpTxnRef(txnRef);

        // Chặn duplicate: nếu đã PAID thì bỏ qua
        if (customerPayment.getStatus() == CustomerPayment.Status.SUCCESS) {
            return;
        }

        // Update trạng thái order
        customerPayment.setStatus(CustomerPayment.Status.SUCCESS);
        customerPaymentRepository.save(customerPayment);

        PaymentResponseStatusEvent event = new PaymentResponseStatusEvent();
        event.setOrderId(customerPayment.getOrderId());
        event.setStatus(customerPayment.getStatus().toString());
        paymentKafkaProducer.sendPaymentCompletedEvent(event);
    }
}