package cart_service.service;

import cart_service.repo.CartItemRepository;
import cart_service.repo.CartRepository;
import common_dto.dto.OrderSuccessOrFailEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderStatusListener {
    CartService cartService;

    @KafkaListener(topics = "order.status", groupId = "cart-service-group")
    public void handleOrderStatus(OrderSuccessOrFailEvent event) {
        log.info("Received OrderSuccessOrFailEvent: status={}, items={}", event.getStatus(), event.getItems());

        if ("SUCCESS".equalsIgnoreCase(event.getStatus())) {
            for (OrderSuccessOrFailEvent.OrderItemEvent itemEvent : event.getItems()) {
                Long productId = itemEvent.getProductId();

                cartService.removeFromCart(productId);

                log.info("Remove productId={} from cart", productId);
            }
        }
    }
}
