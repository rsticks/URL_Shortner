package faang.school.urlshortenerservice.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Data
@Validated
@ConfigurationProperties(prefix = "analytics")
public class AnalyticsProperties {
    /**
     * Secret used to generate daily-rotated pseudonymous visitor keys (HMAC-SHA256).
     * Do NOT reuse JWT secret in production; set via env: ANALYTICS_VISITOR_HMAC_SECRET.
     */
    @NotBlank
    private String visitorHmacSecret;

    /**
     * Retention for raw click events (click_event).
     * Example values: 30d, 7d, 12h
     */
    private Duration clickEventRetention = Duration.ofDays(30);

    /**
     * Cron for click_event cleanup job.
     * Default: every day at 03:00.
     */
    private String cleanClickEventsCron = "0 0 3 * * *";

    /**
     * Optional external metadata sources used to enrich redirects without storing raw IPs.
     */
    private GeoIpProperties geoIp = new GeoIpProperties();

    /**
     * Known search and social referrers used to classify traffic sources.
     */
    private ReferrerProperties referrer = new ReferrerProperties();

    @Data
    public static class GeoIpProperties {
        /**
         * Optional path to GeoLite2/GeoIP2 City MMDB.
         */
        private String cityDbPath;

        /**
         * Optional path to GeoLite2/GeoIP2 ASN MMDB.
         */
        private String asnDbPath;

        /**
         * Optional path to MaxMind Anonymous IP MMDB.
         */
        private String anonymousDbPath;
    }

    @Data
    public static class ReferrerProperties {
        private List<String> searchHosts = new ArrayList<>(List.of(
                "google.",
                "bing.com",
                "yandex.",
                "duckduckgo.com",
                "search.yahoo.com",
                "search.brave.com"
        ));

        private List<String> socialHosts = new ArrayList<>(List.of(
                "t.co",
                "twitter.com",
                "x.com",
                "facebook.com",
                "instagram.com",
                "linkedin.com",
                "t.me",
                "telegram.me",
                "vk.com",
                "youtube.com",
                "youtu.be",
                "reddit.com",
                "pinterest.com",
                "threads.net"
        ));
    }
}


