package faang.school.urlshortenerservice.service.analytics.impl;

import faang.school.urlshortenerservice.dto.analytics.DailyStatDto;
import faang.school.urlshortenerservice.dto.analytics.DimStatDto;
import faang.school.urlshortenerservice.dto.analytics.UrlUtmDto;
import faang.school.urlshortenerservice.dto.analytics.UrlStatsResponse;
import faang.school.urlshortenerservice.model.AppUser;
import faang.school.urlshortenerservice.model.UserUrl;
import faang.school.urlshortenerservice.model.UrlUtm;
import faang.school.urlshortenerservice.repository.UserUrlRepository;
import faang.school.urlshortenerservice.repository.UrlUtmRepository;
import faang.school.urlshortenerservice.repository.analytics.DailyLinkStatsDimRepository;
import faang.school.urlshortenerservice.repository.analytics.DailyLinkStatsRepository;
import faang.school.urlshortenerservice.repository.analytics.DimStatView;
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
    private static final String DIM_REFERRER = "referrer_host";
    private static final String DIM_LANGUAGE = "language";
    private static final String DIM_DEVICE = "device_type";
    private static final String DIM_OS = "os_family";
    private static final String DIM_BROWSER = "browser_family";

    private final AppUserService appUserService;
    private final UserUrlRepository userUrlRepository;
    private final UrlUtmRepository urlUtmRepository;
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

        List<DimStatDto> topReferrers = map(dailyLinkStatsDimRepository.findTop(urlHash, from, to, DIM_REFERRER, limit));
        List<DimStatDto> topLanguages = map(dailyLinkStatsDimRepository.findTop(urlHash, from, to, DIM_LANGUAGE, limit));
        List<DimStatDto> deviceTypes = map(dailyLinkStatsDimRepository.findTop(urlHash, from, to, DIM_DEVICE, limit));
        List<DimStatDto> osFamilies = map(dailyLinkStatsDimRepository.findTop(urlHash, from, to, DIM_OS, limit));
        List<DimStatDto> browserFamilies = map(dailyLinkStatsDimRepository.findTop(urlHash, from, to, DIM_BROWSER, limit));

        UrlUtmDto utm = urlUtmRepository.findById(Objects.requireNonNull(urlHash, "urlHash"))
                .map(UrlStatsServiceImpl::toDto)
                .orElse(null);

        return new UrlStatsResponse(urlHash, from, to, utm, daily, topReferrers, topLanguages, deviceTypes, osFamilies, browserFamilies);
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

    private static UrlUtmDto toDto(UrlUtm m) {
        return new UrlUtmDto(m.getUtmSource(), m.getUtmMedium(), m.getUtmCampaign(), m.getUtmContent(), m.getUtmTerm());
    }
}


