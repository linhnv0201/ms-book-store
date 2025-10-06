package user_service.user.dto;

//import jakarta.validation.constraints.Size;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter @Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    String email;
    @Size(min = 4, message = "INVALID_PASSWORD")
    String password;
    String fullname;
    String phone;
    String address;
    Set<String> role;
}