package common_dto.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderStockResponseEvent {

    Long orderId;
    Status status;
    List<OrderItem> items;

    public enum Status {
        OK,              // tất cả sản phẩm đủ stock
        NOT_ENOUGH       // ít nhất 1 sản phẩm thiếu
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class OrderItem {
        Long productId;
        String productName;
        BigDecimal price;        // snapshot price
        BigDecimal costAtPurchase; // snapshot cost (nullable lúc tạo)
        Integer quantity;
    }
}
