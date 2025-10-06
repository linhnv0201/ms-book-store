package user_service.user.dto;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserPasswordUpdateRequest {

    @Size(min = 4, message = "Password phải có ít nhất 4 ký tự")
    String password;
}
