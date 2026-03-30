package faang.school.urlshortenerservice.repository.analytics;

import faang.school.urlshortenerservice.model.analytics.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {
    @Modifying
    @Query(value = "DELETE FROM click_event WHERE clicked_at < ?1", nativeQuery = true)
    int deleteOlderThan(OffsetDateTime cutoff);
}


