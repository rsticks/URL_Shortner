package faang.school.urlshortenerservice.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

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
}


