package user_service.user.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import user_service.user.enums.Role;
import user_service.user.mapper.UserMapper;
import user_service.user.dto.*;
import user_service.user.entity.User;
import user_service.user.exception.AppException;
import user_service.user.exception.ErrorCode;
import user_service.user.repo.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static user_service.user.UserSpecification.*;


@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    JdbcTemplate jdbcTemplate;

    @Override
    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        // validate role
        for (String roleStr : request.getRole()) {
            try {
                Role.valueOf(roleStr);
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.INVALID_ROLE);
            }
        }

        User user = userMapper.toCreateUser(request);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponse registerCustomer(UserCreationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        User user = userMapper.toCreateUser(request);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Set.of(Role.CUSTOMER));
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userMapper.toUpdateUser(user, request);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    public UserResponse updateMyInfo(UserInfoUpdateRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userMapper.toUpdateUserInfo(user, request);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    public UserResponse updateMyPassword(UserPasswordUpdateRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userMapper.toUserResponse(userRepository.save(user));
    }


    @Override
    public List<UserResponse> getUsers() {
        log.info("getUsers()");
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse).toList();
    }

    @Override
    public List<UserResponse> getUsersByRole(String role) {
        Role roleEnum = Role.valueOf(role);
        return userRepository.findByRole(roleEnum).stream()
                .map(userMapper::toUserResponse).toList();
    }

    @Override
    public UserResponse getUser(Long id) {
        return userMapper.toUserResponse(userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));

    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        userRepository.deleteById(id);
    }


    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponse getMyInfoJDBC() {
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();

        Map<String, Object> result = jdbcTemplate
                .queryForMap("select email, fullname, phone, address from users where email = ?", email);

        // Chuyển Map sang UserResponse
        UserResponse response = new UserResponse();
        response.setEmail((String) result.get("email"));
        response.setFullname((String) result.get("fullname"));
        response.setPhone((String) result.get("phone"));
        response.setAddress((String) result.get("address"));

        return response;
    }


    @Override
    public Page<UserResponse> searchUsers(String email, String fullname, String role, Pageable pageable) {
        return userRepository.findAll(
                hasEmail(email)
                        .and(hasFullName(fullname))
                        .and(hasRole(role))
                , pageable
        ).map(userMapper::toUserResponse);
    }


}
