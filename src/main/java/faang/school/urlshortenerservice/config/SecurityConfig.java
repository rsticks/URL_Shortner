package faang.school.urlshortenerservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.urlshortenerservice.controller.handler.ErrorCode;
import faang.school.urlshortenerservice.dto.ErrorResponse;
import faang.school.urlshortenerservice.service.security.jwt.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final ObjectMapper objectMapper;
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        RegexRequestMatcher publicRedirect = new RegexRequestMatcher("^/api/v1/[A-Za-z0-9]{1,6}$", HttpMethod.GET.name());

        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/url").permitAll()
                .requestMatchers(publicRedirect).permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
        );

        // JWT filter (Authorization: Bearer <token>)
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // Return JSON errors and avoid browser Basic auth prompt
        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
            response.setStatus(401);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
            ErrorResponse body = new ErrorResponse(
                    LocalDateTime.now(),
                    ErrorCode.UNAUTHORIZED.getCode().value(),
                    ErrorCode.UNAUTHORIZED.getMessage(),
                    authException.getMessage()
            );
            objectMapper.writeValue(response.getWriter(), body);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
                    ErrorResponse body = new ErrorResponse(
                            LocalDateTime.now(),
                            ErrorCode.FORBIDDEN.getCode().value(),
                            ErrorCode.FORBIDDEN.getMessage(),
                            accessDeniedException.getMessage()
                    );
                    objectMapper.writeValue(response.getWriter(), body);
                })
        );

        return http.build();
    }
}


