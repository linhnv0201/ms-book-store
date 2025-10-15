package product_service.service;


import common_dto.dto.OrderStockResponseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductKafkaProducer {

    private final KafkaTemplate<String, OrderStockResponseEvent> kafkaTemplate;

    public void sendOrderStockResponseEvent(OrderStockResponseEvent event) {
        log.info("Sending OrderStockResponseEvent to Kafka: orderId={}, status={}, items={}",
                event.getOrderId(), event.getStatus(), event.getItems());
        kafkaTemplate.send("order.stock.response", event);
    }
}
