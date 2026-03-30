package faang.school.urlshortenerservice.service.security.jwt;

import faang.school.urlshortenerservice.model.AppUser;
import faang.school.urlshortenerservice.model.RefreshToken;
import faang.school.urlshortenerservice.properties.JwtProperties;
import faang.school.urlshortenerservice.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@SuppressWarnings("NullAway")
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    public RefreshToken create(AppUser user) {
        RefreshToken token = new RefreshToken();
        token.setId(UUID.randomUUID());
        token.setUser(user);
        token.setRevoked(false);
        token.setCreatedAt(OffsetDateTime.now());
        token.setExpiresAt(OffsetDateTime.now().plusSeconds(jwtProperties.refreshTtlSeconds()));
        return refreshTokenRepository.save(token);
    }

    public Optional<RefreshToken> findActive(UUID id) {
        return refreshTokenRepository.findByIdAndRevokedFalse(Objects.requireNonNull(id, "id"))
                .filter(t -> t.getExpiresAt().isAfter(OffsetDateTime.now()));
    }

    public void revoke(UUID id) {
        refreshTokenRepository.findById(Objects.requireNonNull(id, "id")).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }

    public void revoke(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    public void addRefreshCookie(HttpHeaders headers, String refreshJwt) {
        String name = Objects.requireNonNull(jwtProperties.refreshCookieName(), "refreshCookieName");
        String sameSite = Objects.requireNonNull(jwtProperties.cookieSameSite(), "cookieSameSite");
        ResponseCookie cookie = ResponseCookie.from(name, Objects.requireNonNull(refreshJwt, "refreshJwt"))
                .httpOnly(true)
                .secure(jwtProperties.cookieSecure())
                .sameSite(sameSite)
                .path("/api/v1/auth")
                .build();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void addRefreshCookie(HttpHeaders headers, String refreshJwt, boolean rememberMe) {
        String name = Objects.requireNonNull(jwtProperties.refreshCookieName(), "refreshCookieName");
        String sameSite = Objects.requireNonNull(jwtProperties.cookieSameSite(), "cookieSameSite");
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, Objects.requireNonNull(refreshJwt, "refreshJwt"))
                .httpOnly(true)
                .secure(jwtProperties.cookieSecure())
                .sameSite(sameSite)
                .path("/api/v1/auth");

        // If rememberMe=false -> session cookie (no Max-Age), user will be logged out after closing the browser.
        if (rememberMe) {
            builder = builder.maxAge(Objects.requireNonNull(Duration.ofSeconds(jwtProperties.refreshTtlSeconds()), "maxAge"));
        }

        headers.add(HttpHeaders.SET_COOKIE, builder.build().toString());
    }

    public void clearRefreshCookie(HttpHeaders headers) {
        String name = Objects.requireNonNull(jwtProperties.refreshCookieName(), "refreshCookieName");
        String sameSite = Objects.requireNonNull(jwtProperties.cookieSameSite(), "cookieSameSite");
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(jwtProperties.cookieSecure())
                .sameSite(sameSite)
                .path("/api/v1/auth")
                .maxAge(Objects.requireNonNull(Duration.ZERO, "maxAge"))
                .build();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}


