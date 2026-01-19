package faang.school.urlshortenerservice.repository.analytics;

import faang.school.urlshortenerservice.model.analytics.DailyLinkStatsDim;
import faang.school.urlshortenerservice.model.analytics.DailyLinkStatsDimId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DailyLinkStatsDimRepository extends JpaRepository<DailyLinkStatsDim, DailyLinkStatsDimId> {

    @Modifying
    @Query(value = """
            INSERT INTO daily_link_stats_dim(url_hash, day, dim_type, dim_value, clicks)
            VALUES (:hash, :day, :type, :value, 1)
            ON CONFLICT (url_hash, day, dim_type, dim_value)
            DO UPDATE SET clicks = daily_link_stats_dim.clicks + 1;
            """, nativeQuery = true)
    void upsertIncrement(@Param("hash") String hash,
                         @Param("day") LocalDate day,
                         @Param("type") String type,
                         @Param("value") String value);

    @Query(value = """
            SELECT dim_value AS value, SUM(clicks) AS clicks
            FROM daily_link_stats_dim
            WHERE url_hash = :hash
              AND day BETWEEN :from AND :to
              AND dim_type = :type
            GROUP BY dim_value
            ORDER BY clicks DESC
            LIMIT :limit;
            """, nativeQuery = true)
    List<DimStatView> findTop(@Param("hash") String hash,
                              @Param("from") LocalDate from,
                              @Param("to") LocalDate to,
                              @Param("type") String type,
                              @Param("limit") int limit);
}


