package auth_service.controller;

import auth_service.repo.InvalidatedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/auth")
@RequiredArgsConstructor
public class InvalidatedTokenController {

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @GetMapping("/check-invalidated")
    public Boolean checkInvalidated(@RequestParam String jti) {
        return invalidatedTokenRepository.existsById(jti);
    }
}
