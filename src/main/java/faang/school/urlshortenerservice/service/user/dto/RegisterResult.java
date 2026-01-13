package faang.school.urlshortenerservice.service.user.dto;

import java.time.OffsetDateTime;

public record RegisterResult(
        Long id,
        String username,
        OffsetDateTime subscriptionExpiresAt,
        boolean subscribed
) {
}


