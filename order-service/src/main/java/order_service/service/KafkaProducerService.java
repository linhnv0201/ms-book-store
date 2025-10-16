package order_service.service;

import common_dto.dto.OrderCreatedEvent;
import common_dto.dto.OrderSuccessOrFailEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaProducerService {

    KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    KafkaTemplate<String, OrderSuccessOrFailEvent> kafkaTemplate2;

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Sending OrderCreatedEvent to Kafka: {}", event);
        kafkaTemplate.send("order.created", event);
    }

    public void sendOrderSuccessOrFailEvent(OrderSuccessOrFailEvent event) {
        log.info("Sending OrderSuccessOrFailEvent to Kafka: {}", event);
        kafkaTemplate2.send("order.status", event);
    }
}
