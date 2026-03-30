package faang.school.urlshortenerservice.service.security.jwt;

import faang.school.urlshortenerservice.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;

    public String generateAccessToken(UserDetails user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(jwtProperties.ttlSeconds());
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .addClaims(Map.of("typ", "access"))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Instant getAccessExpirationFromNow() {
        return Instant.now().plusSeconds(jwtProperties.ttlSeconds());
    }

    public String generateRefreshToken(String username, UUID tokenId) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(jwtProperties.refreshTtlSeconds());
        return Jwts.builder()
                .setSubject(username)
                .setId(tokenId.toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .addClaims(Map.of("typ", "refresh", "rm", false))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String username, UUID tokenId, boolean rememberMe) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(jwtProperties.refreshTtlSeconds());
        return Jwts.builder()
                .setSubject(username)
                .setId(tokenId.toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .addClaims(Map.of("typ", "refresh", "rm", rememberMe))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Instant getRefreshExpirationFromNow() {
        return Instant.now().plusSeconds(jwtProperties.refreshTtlSeconds());
    }

    public String extractUsername(String token) {
        return parse(token).getBody().getSubject();
    }

    public String extractType(String token) {
        Object typ = parse(token).getBody().get("typ");
        return typ == null ? null : String.valueOf(typ);
    }

    public UUID extractTokenId(String token) {
        String id = parse(token).getBody().getId();
        if (id == null) {
            throw new IllegalArgumentException("Refresh token id missing");
        }
        return UUID.fromString(id);
    }

    public boolean extractRememberMe(String token) {
        Object rm = parse(token).getBody().get("rm");
        if (rm == null) return false;
        if (rm instanceof Boolean b) return b;
        return Boolean.parseBoolean(String.valueOf(rm));
    }

    public boolean isAccessTokenValid(String token, UserDetails user) {
        if (!"access".equals(extractType(token))) return false;
        String username = extractUsername(token);
        return username.equals(user.getUsername()) && !isExpired(token);
    }

    public boolean isRefreshTokenValid(String token, String expectedUsername) {
        if (!"refresh".equals(extractType(token))) return false;
        String username = extractUsername(token);
        return expectedUsername.equals(username) && !isExpired(token);
    }

    private boolean isExpired(String token) {
        Date exp = parse(token).getBody().getExpiration();
        return exp.before(new Date());
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token);
    }

    private SecretKey getKey() {
        // Allow passing either a base64 secret or a raw string secret.
        // HS256 requires at least 256-bit key length.
        byte[] bytes;
        String secret = jwtProperties.secret();
        try {
            bytes = Decoders.BASE64.decode(secret);
        } catch (Exception ignored) {
            bytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes for HS256");
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}


