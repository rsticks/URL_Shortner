package faang.school.urlshortenerservice.service.analytics.impl;

import faang.school.urlshortenerservice.model.analytics.ClickEvent;
import faang.school.urlshortenerservice.repository.analytics.ClickEventRepository;
import faang.school.urlshortenerservice.repository.analytics.DailyLinkStatsDimRepository;
import faang.school.urlshortenerservice.repository.analytics.DailyLinkStatsRepository;
import faang.school.urlshortenerservice.repository.analytics.DailyUniqueVisitorRepository;
import faang.school.urlshortenerservice.service.analytics.ClickAnalyticsService;
import faang.school.urlshortenerservice.service.analytics.RequestFingerprint;
import faang.school.urlshortenerservice.service.analytics.RequestFingerprintService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClickAnalyticsServiceImpl implements ClickAnalyticsService {
    private static final String DIM_REFERRER = "referrer_host";
    private static final String DIM_LANGUAGE = "language";
    private static final String DIM_DEVICE = "device_type";
    private static final String DIM_OS = "os_family";
    private static final String DIM_BROWSER = "browser_family";
    private static final String DIM_BOT = "is_bot";

    private final ClickEventRepository clickEventRepository;
    private final DailyUniqueVisitorRepository dailyUniqueVisitorRepository;
    private final DailyLinkStatsRepository dailyLinkStatsRepository;
    private final DailyLinkStatsDimRepository dailyLinkStatsDimRepository;
    private final RequestFingerprintService requestFingerprintService;

    @Override
    @Transactional
    public void recordRedirect(String urlHash, int httpStatus, HttpServletRequest request) {
        LocalDate day = OffsetDateTime.now(ZoneOffset.UTC).toLocalDate();
        RequestFingerprint fp = requestFingerprintService.fingerprint(day, request);

        // 1) Raw event (short retention)
        ClickEvent ev = new ClickEvent();
        ev.setUrlHash(urlHash);
        ev.setClickedAt(OffsetDateTime.now(ZoneOffset.UTC));
        ev.setVisitorHashDay(fp.visitorHashDay());
        ev.setReferrerHost(fp.referrerHost());
        ev.setLanguage(fp.language());
        ev.setDeviceType(fp.deviceType());
        ev.setOsFamily(fp.osFamily());
        ev.setBrowserFamily(fp.browserFamily());
        ev.setBot(fp.bot());
        ev.setHttpStatus(httpStatus);
        ev.setResult("redirect");
        clickEventRepository.save(ev);

        // 2) Unique per day (atomic)
        int inserted = dailyUniqueVisitorRepository.tryInsert(urlHash, day, fp.visitorHashDay());
        long uniqueInc = inserted > 0 ? 1L : 0L;

        // 3) Daily aggregates (atomic)
        dailyLinkStatsRepository.upsertIncrement(urlHash, day, uniqueInc);

        // 4) Breakdown increments (clicks only)
        dailyLinkStatsDimRepository.upsertIncrement(urlHash, day, DIM_REFERRER, safeDim(fp.referrerHost()));
        dailyLinkStatsDimRepository.upsertIncrement(urlHash, day, DIM_LANGUAGE, safeDim(fp.language()));
        dailyLinkStatsDimRepository.upsertIncrement(urlHash, day, DIM_DEVICE, safeDim(fp.deviceType()));
        dailyLinkStatsDimRepository.upsertIncrement(urlHash, day, DIM_OS, safeDim(fp.osFamily()));
        dailyLinkStatsDimRepository.upsertIncrement(urlHash, day, DIM_BROWSER, safeDim(fp.browserFamily()));
        dailyLinkStatsDimRepository.upsertIncrement(urlHash, day, DIM_BOT, fp.bot() ? "true" : "false");
    }

    private static String safeDim(String v) {
        if (v == null || v.isBlank()) return "unknown";
        String s = v.trim();
        return s.length() > 255 ? s.substring(0, 255) : s;
    }
}


