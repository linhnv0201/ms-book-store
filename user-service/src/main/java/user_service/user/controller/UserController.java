package user_service.user.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import user_service.user.dto.*;
import user_service.user.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/helloUser")
    public String helloUser() {
        return "hello from UserService";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> createUser(@RequestBody UserCreationRequest request) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.createUser(request));
        return apiResponse;
    }

    // Update User
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UserUpdateRequest request) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.updateUser(id, request));
        apiResponse.setMessage("Successfully updated user");
        return apiResponse;
    }

    @GetMapping
//    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    ApiResponse<List<UserResponse>> getUsers() {
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getUsers())
                .build();
    }

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody @Valid UserCreationRequest request) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Register successful");
        apiResponse.setResult(userService.registerCustomer(request));
        return apiResponse;
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    ApiResponse<List<UserResponse>> getUsersByRole(@PathVariable String role) {
        ApiResponse<List<UserResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.getUsersByRole(role));
        return apiResponse;
    }

    @GetMapping("/spec/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<UserResponse>> searchUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        ApiResponse<Page<UserResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Successfully search users");
        apiResponse.setResult(userService.searchUsers(email, fullName, role, PageRequest.of(page, size)));
        return apiResponse;
    }


    //    // Get user by id
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse>    getUser(@PathVariable Long id,
                                             @RequestHeader(value = "Authorization", required = false) String authHeader) {
        System.out.println("Authorization header: " + authHeader); // <-- check token
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.getUser(id));
        return apiResponse;
    }

//    @GetMapping("/{id}")
//    public ApiResponse<UserResponse> getUser (@PathVariable Long id) {
//        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
//        apiResponse.setResult(userService.getUser(id));
//        return apiResponse;
//    }


    // Delete user
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);

        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("User deleted successfully");
        return apiResponse;
    }

    //có danh sách role
    @GetMapping("/myInfo")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

    //    @GetMapping("/myInfoJDBC")
//    public ApiResponse<Map<String, Object>> getMyInfoJDBC() {
//        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>();
//        apiResponse.setResult(userService.getMyInfoJDBC());
//        return apiResponse;
//    }
    @GetMapping("/myInfoJDBC")
    public ApiResponse<UserResponse> getMyInfoJDBC() {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.getMyInfoJDBC());
        return apiResponse;
    }

    // Update User
    @PutMapping("/myInfo")
    public ApiResponse<UserResponse> updateMyInfo(@RequestBody @Valid UserInfoUpdateRequest request) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.updateMyInfo(request));
        apiResponse.setMessage("Successfully updated your info");
        return apiResponse;
    }

    @PutMapping("/update-password")
    public ApiResponse<UserResponse> updatePassword(@RequestBody @Valid UserPasswordUpdateRequest request) {
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.updateMyPassword(request));
        apiResponse.setMessage("Successfully updated your password");
        return apiResponse;
    }
}
