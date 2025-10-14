package purchase_order_service.service;

import common_dto.PurchaseOrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, PurchaseOrderCreatedEvent> kafkaTemplate;

    public void sendPurchaseOrderCreatedEvent(PurchaseOrderCreatedEvent event) {
        log.info("Sending PurchaseOrderCreatedEvent to Kafka, orderId={}, items={}",
                event.getPurchaseOrderId(), event.getItems());
        kafkaTemplate.send("purchase_order.created", event);
    }
}
