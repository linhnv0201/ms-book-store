package product_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter @Setter
//@Builder giúp tự động sinh Builder Pattern cho class.
//Thay vì phải viết nhiều constructor hoặc setter, bạn có thể tạo object một cách linh hoạt, dễ đọc.
@Builder
//@AllArgsConstructor tự động tạo constructor chứa tất cả các field trong class
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE )
public class ApiResponse<T> {
    int code = 1000 ;
    String message;
    T result;
}