package faang.school.urlshortenerservice.repository.analytics;

import faang.school.urlshortenerservice.model.analytics.DailyLinkStatsDim;
import faang.school.urlshortenerservice.model.analytics.DailyLinkStatsDimId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface DailyLinkStatsDimRepository extends JpaRepository<DailyLinkStatsDim, DailyLinkStatsDimId> {

    @Modifying
    @Query(value = """
            INSERT INTO daily_link_stats_dim(url_hash, day, dim_type, dim_value, clicks)
            VALUES (?1, ?2, ?3, ?4, 1)
            ON CONFLICT (url_hash, day, dim_type, dim_value)
            DO UPDATE SET clicks = daily_link_stats_dim.clicks + 1
            """, nativeQuery = true)
    void upsertIncrement(String hash, LocalDate day, String type, String value);

    @Query(value = """
            SELECT dim_value AS value, SUM(clicks) AS clicks
            FROM daily_link_stats_dim
            WHERE url_hash = ?1
              AND day BETWEEN ?2 AND ?3
              AND dim_type = ?4
            GROUP BY dim_value
            ORDER BY clicks DESC
            LIMIT ?5
            """, nativeQuery = true)
    List<DimStatView> findTop(String hash, LocalDate from, LocalDate to, String type, int limit);
}


