package order_service.service;

import common_dto.dto.OrderStockResponseEvent;
import common_dto.dto.OrderSuccessOrFailEvent;
import common_dto.dto.PaymentResponseStatusEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import order_service.enums.Status;
import order_service.repo.OrderItemRepository;
import order_service.repo.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import order_service.entity.Order;
import order_service.entity.OrderItem;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderEventListener {

    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    KafkaProducerService kafkaProducerService;


    @KafkaListener(topics = "order.stock.response", groupId = "order-service-group")
    @Transactional
    public void handleOrderStockResponse(OrderStockResponseEvent event) {
        log.info("Received OrderStockResponseEvent: orderId={}, status={}", event.getOrderId(), event.getStatus());

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));

        // Build a map of existing order items by productId for easy update
        Map<Long, OrderItem> itemMap = new HashMap<>();
        order.getItems().forEach(i -> itemMap.put(i.getProductId(), i));

        BigDecimal totalAmount = BigDecimal.ZERO;

        // Cập nhật tất cả item dựa trên event
        for (OrderStockResponseEvent.OrderItem itemEvent : event.getItems()) {
            OrderItem orderItem = itemMap.get(itemEvent.getProductId());
            if (orderItem != null) {
                orderItem.setProductName(itemEvent.getProductName());
                orderItem.setPrice(itemEvent.getPrice());
                orderItem.setCost(itemEvent.getCostAtPurchase());
                orderItem.setQuantity(itemEvent.getQuantity());
                orderItemRepository.save(orderItem);

                totalAmount = totalAmount.add(
                        itemEvent.getPrice().multiply(BigDecimal.valueOf(itemEvent.getQuantity())));
            }
        }

        order.setTotalAmount(totalAmount);

        // Cập nhật status order dựa trên event status
        if (event.getStatus() == OrderStockResponseEvent.Status.OK) {
            order.setStatus(Status.PENDING_PAYMENT);
        } else {
            order.setStatus(Status.CANCELLED);
        }

        orderRepository.save(order);
        log.info("Updated Order id={} status={} after stock check", order.getId(), order.getStatus());
    }

    @KafkaListener(topics = "payment.response", groupId = "order-service-group")
    @Transactional
    public void handlePaymentCompleted(PaymentResponseStatusEvent event) {
        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if ("SUCCESS".equals(event.getStatus())) {
            order.setStatus(Status.PAID);


            OrderSuccessOrFailEvent event1 = new OrderSuccessOrFailEvent();
            event1.setStatus(event.getStatus());

            List<OrderSuccessOrFailEvent.OrderItemEvent> items = order.getItems().stream()
                    .map(item -> OrderSuccessOrFailEvent.OrderItemEvent.builder()
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .build())
                    .collect(Collectors.toList());

            event1.setItems(items);
            kafkaProducerService.sendOrderSuccessOrFailEvent(event1);
        } else {
            order.setStatus(Status.PENDING_PAYMENT); // cho retry
        }
        orderRepository.save(order);
    }
}
