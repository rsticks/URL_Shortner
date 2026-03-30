package faang.school.urlshortenerservice.service.analytics;

import lombok.Builder;

@Builder
public record IpMetadata(
        String country,
        String region,
        String city,
        String timezone,
        String asn,
        String provider,
        String networkType,
        String proxyStatus,
        String vpnStatus,
        String torStatus
) {
    public static IpMetadata unknown() {
        return IpMetadata.builder()
                .country("unknown")
                .region("unknown")
                .city("unknown")
                .timezone("unknown")
                .asn("unknown")
                .provider("unknown")
                .networkType("unknown")
                .proxyStatus("unknown")
                .vpnStatus("unknown")
                .torStatus("unknown")
                .build();
    }
}
