package faang.school.urlshortenerservice.model.analytics;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class DailyUniqueVisitorId implements Serializable {
    @Column(name = "url_hash", nullable = false, length = 6)
    private String urlHash;

    @Column(name = "day", nullable = false)
    private LocalDate day;

    @Column(name = "visitor_hash_day", nullable = false, length = 64)
    private String visitorHashDay;
}


