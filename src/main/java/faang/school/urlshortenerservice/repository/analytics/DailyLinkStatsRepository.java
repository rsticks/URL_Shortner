package faang.school.urlshortenerservice.repository.analytics;

import faang.school.urlshortenerservice.model.analytics.DailyLinkStats;
import faang.school.urlshortenerservice.model.analytics.DailyLinkStatsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DailyLinkStatsRepository extends JpaRepository<DailyLinkStats, DailyLinkStatsId> {

    List<DailyLinkStats> findAllByIdUrlHashAndIdDayBetweenOrderByIdDay(String urlHash, LocalDate from, LocalDate to);

    @Modifying
    @Query(value = """
            INSERT INTO daily_link_stats(url_hash, day, clicks, unique_visitors)
            VALUES (:hash, :day, 1, :uniqueInc)
            ON CONFLICT (url_hash, day)
            DO UPDATE SET
              clicks = daily_link_stats.clicks + 1,
              unique_visitors = daily_link_stats.unique_visitors + :uniqueInc;
            """, nativeQuery = true)
    void upsertIncrement(@Param("hash") String hash, @Param("day") LocalDate day, @Param("uniqueInc") long uniqueInc);
}


