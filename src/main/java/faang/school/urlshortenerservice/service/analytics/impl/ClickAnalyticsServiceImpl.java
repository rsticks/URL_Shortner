package faang.school.urlshortenerservice.service.analytics.impl;

import faang.school.urlshortenerservice.model.analytics.ClickEvent;
import faang.school.urlshortenerservice.repository.analytics.ClickEventRepository;
import faang.school.urlshortenerservice.repository.analytics.DailyLinkStatsDimRepository;
import faang.school.urlshortenerservice.repository.analytics.DailyLinkStatsRepository;
import faang.school.urlshortenerservice.repository.analytics.DailyUniqueVisitorRepository;
import faang.school.urlshortenerservice.service.analytics.AnalyticsDimensions;
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
    private final ClickEventRepository clickEventRepository;
    private final DailyUniqueVisitorRepository dailyUniqueVisitorRepository;
    private final DailyLinkStatsRepository dailyLinkStatsRepository;
    private final DailyLinkStatsDimRepository dailyLinkStatsDimRepository;
    private final RequestFingerprintService requestFingerprintService;

    @Override
    @Transactional
    public void recordRedirect(String urlHash, int httpStatus, HttpServletRequest request) {
        OffsetDateTime clickedAt = OffsetDateTime.now(ZoneOffset.UTC);
        LocalDate day = clickedAt.toLocalDate();
        RequestFingerprint fp = requestFingerprintService.fingerprint(day, request);

        // 1) Raw event (short retention)
        ClickEvent ev = new ClickEvent();
        ev.setUrlHash(urlHash);
        ev.setClickedAt(clickedAt);
        ev.setVisitorHashDay(fp.visitorHashDay());
        ev.setReferrerHost(fp.referrerHost());
        ev.setReferrerCategory(fp.referrerCategory());
        ev.setLanguage(fp.language());
        ev.setDeviceType(fp.deviceType());
        ev.setOsFamily(fp.osFamily());
        ev.setOsVersion(fp.osVersion());
        ev.setBrowserFamily(fp.browserFamily());
        ev.setBrowserVersion(fp.browserVersion());
        ev.setCountry(fp.country());
        ev.setRegion(fp.region());
        ev.setCity(fp.city());
        ev.setTimezone(fp.timezone());
        ev.setAsn(fp.asn());
        ev.setProvider(fp.provider());
        ev.setNetworkType(fp.networkType());
        ev.setProxyStatus(fp.proxyStatus());
        ev.setVpnStatus(fp.vpnStatus());
        ev.setTorStatus(fp.torStatus());
        ev.setClientHintPlatform(fp.clientHintPlatform());
        ev.setClientHintPlatformVersion(fp.clientHintPlatformVersion());
        ev.setClientHintMobile(fp.clientHintMobile());
        ev.setClientHintModel(fp.clientHintModel());
        ev.setHourOfDay(clickedAt.getHour());
        ev.setDayOfWeek(clickedAt.getDayOfWeek().name());
        ev.setBot(fp.bot());
        ev.setHttpStatus(httpStatus);
        ev.setResult("redirect");
        clickEventRepository.save(ev);

        // Ensure parent row exists for FK constraints (daily_unique_visitor, daily_link_stats_dim)
        dailyLinkStatsRepository.ensureRow(urlHash, day);

        // 2) Unique per day (atomic)
        int inserted = dailyUniqueVisitorRepository.tryInsert(urlHash, day, fp.visitorHashDay());
        long uniqueInc = inserted > 0 ? 1L : 0L;

        // 3) Daily aggregates (atomic)
        dailyLinkStatsRepository.upsertIncrement(urlHash, day, uniqueInc);

        // 4) Breakdown increments (clicks only)
        upsertDim(urlHash, day, AnalyticsDimensions.REFERRER, fp.referrerHost());
        upsertDim(urlHash, day, AnalyticsDimensions.REFERRER_CATEGORY, fp.referrerCategory());
        upsertDim(urlHash, day, AnalyticsDimensions.LANGUAGE, fp.language());
        upsertDim(urlHash, day, AnalyticsDimensions.DEVICE, fp.deviceType());
        upsertDim(urlHash, day, AnalyticsDimensions.OS, fp.osFamily());
        upsertDim(urlHash, day, AnalyticsDimensions.OS_VERSION, fp.osVersion());
        upsertDim(urlHash, day, AnalyticsDimensions.BROWSER, fp.browserFamily());
        upsertDim(urlHash, day, AnalyticsDimensions.BROWSER_VERSION, fp.browserVersion());
        upsertDim(urlHash, day, AnalyticsDimensions.COUNTRY, fp.country());
        upsertDim(urlHash, day, AnalyticsDimensions.REGION, fp.region());
        upsertDim(urlHash, day, AnalyticsDimensions.CITY, fp.city());
        upsertDim(urlHash, day, AnalyticsDimensions.TIMEZONE, fp.timezone());
        upsertDim(urlHash, day, AnalyticsDimensions.ASN, fp.asn());
        upsertDim(urlHash, day, AnalyticsDimensions.PROVIDER, fp.provider());
        upsertDim(urlHash, day, AnalyticsDimensions.NETWORK_TYPE, fp.networkType());
        upsertDim(urlHash, day, AnalyticsDimensions.PROXY_STATUS, fp.proxyStatus());
        upsertDim(urlHash, day, AnalyticsDimensions.VPN_STATUS, fp.vpnStatus());
        upsertDim(urlHash, day, AnalyticsDimensions.TOR_STATUS, fp.torStatus());
        upsertDim(urlHash, day, AnalyticsDimensions.HOUR_OF_DAY, String.format("%02d", clickedAt.getHour()));
        upsertDim(urlHash, day, AnalyticsDimensions.DAY_OF_WEEK, clickedAt.getDayOfWeek().name());
        upsertDim(urlHash, day, AnalyticsDimensions.CLIENT_HINT_PLATFORM, fp.clientHintPlatform());
        upsertDim(urlHash, day, AnalyticsDimensions.CLIENT_HINT_PLATFORM_VERSION, fp.clientHintPlatformVersion());
        upsertDim(urlHash, day, AnalyticsDimensions.CLIENT_HINT_MOBILE, fp.clientHintMobile());
        upsertDim(urlHash, day, AnalyticsDimensions.CLIENT_HINT_MODEL, fp.clientHintModel());
        upsertDim(urlHash, day, AnalyticsDimensions.BOT, fp.bot() ? "true" : "false");
    }

    private void upsertDim(String urlHash, LocalDate day, String type, String value) {
        dailyLinkStatsDimRepository.upsertIncrement(urlHash, day, type, safeDim(value));
    }

    private static String safeDim(String v) {
        if (v == null || v.isBlank()) return "unknown";
        String s = v.trim();
        return s.length() > 255 ? s.substring(0, 255) : s;
    }
}


