package user_service.user.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter @Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
//    @Size(min = 4, message = "Password phải có ít nhất 4 ký tự")
//    @Nullable
//    String password;
    String fullname;
    String phone;
    String address;
    Set<String> role;
}