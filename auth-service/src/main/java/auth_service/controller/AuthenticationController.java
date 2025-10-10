package auth_service.controller;


import auth_service.dto.request.AuthenticationRequest;
import auth_service.dto.request.IntrospectRequest;
import auth_service.dto.request.LogoutRequest;
import auth_service.dto.request.RefreshRequest;
import auth_service.dto.response.ApiResponse;
import auth_service.dto.response.AuthenticationResponse;
import auth_service.dto.response.IntrospectResponse;
import auth_service.dto.response.RequireRefreshTokenResponse;
import auth_service.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    AuthenticationService authenticationService;

    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> login(
            @RequestBody AuthenticationRequest request,
            HttpServletResponse response) throws ParseException, JOSEException {
        ApiResponse<AuthenticationResponse> apiResponse = new ApiResponse<>();
        AuthenticationResponse result = authenticationService.authenticate(request, response);
        apiResponse.setResult(result);
        return apiResponse;
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        IntrospectResponse result = authenticationService.introspect(request);
        ApiResponse<IntrospectResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(result);
        return apiResponse;
    }

    @PostMapping("/refresh")
    ApiResponse<RequireRefreshTokenResponse> refresh(
            @RequestBody RefreshRequest request,
            HttpServletRequest httpRequest) throws ParseException, JOSEException {

        // Truyền cả request để service đọc access token cũ
        var result = authenticationService.refreshToken(request,httpRequest);

        ApiResponse<RequireRefreshTokenResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(result);
        return apiResponse;
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Logout successful");
        authenticationService.logout(request);
        return apiResponse;
    }

}