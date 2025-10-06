package auth_service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/auth/helloWorld")
    public String helloWorld() {
        return "Hello world from Authentication Service !";
    }
}
