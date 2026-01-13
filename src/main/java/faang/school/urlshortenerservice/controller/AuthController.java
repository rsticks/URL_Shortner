package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.RegisterRequest;
import faang.school.urlshortenerservice.service.user.AppUserService;
import faang.school.urlshortenerservice.service.user.dto.RegisterResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api-version}/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AppUserService appUserService;

    @PostMapping("/register")
    public RegisterResult register(@Valid @RequestBody RegisterRequest request) {
        return appUserService.register(request.username(), request.password());
    }
}


