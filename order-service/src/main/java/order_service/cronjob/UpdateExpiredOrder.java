package order_service.cronjob;

import common_dto.dto.OrderSuccessOrFailEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import order_service.entity.Order;
import order_service.enums.Status;
import order_service.repo.OrderRepository;
import order_service.service.KafkaProducerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UpdateExpiredOrder {
    OrderRepository orderRepository;
    KafkaProducerService kafkaProducerService;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void updateExpiredOrders() {
        log.info("Checking for expired orders...");

        LocalDateTime now = LocalDateTime.now();
        List<Order> orders = orderRepository.findAll();

        for (Order order : orders) {
            if (order.getStatus() == Status.PENDING_PAYMENT
                    && order.getCreatedAt().plusMinutes(30).isBefore(now)) {
                log.info("Order {} is expired. Updating status...", order.getId());
                order.setStatus(Status.EXPIRED);
                orderRepository.save(order);

                // Bắn event hủy reserved stock
                OrderSuccessOrFailEvent event = new OrderSuccessOrFailEvent();
                event.setStatus("EXPIRED");
                List<OrderSuccessOrFailEvent.OrderItemEvent> items = order.getItems().stream()
                        .map(item -> OrderSuccessOrFailEvent.OrderItemEvent.builder()
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .build())
                        .collect(Collectors.toList());
                event.setItems(items);

                kafkaProducerService.sendOrderSuccessOrFailEvent(event);
            }
        }
    }
}
