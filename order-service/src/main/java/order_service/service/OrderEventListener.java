package order_service.service;

import common_dto.dto.OrderStockResponseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import order_service.enums.Status;
import order_service.repo.OrderItemRepository;
import order_service.repo.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import order_service.entity.Order;
import order_service.entity.OrderItem;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @KafkaListener(topics = "order.stock.response", groupId = "order-service-group")
    @Transactional
    public void handleOrderStockResponse(OrderStockResponseEvent event) {
        log.info("Received OrderStockResponseEvent: orderId={}, status={}", event.getOrderId(), event.getStatus());

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));

        // Build a map of existing order items by productId for easy update
        Map<Long, OrderItem> itemMap = new HashMap<>();
        order.getItems().forEach(i -> itemMap.put(i.getProductId(), i));

        // Cập nhật tất cả item dựa trên event
        for (OrderStockResponseEvent.OrderItem itemEvent : event.getItems()) {
            OrderItem orderItem = itemMap.get(itemEvent.getProductId());
            if (orderItem != null) {
                orderItem.setProductName(itemEvent.getProductName());
                orderItem.setPrice(itemEvent.getPrice());
                orderItem.setCost(itemEvent.getCostAtPurchase());
                orderItem.setQuantity(itemEvent.getQuantity());
                orderItemRepository.save(orderItem);
            }
        }

        // Cập nhật status order dựa trên event status
        if (event.getStatus() == OrderStockResponseEvent.Status.OK) {
            order.setStatus(Status.PENDING_PAYMENT);
        } else {
            order.setStatus(Status.CANCELLED);
        }

        orderRepository.save(order);
        log.info("Updated Order id={} status={} after stock check", order.getId(), order.getStatus());
    }
}
