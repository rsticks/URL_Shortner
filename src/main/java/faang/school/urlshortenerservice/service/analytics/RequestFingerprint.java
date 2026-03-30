package faang.school.urlshortenerservice.service.analytics;

import lombok.Builder;

@Builder
public record RequestFingerprint(
        String visitorHashDay,
        String referrerHost,
        String referrerCategory,
        String language,
        String deviceType,
        String osFamily,
        String osVersion,
        String browserFamily,
        String browserVersion,
        String country,
        String region,
        String city,
        String timezone,
        String asn,
        String provider,
        String networkType,
        String proxyStatus,
        String vpnStatus,
        String torStatus,
        String clientHintPlatform,
        String clientHintPlatformVersion,
        String clientHintMobile,
        String clientHintModel,
        boolean bot
) {
}


