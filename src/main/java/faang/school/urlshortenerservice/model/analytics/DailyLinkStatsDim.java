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
@Table(name = "daily_link_stats_dim")
public class DailyLinkStatsDim {
    @EmbeddedId
    private DailyLinkStatsDimId id;

    @Column(name = "clicks", nullable = false)
    private long clicks;
}


