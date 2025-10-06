package user_service.user.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter @Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserInfoUpdateRequest {
    String fullname;
    String phone;
    String address;
}