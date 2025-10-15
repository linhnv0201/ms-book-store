package common_dto.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PurchaseOrderCreatedEvent {
    Long purchaseOrderId;
    List<PurchaseOrderItemEvent> items;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PurchaseOrderItemEvent {
        Long productId;
        Integer quantity;
        BigDecimal cost;
    }
}
