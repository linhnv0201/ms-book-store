package user_service.user.controller;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import user_service.user.dto.ApiResponse;
import user_service.user.dto.UserResponse;
import user_service.user.service.UserService;

@Slf4j
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalUserController {
    private final UserService userService;

    @GetMapping("/by-email")
    public ApiResponse<UserResponse> getUserByEmail(@RequestParam String email) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.getUserByEmail(email));
        return apiResponse;
    }
}

