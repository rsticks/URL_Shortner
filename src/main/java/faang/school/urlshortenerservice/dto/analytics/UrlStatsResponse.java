package faang.school.urlshortenerservice.dto.analytics;

import java.time.LocalDate;
import java.util.List;

public record UrlStatsResponse(
        String urlHash,
        LocalDate from,
        LocalDate to,
        List<DailyStatDto> daily,
        List<DimStatDto> topReferrers,
        List<DimStatDto> referrerCategories,
        List<DimStatDto> topLanguages,
        List<DimStatDto> deviceTypes,
        List<DimStatDto> osFamilies,
        List<DimStatDto> osVersions,
        List<DimStatDto> browserFamilies,
        List<DimStatDto> browserVersions,
        List<DimStatDto> countries,
        List<DimStatDto> regions,
        List<DimStatDto> cities,
        List<DimStatDto> timezones,
        List<DimStatDto> asns,
        List<DimStatDto> providers,
        List<DimStatDto> networkTypes,
        List<DimStatDto> proxyStatuses,
        List<DimStatDto> vpnStatuses,
        List<DimStatDto> torStatuses,
        List<DimStatDto> hoursOfDay,
        List<DimStatDto> daysOfWeek,
        List<DimStatDto> clientHintPlatforms,
        List<DimStatDto> clientHintPlatformVersions,
        List<DimStatDto> clientHintMobiles,
        List<DimStatDto> clientHintModels
) {
}


