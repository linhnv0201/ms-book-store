package auth_service.openfeign;

import auth_service.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", path = "/internal/users")
public interface UserClient {

    @GetMapping("/by-email")
    ApiResponse<UserResponse> getUserByEmail(@RequestParam("email") String email);
}
