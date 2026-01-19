package faang.school.urlshortenerservice.model.analytics;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "daily_unique_visitor")
public class DailyUniqueVisitor {
    @EmbeddedId
    private DailyUniqueVisitorId id;
}


