package faang.school.urlshortenerservice.repository.analytics;

import faang.school.urlshortenerservice.model.analytics.DailyUniqueVisitor;
import faang.school.urlshortenerservice.model.analytics.DailyUniqueVisitorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface DailyUniqueVisitorRepository extends JpaRepository<DailyUniqueVisitor, DailyUniqueVisitorId> {

    @Modifying
    @Query(value = """
            INSERT INTO daily_unique_visitor(url_hash, day, visitor_hash_day)
            VALUES (?1, ?2, ?3)
            ON CONFLICT (url_hash, day, visitor_hash_day) DO NOTHING;
            """, nativeQuery = true)
    int tryInsert(String hash, LocalDate day, String visitorHashDay);
}


