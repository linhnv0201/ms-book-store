package purchase_order_service.service.impl;

import common_dto.PurchaseOrderCreatedEvent;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import purchase_order_service.config.CurrentUserProvider;
import purchase_order_service.dto.request.PurchaseOrderCreationRequest;
import purchase_order_service.dto.request.PurchaseOrderItemRequest;
import purchase_order_service.dto.response.CurrentUserResponse;
import purchase_order_service.dto.response.PurchaseOrderResponse;
import purchase_order_service.entity.PurchaseOrder;
import purchase_order_service.entity.PurchaseOrderItem;
import purchase_order_service.entity.Supplier;
import purchase_order_service.exception.AppException;
import purchase_order_service.exception.ErrorCode;
import purchase_order_service.mapper.PurchaseOrderMapper;
import purchase_order_service.repo.PurchaseOrderRepository;
import purchase_order_service.repo.SupplierRepository;
import purchase_order_service.service.KafkaProducerService;
import purchase_order_service.service.PurchasrOrderService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PurchaseOrderServiceImpl implements PurchasrOrderService {

    CurrentUserProvider currentUserProvider;
    PurchaseOrderRepository purchaseOrderRepository;
    SupplierRepository supplierRepository;
    PurchaseOrderMapper purchaseOrderMapper;
    KafkaProducerService kafkaProducerService;

    @Override
    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderCreationRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new AppException(ErrorCode.EMPTY_ORDER);
        }

        CurrentUserResponse currentUserResponse = currentUserProvider.getUserDetailsFromRequest();

        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setCreatedAt(LocalDateTime.now());
        purchaseOrder.setCreatedBy(currentUserResponse.getUserId());
        purchaseOrder.setCreatedByEmail(currentUserResponse.getEmail());
        purchaseOrder.setNote(request.getNote());
        purchaseOrder.setCode(generatePurchaseOrderCode());
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_NOT_FOUND));
        purchaseOrder.setSupplier(supplier);

        BigDecimal total = BigDecimal.ZERO;

        for (PurchaseOrderItemRequest itemReq : request.getItems()) {
            PurchaseOrderItem orderItem = new PurchaseOrderItem();
            orderItem.setPurchaseOrder(purchaseOrder);
            orderItem.setProductId(itemReq.getProductId());
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setCost(itemReq.getCost());
            total = total.add(itemReq.getCost().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
            purchaseOrder.getItems().add(orderItem);
        }

        purchaseOrder.setTotalAmount(total);
        purchaseOrderRepository.save(purchaseOrder);

        PurchaseOrderCreatedEvent event = new PurchaseOrderCreatedEvent();
        event.setPurchaseOrderId(purchaseOrder.getId());
        event.setItems(
                purchaseOrder.getItems().stream().map(item -> {
                    PurchaseOrderCreatedEvent.PurchaseOrderItemEvent e = new PurchaseOrderCreatedEvent.PurchaseOrderItemEvent();
                    e.setProductId(item.getProductId());
                    e.setQuantity(item.getQuantity());
                    e.setCost(item.getCost());
                    return e;
                }).toList()
        );
        kafkaProducerService.sendPurchaseOrderCreatedEvent(event);

        PurchaseOrderResponse purchaseOrderResponse = new PurchaseOrderResponse();
        purchaseOrderResponse.setCreatedBy(currentUserResponse.getEmail());
        return purchaseOrderMapper.toResponse(purchaseOrder);
    }

//    @Override
//    @Transactional
//    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderCreationRequest request) {
//        if (request.getItems() == null || request.getItems().isEmpty()) {
//            throw new AppException(ErrorCode.EMPTY_ORDER);
//        }
//
//        CurrentUserResponse currentUserResponse = currentUserProvider.getUserDetailsFromRequest();
//
//        PurchaseOrder purchaseOrder = new PurchaseOrder();
//        purchaseOrder.setCreatedAt(LocalDateTime.now());
//        purchaseOrder.setCreatedBy(currentUserResponse.getUserId());
//        purchaseOrder.setCreatedByEmail(currentUserResponse.getEmail());
//        purchaseOrder.setNote(request.getNote());
//        purchaseOrder.setCode(generatePurchaseOrderCode());
//        Supplier supplier = supplierRepository.findById(request.getSupplierId())
//                .orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_NOT_FOUND));
//        purchaseOrder.setSupplier(supplier);
//
//        BigDecimal total = BigDecimal.ZERO;

//        for (PurchaseOrderItemRequest itemReq : request.getItems()) {
//            Product product = productRepository.findById(itemReq.getProductId())
//                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

//            PurchaseOrderItem orderItem = new PurchaseOrderItem();
//            orderItem.setPurchaseOrder(purchaseOrder);
////            orderItem.setProduct(product);
//            orderItem.setProductId(itemReq.getProductId());
//            orderItem.setQuantity(itemReq.getQuantity());
//            orderItem.setCost(itemReq.getCost());
//
//            total = total.add(itemReq.getCost().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
//            purchaseOrder.getItems().add(orderItem);

            // 🟢 Update giá cost trung bình cho Product
//            BigDecimal oldCost = product.getCost() != null ? product.getCost() : BigDecimal.ZERO;
//            long oldStock = product.getStock() != null ? product.getStock() : 0;

//            long newQuantity = itemReq.getQuantity();
//            BigDecimal newCost = itemReq.getCost();
//
//            long totalStock = oldStock + newQuantity;

//            if (totalStock > 0) {
//                BigDecimal newAvgCost = (oldCost.multiply(BigDecimal.valueOf(oldStock))
//                        .add(newCost.multiply(BigDecimal.valueOf(newQuantity))))
//                        .divide(BigDecimal.valueOf(totalStock), RoundingMode.HALF_UP);
//
//                product.setCost(newAvgCost);
//            } else {
//                // trường hợp chưa có tồn kho thì set bằng giá mới
//                product.setCost(newCost);
//            }

            // cập nhật tồn kho
//            product.setStock(Math.toIntExact(totalStock));

//            productRepository.save(product);
//        }
//
//        purchaseOrder.setTotalAmount(total);
//        purchaseOrderRepository.save(purchaseOrder);
//        PurchaseOrderResponse purchaseOrderResponse = new PurchaseOrderResponse();
//        purchaseOrderResponse.setCreatedBy(currentUserResponse.getEmail());
//        return purchaseOrderMapper.toResponse(purchaseOrder);
//    }

    @Override
    public List<PurchaseOrderResponse> getAll() {
        return purchaseOrderRepository.findAll()
                .stream()
                .map(purchaseOrderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PurchaseOrderResponse getById(Long id) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        return purchaseOrderMapper.toResponse(purchaseOrder);
    }

    private String generatePurchaseOrderCode() {
        // 1. Lấy ngày hiện tại dạng YYYYMMDD
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 2. Sinh 4 chữ số ngẫu nhiên
        int randomPart = (int)(Math.random() * 10000); // 0-9999
        String randomPartStr = String.format("%04d", randomPart);

        // 3. Kết hợp ngày + random
        String code = "PO" + datePart + randomPartStr;

        // 4. Kiểm tra trùng với DB (nếu muốn thật sự an toàn)
        while (purchaseOrderRepository.existsByCode(code)) {
            randomPart = (int)(Math.random() * 10000);
            randomPartStr = String.format("%04d", randomPart);
            code = "ORDER" + datePart + randomPartStr;
        }

        return code;
    }
}
