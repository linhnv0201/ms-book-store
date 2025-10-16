package payment_service.service;

import common_dto.dto.PaymentResponseStatusEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentKafkaProducer {

    private final KafkaTemplate<String, PaymentResponseStatusEvent> kafkaTemplate;

    public void sendPaymentCompletedEvent(PaymentResponseStatusEvent event) {

        kafkaTemplate.send("payment.response", event);
        log.info("Sent PaymentCompletedEvent: orderId={}, status={}", event.getOrderId(), event.getStatus());
    }
}
