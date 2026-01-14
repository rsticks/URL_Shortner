package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByIdAndRevokedFalse(UUID id);

    long deleteAllByExpiresAtBefore(OffsetDateTime before);
}


