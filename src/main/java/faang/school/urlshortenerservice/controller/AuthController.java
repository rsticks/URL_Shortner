package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.LoginRequest;
import faang.school.urlshortenerservice.dto.LoginResponse;
import faang.school.urlshortenerservice.dto.RegisterRequest;
import faang.school.urlshortenerservice.properties.JwtProperties;
import faang.school.urlshortenerservice.service.security.jwt.JwtService;
import faang.school.urlshortenerservice.service.security.jwt.RefreshTokenService;
import faang.school.urlshortenerservice.service.user.AppUserService;
import faang.school.urlshortenerservice.service.user.dto.RegisterResult;
import faang.school.urlshortenerservice.repository.AppUserRepository;
import faang.school.urlshortenerservice.service.security.AppUserDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("${api-version}/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AppUserService appUserService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final JwtProperties jwtProperties;
    private final AppUserRepository appUserRepository;
    private final AppUserDetailsService appUserDetailsService;

    @PostMapping("/register")
    public RegisterResult register(@Valid @RequestBody RegisterRequest request) {
        return appUserService.register(request.username(), request.email(), request.password());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        UserDetails principal = (UserDetails) auth.getPrincipal();
        String access = jwtService.generateAccessToken(principal);
        Instant exp = jwtService.getAccessExpirationFromNow();

        // Create server-side refresh token record and send refresh JWT via HttpOnly cookie
        var user = appUserRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not found"));
        var refreshRecord = refreshTokenService.create(user);
        String refreshJwt = jwtService.generateRefreshToken(user.getUsername(), refreshRecord.getId());

        HttpHeaders headers = new HttpHeaders();
        refreshTokenService.addRefreshCookie(headers, refreshJwt);
        return ResponseEntity.ok().headers(headers)
                .body(new LoginResponse("Bearer", access, exp.getEpochSecond(), user.getUsername()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(HttpServletRequest request) {
        String refreshJwt = readCookie(request, jwtProperties.refreshCookieName());
        if (refreshJwt == null || refreshJwt.isBlank()) {
            throw new ResponseStatusException(UNAUTHORIZED, "Refresh token missing");
        }

        String username = jwtService.extractUsername(refreshJwt);
        if (username == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Refresh token invalid");
        }
        if (!jwtService.isRefreshTokenValid(refreshJwt, username)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Refresh token expired/invalid");
        }

        UUID tokenId = jwtService.extractTokenId(refreshJwt);
        var existing = refreshTokenService.findActive(tokenId)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Refresh token revoked"));
        if (!existing.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Refresh token invalid");
        }

        // rotate
        refreshTokenService.revoke(existing);
        var newRecord = refreshTokenService.create(existing.getUser());
        String newRefreshJwt = jwtService.generateRefreshToken(existing.getUser().getUsername(), newRecord.getId());

        // issue new access
        UserDetails userDetails = appUserDetailsService.loadUserByUsername(username);
        String access = jwtService.generateAccessToken(userDetails);
        Instant exp = jwtService.getAccessExpirationFromNow();

        HttpHeaders headers = new HttpHeaders();
        refreshTokenService.addRefreshCookie(headers, newRefreshJwt);
        return ResponseEntity.ok().headers(headers)
                .body(new LoginResponse("Bearer", access, exp.getEpochSecond(), username));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String refreshJwt = readCookie(request, jwtProperties.refreshCookieName());
        if (refreshJwt != null && !refreshJwt.isBlank()) {
            try {
                UUID tokenId = jwtService.extractTokenId(refreshJwt);
                refreshTokenService.revoke(tokenId);
            } catch (Exception ignored) {
            }
        }
        HttpHeaders headers = new HttpHeaders();
        refreshTokenService.clearRefreshCookie(headers);
        return ResponseEntity.noContent().headers(headers).build();
    }

    private static String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (cookie != null && name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}


