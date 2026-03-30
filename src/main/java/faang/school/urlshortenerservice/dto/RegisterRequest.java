package faang.school.urlshortenerservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Size(min = 3, max = 64)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Логин: только латиница, цифры, '.', '_' или '-'")
        String username,
        @NotBlank
        @Email(message = "Email: некорректный формат")
        @Size(max = 255)
        String email,
        @NotBlank
        @Size(min = 6, max = 72, message = "Пароль: от 6 до 72 символов")
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Пароль: только латиница, цифры, '.', '_' или '-'")
        String password
) {
}


