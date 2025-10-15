package order_service.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import order_service.dto.response.CurrentUserResponse;
import order_service.exception.AppException;
import order_service.exception.ErrorCode;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CurrentUserProvider {

    CustomJwtDecoder customJwtDecoder;

    public CurrentUserResponse getUserDetailsFromRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes())
                .getRequest();

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new AppException(ErrorCode.NOT_CORRECT_TOKEN);
        }

        token = token.substring(7); // bỏ "Bearer "
        Jwt jwt = customJwtDecoder.decode(token);
        Map<String, Object> claims = jwt.getClaims();

        CurrentUserResponse currentUser = new CurrentUserResponse();

        currentUser.setUserId(Long.valueOf(claims.get("sub").toString()));
        currentUser.setEmail(claims.get("email").toString());

        return currentUser;
    }
}
