package auth_service.service;

import auth_service.dto.request.AuthenticationRequest;
import auth_service.dto.request.IntrospectRequest;
import auth_service.dto.request.LogoutRequest;
import auth_service.dto.request.RefreshRequest;
import auth_service.dto.response.AuthenticationResponse;
import auth_service.dto.response.IntrospectResponse;
import auth_service.dto.response.RequireRefreshTokenResponse;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.text.ParseException;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest, HttpServletResponse response) throws ParseException, JOSEException;

    IntrospectResponse introspect(IntrospectRequest request) throws ParseException, JOSEException;

    void logout(LogoutRequest request) throws ParseException, JOSEException;

//    RequireRefreshTokenResponse refreshToken(RefreshRequest request, HttpServletRequest httpRequest) throws ParseException, JOSEException;
}
