package faang.school.urlshortenerservice.repository.analytics;

import faang.school.urlshortenerservice.model.analytics.DailyLinkStats;
import faang.school.urlshortenerservice.model.analytics.DailyLinkStatsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface DailyLinkStatsRepository extends JpaRepository<DailyLinkStats, DailyLinkStatsId> {

    List<DailyLinkStats> findAllByIdUrlHashAndIdDayBetweenOrderByIdDay(String urlHash, LocalDate from, LocalDate to);

    @Modifying
    @Query(value = """
            INSERT INTO daily_link_stats(url_hash, day, clicks, unique_visitors)
            VALUES (?1, ?2, 0, 0)
            ON CONFLICT (url_hash, day) DO NOTHING
            """, nativeQuery = true)
    void ensureRow(String hash, LocalDate day);

    @Modifying
    @Query(value = """
            INSERT INTO daily_link_stats(url_hash, day, clicks, unique_visitors)
            VALUES (?1, ?2, 1, ?3)
            ON CONFLICT (url_hash, day)
            DO UPDATE SET
              clicks = daily_link_stats.clicks + 1,
              unique_visitors = daily_link_stats.unique_visitors + ?3
            """, nativeQuery = true)
    void upsertIncrement(String hash, LocalDate day, long uniqueInc);
}


