package faang.school.urlshortenerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank
        @Size(min = 3, max = 64)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Логин: только латиница, цифры, '.', '_' или '-'")
        String username,
        @NotBlank
        @Size(min = 6, max = 128)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Пароль: только латиница, цифры, '.', '_' или '-'")
        String password
) {
}


