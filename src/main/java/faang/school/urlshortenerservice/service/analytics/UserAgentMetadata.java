package faang.school.urlshortenerservice.service.analytics;

import lombok.Builder;

@Builder
public record UserAgentMetadata(
        String deviceType,
        String browserFamily,
        String browserVersion,
        String osFamily,
        String osVersion,
        String clientHintPlatform,
        String clientHintPlatformVersion,
        String clientHintMobile,
        String clientHintModel
) {
    public static UserAgentMetadata unknown() {
        return UserAgentMetadata.builder()
                .deviceType("unknown")
                .browserFamily("unknown")
                .browserVersion("unknown")
                .osFamily("unknown")
                .osVersion("unknown")
                .clientHintPlatform("unknown")
                .clientHintPlatformVersion("unknown")
                .clientHintMobile("unknown")
                .clientHintModel("unknown")
                .build();
    }
}
