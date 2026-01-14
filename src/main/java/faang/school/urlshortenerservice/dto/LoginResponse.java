package faang.school.urlshortenerservice.dto;

public record LoginResponse(
        String tokenType,
        String accessToken,
        long expiresAtEpochSeconds,
        String username
) {
}


