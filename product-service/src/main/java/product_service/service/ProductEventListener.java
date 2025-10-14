package product_service.service;

import common_dto.PurchaseOrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import product_service.entity.Product;
import product_service.repo.ProductRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductEventListener {

    private final ProductRepository productRepository;

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
}

