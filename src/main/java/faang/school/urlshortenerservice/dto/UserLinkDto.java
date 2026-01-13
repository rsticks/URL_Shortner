package faang.school.urlshortenerservice.dto;

import java.time.LocalDateTime;

public record UserLinkDto(
        String hash,
        String originalUrl,
        String shortUrl,
        LocalDateTime createdAt
) {
}


