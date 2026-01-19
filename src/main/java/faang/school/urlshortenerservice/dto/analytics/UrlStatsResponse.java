package faang.school.urlshortenerservice.dto.analytics;

import java.time.LocalDate;
import java.util.List;

public record UrlStatsResponse(
        String urlHash,
        LocalDate from,
        LocalDate to,
        UrlUtmDto utm,
        List<DailyStatDto> daily,
        List<DimStatDto> topReferrers,
        List<DimStatDto> topLanguages,
        List<DimStatDto> deviceTypes,
        List<DimStatDto> osFamilies,
        List<DimStatDto> browserFamilies
) {
}


