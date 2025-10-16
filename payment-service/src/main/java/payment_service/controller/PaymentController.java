package payment_service.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import payment_service.dto.response.ApiResponse;
import payment_service.dto.response.PaymentDTO;
import payment_service.service.PaymentService;
import payment_service.dto.response.VNPayQueryResponse;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping("/vn-pay")
    public ApiResponse<PaymentDTO.VNPayResponse> pay(HttpServletRequest request) {
        ApiResponse<PaymentDTO.VNPayResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(paymentService.createVnPayPayment(request));
        return apiResponse;
    }

    @GetMapping("/vn-pay-callback")
    public ApiResponse<PaymentDTO.VNPayResponse> payCallbackHandler(HttpServletRequest request) {
        return paymentService.handleVnPayCallback(request);
    }

    @PostMapping("/query-order")
    public ApiResponse<VNPayQueryResponse> queryOrder(HttpServletRequest request) {
        VNPayQueryResponse response = paymentService.queryOrderStatus(request);
        paymentService.updatePaymentFromQueryDr(response);
        ApiResponse<VNPayQueryResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(response);
        return apiResponse;
    }
}