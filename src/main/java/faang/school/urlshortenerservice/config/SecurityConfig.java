package faang.school.urlshortenerservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.urlshortenerservice.controller.handler.ErrorCode;
import faang.school.urlshortenerservice.dto.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        RegexRequestMatcher publicRedirect = new RegexRequestMatcher("^/api/v1/[A-Za-z0-9]{6}$", HttpMethod.GET.name());

        http.csrf(csrf -> csrf.disable());

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/url").permitAll()
                .requestMatchers(publicRedirect).permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
        );

        http.httpBasic(Customizer.withDefaults());

        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
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
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
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


