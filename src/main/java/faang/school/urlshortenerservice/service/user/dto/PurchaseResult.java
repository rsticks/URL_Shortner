package faang.school.urlshortenerservice.service.user.dto;

import java.time.OffsetDateTime;

public record PurchaseResult(
        String username,
        OffsetDateTime subscriptionExpiresAt,
        boolean subscribed
) {
}


