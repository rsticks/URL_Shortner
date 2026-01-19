package faang.school.urlshortenerservice.model.analytics;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "daily_link_stats")
public class DailyLinkStats {
    @EmbeddedId
    private DailyLinkStatsId id;

    @Column(name = "clicks", nullable = false)
    private long clicks;

    @Column(name = "unique_visitors", nullable = false)
    private long uniqueVisitors;
}


