package faang.school.urlshortenerservice.service.analytics;

import lombok.Builder;

@Builder
public record RequestFingerprint(
        String visitorHashDay,
        String referrerHost,
        String language,
        String deviceType,
        String osFamily,
        String browserFamily,
        boolean bot
) {
}


