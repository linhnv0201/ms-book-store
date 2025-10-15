package product_service.service;

import common_dto.dto.OrderCreatedEvent;
import common_dto.dto.OrderStockResponseEvent;
import common_dto.dto.PurchaseOrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import product_service.entity.Product;
import product_service.repo.ProductRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductEventListener {

    private final ProductRepository productRepository;
    private final ProductKafkaProducer productKafkaProducer;

    @KafkaListener(topics = "purchase_order.created", groupId = "product-service-group")
    public void handlePurchaseOrderCreated(PurchaseOrderCreatedEvent event) {
        log.info("Received PurchaseOrderCreatedEvent: orderId={}, items={}",
                event.getPurchaseOrderId(), event.getItems());

        for (PurchaseOrderCreatedEvent.PurchaseOrderItemEvent item : event.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));

            // Update stock
            long newStock = (product.getStock() != null ? product.getStock() : 0) + item.getQuantity();
            product.setStock(Math.toIntExact(newStock));

            // Update cost trung bình
            BigDecimal oldCost = product.getCost() != null ? product.getCost() : BigDecimal.ZERO;
            long oldStock = product.getStock() != null ? product.getStock() : 0;
            BigDecimal totalCost = oldCost.multiply(BigDecimal.valueOf(oldStock))
                    .add(item.getCost().multiply(BigDecimal.valueOf(item.getQuantity())));
            BigDecimal newAvgCost = totalCost.divide(BigDecimal.valueOf(newStock), RoundingMode.HALF_UP);
            product.setCost(newAvgCost);

            productRepository.save(product);
        }
    }

    @KafkaListener(topics = "order.created", groupId = "product-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent: orderId={}, items={}", event.getOrderId(), event.getItems());

        boolean allAvailable = true;
        List<OrderStockResponseEvent.OrderItem> responseItems = new ArrayList<>();

        for (OrderCreatedEvent.OrderItemEvent item : event.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));

            int currentStock = product.getStock() != null ? product.getStock() : 0;
            int reserved = product.getReserved() != null ? product.getReserved() : 0;

            if (currentStock < item.getQuantity()) {
                log.warn("Not enough stock for productId={}, required={}, available={}",
                        item.getProductId(), item.getQuantity(), currentStock);
                allAvailable = false;
            } else {
                // Đủ hàng -> giảm stock, tăng reserved
                product.setStock(currentStock - item.getQuantity());
                product.setReserved(reserved + item.getQuantity());
                productRepository.save(product);
                log.info("Reserved stock for productId={}, reserved={}, remainingStock={}",
                        item.getProductId(), product.getReserved(), product.getStock());
            }

            // Build response item đầy đủ thông tin
            OrderStockResponseEvent.OrderItem responseItem = OrderStockResponseEvent.OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .price(product.getPrice())
                    .costAtPurchase(product.getCost())
                    .quantity(item.getQuantity())
                    .build();
            responseItems.add(responseItem);
        }

        // Xác định status
        OrderStockResponseEvent.Status status = allAvailable
                ? OrderStockResponseEvent.Status.OK
                : OrderStockResponseEvent.Status.NOT_ENOUGH;

        // Gửi event về OrderService
        OrderStockResponseEvent responseEvent = OrderStockResponseEvent.builder()
                .orderId(event.getOrderId())
                .status(status)
                .items(responseItems)
                .build();

        productKafkaProducer.sendOrderStockResponseEvent(responseEvent);
    }
}

