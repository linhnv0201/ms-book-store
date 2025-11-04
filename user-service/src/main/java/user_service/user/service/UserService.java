package user_service.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import user_service.user.dto.*;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserCreationRequest request);
    UserResponse updateUser(Long id, UserUpdateRequest request);
    UserResponse updateMyInfo(UserInfoUpdateRequest request);
    UserResponse updateMyPassword(UserPasswordUpdateRequest request);
    List<UserResponse> getUsers();
    List<UserResponse> getUsersByRole(String role);
    UserResponse getUser(Long id);
    void deleteUser(Long id);
    UserResponse getMyInfo();
//    Map<String, Object> getMyInfoJDBC();
    UserResponse registerCustomer(UserCreationRequest request);
    UserResponse getMyInfoJDBC();
    Page<UserResponse> searchUsers(String email, String fullname, String role, Pageable pageable);
    UserResponseForAuthentication getUserByEmailPassword(String email, String password);
}
