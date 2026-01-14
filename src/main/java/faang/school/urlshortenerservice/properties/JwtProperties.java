package faang.school.urlshortenerservice.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        long ttlSeconds,
        long refreshTtlSeconds,
        String refreshCookieName,
        boolean cookieSecure,
        String cookieSameSite
) {
}


