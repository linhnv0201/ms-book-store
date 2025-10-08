package api_gateway.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// DTO để map response từ Auth Service
record IntrospectRequest(String token) {}
record IntrospectResponse(boolean valid) {}

@FeignClient(name = "auth-service", path = "/auth")
public interface AuthServiceClient {

    @PostMapping("/introspect")
    IntrospectResponse introspect(@RequestBody IntrospectRequest request);
}
