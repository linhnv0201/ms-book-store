package api_gateway.filter;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Slf4j
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        // Bỏ qua các public endpoint
        if (path.startsWith("/auth")
                || (path.startsWith("/products") && HttpMethod.GET.equals(method))
                || path.startsWith("/payments/vn-pay-callback")) {
            return chain.filter(exchange); // bypass JWT
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid token");
        }

        String token = authHeader.substring(7);

        // Gọi AuthService để introspect token
        return webClientBuilder.build()
                .post()
                .uri("lb://auth-service/auth/introspect")
                .bodyValue(Map.of("token", token))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(response -> {
                    boolean valid = response.path("result").path("valid").asBoolean(false);
                    if (valid) {
                        // Token hợp lệ → forward token xuống service
                        ServerWebExchange mutatedExchange = exchange.mutate()
                                .request(r -> r.headers(h -> h.set(HttpHeaders.AUTHORIZATION, authHeader)))
                                .build();
                        return chain.filter(mutatedExchange);
                    } else {
                        return unauthorized(exchange, "Invalid or expired token");
                    }
                })
                .onErrorResume(e -> {
                    log.error("[Gateway] Introspect token error", e);
                    return unauthorized(exchange, "Token introspect failed");
                });
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().set(HttpHeaders.CONTENT_TYPE, "application/json");
        var buffer = exchange.getResponse().bufferFactory()
                .wrap(("{\"error\":\"" + message + "\"}").getBytes());
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1; // ưu tiên cao, chạy sớm
    }
}
