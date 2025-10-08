package product_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductUpdateRequest {

    String name;
    String author;
    String language;

    Set<Long> categoryIds; // có thể null nếu không cập nhật category

    String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    BigDecimal price;   // validate chỉ khi có giá mới

//    @DecimalMin(value = "0.0", inclusive = true, message = "Cost must be >= 0")
//    BigDecimal cost;

    // Cho phép admin chỉnh sửa hiển thị sản phẩm
    Boolean isVisible;    // Cho phép admin chỉnh sửa hiển thị sản phẩm
}
