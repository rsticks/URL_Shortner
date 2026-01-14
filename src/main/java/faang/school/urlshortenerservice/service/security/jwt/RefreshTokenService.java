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
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
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
        return refreshTokenRepository.findByIdAndRevokedFalse(id)
                .filter(t -> t.getExpiresAt().isAfter(OffsetDateTime.now()));
    }

    public void revoke(UUID id) {
        refreshTokenRepository.findById(id).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }

    public void revoke(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    public void addRefreshCookie(HttpHeaders headers, String refreshJwt) {
        ResponseCookie cookie = ResponseCookie.from(jwtProperties.refreshCookieName(), refreshJwt)
                .httpOnly(true)
                .secure(jwtProperties.cookieSecure())
                .sameSite(jwtProperties.cookieSameSite())
                .path("/api/v1/auth")
                .maxAge(Duration.ofSeconds(jwtProperties.refreshTtlSeconds()))
                .build();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clearRefreshCookie(HttpHeaders headers) {
        ResponseCookie cookie = ResponseCookie.from(jwtProperties.refreshCookieName(), "")
                .httpOnly(true)
                .secure(jwtProperties.cookieSecure())
                .sameSite(jwtProperties.cookieSameSite())
                .path("/api/v1/auth")
                .maxAge(Duration.ZERO)
                .build();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}


