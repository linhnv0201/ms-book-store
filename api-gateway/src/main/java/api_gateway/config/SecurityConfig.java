//package api_gateway.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.web.server.ServerHttpSecurity;
//import org.springframework.security.web.server.SecurityWebFilterChain;
//
//@Configuration
//public class SecurityConfig {
//
//    @Bean
//    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
//        http
//                .csrf(ServerHttpSecurity.CsrfSpec::disable)
//                .authorizeExchange(exchanges -> exchanges
//                        .pathMatchers("/auth/**").permitAll()
//                        .anyExchange().authenticated()
//                )
//                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
//                .formLogin(ServerHttpSecurity.FormLoginSpec::disable);
//
//        return http.build();
//    }
//}
