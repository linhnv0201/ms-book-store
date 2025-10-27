package product_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL) // bỏ các field null khi serialize
public class ProductResponseForAdmin {
    Long id;
    String name;
    String author;
    String language;
    Set<CategoryResponse> categories;
    String description;
    BigDecimal price;
    BigDecimal cost;
    Integer stock;
    Integer soldQuantity;
    LocalDateTime createdAt;
    Boolean isVisible;
}
