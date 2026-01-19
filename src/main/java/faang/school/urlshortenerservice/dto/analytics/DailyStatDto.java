package faang.school.urlshortenerservice.dto.analytics;

import java.time.LocalDate;

public record DailyStatDto(
        LocalDate day,
        long clicks,
        long uniqueVisitors
) {
}


