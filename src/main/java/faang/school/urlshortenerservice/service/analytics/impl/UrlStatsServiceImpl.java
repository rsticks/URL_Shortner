package faang.school.urlshortenerservice.service.analytics.impl;

import faang.school.urlshortenerservice.dto.analytics.DailyStatDto;
import faang.school.urlshortenerservice.dto.analytics.DimStatDto;
import faang.school.urlshortenerservice.dto.analytics.UrlStatsResponse;
import faang.school.urlshortenerservice.model.AppUser;
import faang.school.urlshortenerservice.model.UserUrl;
import faang.school.urlshortenerservice.repository.UserUrlRepository;
import faang.school.urlshortenerservice.repository.analytics.DailyLinkStatsDimRepository;
import faang.school.urlshortenerservice.repository.analytics.DailyLinkStatsRepository;
import faang.school.urlshortenerservice.repository.analytics.DimStatView;
import faang.school.urlshortenerservice.service.analytics.AnalyticsDimensions;
import faang.school.urlshortenerservice.service.analytics.UrlStatsService;
import faang.school.urlshortenerservice.service.user.AppUserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UrlStatsServiceImpl implements UrlStatsService {
    private final AppUserService appUserService;
    private final UserUrlRepository userUrlRepository;
    private final DailyLinkStatsRepository dailyLinkStatsRepository;
    private final DailyLinkStatsDimRepository dailyLinkStatsDimRepository;

    @Override
    public UrlStatsResponse getStatsForOwner(String urlHash, LocalDate from, LocalDate to, int topLimit) {
        // Paid feature: only users with active subscription can access stats
        appUserService.ensureSubscribed();
        ensureOwner(urlHash);
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from must be <= to");
        }
        int limit = Math.max(1, Math.min(topLimit, 50));

        List<DailyStatDto> daily = dailyLinkStatsRepository
                .findAllByIdUrlHashAndIdDayBetweenOrderByIdDay(urlHash, from, to)
                .stream()
                .map(s -> new DailyStatDto(s.getId().getDay(), s.getClicks(), s.getUniqueVisitors()))
                .toList();

        List<DimStatDto> topReferrers = top(urlHash, from, to, AnalyticsDimensions.REFERRER, limit);
        List<DimStatDto> referrerCategories = top(urlHash, from, to, AnalyticsDimensions.REFERRER_CATEGORY, limit);
        List<DimStatDto> topLanguages = top(urlHash, from, to, AnalyticsDimensions.LANGUAGE, limit);
        List<DimStatDto> deviceTypes = top(urlHash, from, to, AnalyticsDimensions.DEVICE, limit);
        List<DimStatDto> osFamilies = top(urlHash, from, to, AnalyticsDimensions.OS, limit);
        List<DimStatDto> osVersions = top(urlHash, from, to, AnalyticsDimensions.OS_VERSION, limit);
        List<DimStatDto> browserFamilies = top(urlHash, from, to, AnalyticsDimensions.BROWSER, limit);
        List<DimStatDto> browserVersions = top(urlHash, from, to, AnalyticsDimensions.BROWSER_VERSION, limit);
        List<DimStatDto> countries = top(urlHash, from, to, AnalyticsDimensions.COUNTRY, limit);
        List<DimStatDto> regions = top(urlHash, from, to, AnalyticsDimensions.REGION, limit);
        List<DimStatDto> cities = top(urlHash, from, to, AnalyticsDimensions.CITY, limit);
        List<DimStatDto> timezones = top(urlHash, from, to, AnalyticsDimensions.TIMEZONE, limit);
        List<DimStatDto> asns = top(urlHash, from, to, AnalyticsDimensions.ASN, limit);
        List<DimStatDto> providers = top(urlHash, from, to, AnalyticsDimensions.PROVIDER, limit);
        List<DimStatDto> networkTypes = top(urlHash, from, to, AnalyticsDimensions.NETWORK_TYPE, limit);
        List<DimStatDto> proxyStatuses = top(urlHash, from, to, AnalyticsDimensions.PROXY_STATUS, limit);
        List<DimStatDto> vpnStatuses = top(urlHash, from, to, AnalyticsDimensions.VPN_STATUS, limit);
        List<DimStatDto> torStatuses = top(urlHash, from, to, AnalyticsDimensions.TOR_STATUS, limit);
        List<DimStatDto> hoursOfDay = top(urlHash, from, to, AnalyticsDimensions.HOUR_OF_DAY, limit);
        List<DimStatDto> daysOfWeek = top(urlHash, from, to, AnalyticsDimensions.DAY_OF_WEEK, limit);
        List<DimStatDto> clientHintPlatforms = top(urlHash, from, to, AnalyticsDimensions.CLIENT_HINT_PLATFORM, limit);
        List<DimStatDto> clientHintPlatformVersions = top(urlHash, from, to, AnalyticsDimensions.CLIENT_HINT_PLATFORM_VERSION, limit);
        List<DimStatDto> clientHintMobiles = top(urlHash, from, to, AnalyticsDimensions.CLIENT_HINT_MOBILE, limit);
        List<DimStatDto> clientHintModels = top(urlHash, from, to, AnalyticsDimensions.CLIENT_HINT_MODEL, limit);
        return new UrlStatsResponse(
                urlHash,
                from,
                to,
                daily,
                topReferrers,
                referrerCategories,
                topLanguages,
                deviceTypes,
                osFamilies,
                osVersions,
                browserFamilies,
                browserVersions,
                countries,
                regions,
                cities,
                timezones,
                asns,
                providers,
                networkTypes,
                proxyStatuses,
                vpnStatuses,
                torStatuses,
                hoursOfDay,
                daysOfWeek,
                clientHintPlatforms,
                clientHintPlatformVersions,
                clientHintMobiles,
                clientHintModels
        );
    }

    private void ensureOwner(String urlHash) {
        String safeHash = Objects.requireNonNull(urlHash, "urlHash");
        AppUser user = appUserService.getCurrentUser();
        UserUrl owner = userUrlRepository.findById(safeHash)
                .orElseThrow(() -> new EntityNotFoundException("Stats not available for url: " + safeHash));
        if (!owner.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not an owner of url: " + safeHash);
        }
    }

    private static List<DimStatDto> map(List<DimStatView> views) {
        return views.stream().map(v -> new DimStatDto(v.getValue(), v.getClicks())).toList();
    }

    private List<DimStatDto> top(String urlHash, LocalDate from, LocalDate to, String type, int limit) {
        return map(dailyLinkStatsDimRepository.findTop(urlHash, from, to, type, limit));
    }
}


