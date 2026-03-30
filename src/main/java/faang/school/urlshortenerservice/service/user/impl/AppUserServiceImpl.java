package faang.school.urlshortenerservice.service.user.impl;

import faang.school.urlshortenerservice.exception.SubscriptionRequiredException;
import faang.school.urlshortenerservice.exception.UserAlreadyExistsException;
import faang.school.urlshortenerservice.model.AppUser;
import faang.school.urlshortenerservice.repository.AppUserRepository;
import faang.school.urlshortenerservice.service.user.AppUserService;
import faang.school.urlshortenerservice.service.user.dto.PurchaseResult;
import faang.school.urlshortenerservice.service.user.dto.RegisterResult;
import faang.school.urlshortenerservice.service.user.dto.SubscriptionPlan;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService {
    private static final int MAX_BCRYPT_PASSWORD_LENGTH = 72;

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public RegisterResult register(String username, String email, String rawPassword) {
        if (appUserRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username already exists: " + username);
        }
        if (appUserRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email already exists: " + email);
        }
        if (rawPassword.length() > MAX_BCRYPT_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Пароль: от 6 до 72 символов");
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        AppUser saved = appUserRepository.save(user);
        return new RegisterResult(saved.getId(), saved.getUsername(), saved.getSubscriptionExpiresAt(), isSubscribed(saved));
    }

    @Override
    public AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new EntityNotFoundException("No authenticated user");
        }
        return appUserRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + authentication.getName()));
    }

    @Override
    public boolean isSubscribed(AppUser user) {
        OffsetDateTime expiresAt = user.getSubscriptionExpiresAt();
        return expiresAt != null && expiresAt.isAfter(OffsetDateTime.now());
    }

    @Override
    public void ensureSubscribed() {
        AppUser user = getCurrentUser();
        if (!isSubscribed(user)) {
            throw new SubscriptionRequiredException("Subscription required to create short links");
        }
    }

    @Override
    public PurchaseResult purchaseSubscription(SubscriptionPlan plan) {
        AppUser user = getCurrentUser();
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime base = user.getSubscriptionExpiresAt();
        if (base == null || base.isBefore(now)) {
            base = now;
        }
        user.setSubscriptionExpiresAt(base.plusDays(plan.getDays()));
        AppUser saved = appUserRepository.save(user);
        return new PurchaseResult(saved.getUsername(), saved.getSubscriptionExpiresAt(), isSubscribed(saved));
    }
}


