package user_service.user.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import user_service.user.enums.Role;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Long id;
    String email;
    String password;
    String fullname;
    String phone;
    String address;
    Set<Role> role;
}