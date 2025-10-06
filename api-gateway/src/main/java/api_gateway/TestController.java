package api_gateway;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class TestController {
    private final DiscoveryClient discoveryClient;
    private final WebClient.Builder webClientBuilder;

    public TestController(DiscoveryClient discoveryClient, WebClient.Builder webClientBuilder) {
        this.discoveryClient = discoveryClient;
        this.webClientBuilder = webClientBuilder;
    }

    @GetMapping("/helloEureka")
    public Mono<String> helloWorld() {
        ServiceInstance serviceInstance = discoveryClient.getInstances("auth-service").get(0);
        return webClientBuilder.build()
                .get()
                .uri(serviceInstance.getUri() + "/helloWorld")
                .retrieve()
                .bodyToMono(String.class);
    }
}

