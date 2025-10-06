package user_service.user.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponseNoRole {
    Long id;
    String email;
    String password;
    String fullname;
    String phone;
    String address;
}