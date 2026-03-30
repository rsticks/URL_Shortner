package faang.school.urlshortenerservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LoginRequest(
        @NotBlank
        @Size(min = 3, max = 64)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Логин: только латиница, цифры, '.', '_' или '-'")
        String username,
        @NotBlank
        @Size(min = 6, max = 72, message = "Пароль: от 6 до 72 символов")
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Пароль: только латиница, цифры, '.', '_' или '-'")
        String password,
        boolean rememberMe
) {
}


