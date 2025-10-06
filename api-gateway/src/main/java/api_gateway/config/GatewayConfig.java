//package api_gateway.config;
//
//import org.springframework.cloud.gateway.route.RouteLocator;
//import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
//import org.springframework.context.annotation.Bean;
//
//public class GatewayConfig {
//    @Bean
//    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
//        return builder.routes()
//                .route("auth-service", r -> r
//                        .path("/auth/**")
//                        .uri("lb://auth-service"))
//                .route("auth-service", r -> r
//                        .path("/users/**")
//                        .uri("lb://auth-service"))
//                .build();
//    }
//}
