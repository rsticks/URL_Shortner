package faang.school.urlshortenerservice.service.analytics.impl;

import faang.school.urlshortenerservice.model.analytics.ClickEvent;
import faang.school.urlshortenerservice.repository.analytics.ClickEventRepository;
import faang.school.urlshortenerservice.repository.analytics.DailyLinkStatsDimRepository;
import faang.school.urlshortenerservice.repository.analytics.DailyLinkStatsRepository;
import faang.school.urlshortenerservice.repository.analytics.DailyUniqueVisitorRepository;
import faang.school.urlshortenerservice.service.analytics.AnalyticsDimensions;
import faang.school.urlshortenerservice.service.analytics.RequestFingerprint;
import faang.school.urlshortenerservice.service.analytics.RequestFingerprintService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClickAnalyticsServiceImplTest {

    @Mock
    private ClickEventRepository clickEventRepository;
    @Mock
    private DailyUniqueVisitorRepository dailyUniqueVisitorRepository;
    @Mock
    private DailyLinkStatsRepository dailyLinkStatsRepository;
    @Mock
    private DailyLinkStatsDimRepository dailyLinkStatsDimRepository;
    @Mock
    private RequestFingerprintService requestFingerprintService;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ClickAnalyticsServiceImpl service;

    @Captor
    private ArgumentCaptor<ClickEvent> clickEventCaptor;
    @Captor
    private ArgumentCaptor<String> typeCaptor;
    @Captor
    private ArgumentCaptor<String> valueCaptor;

    @Test
    void shouldPersistExtendedClickEventAndDimensions() {
        RequestFingerprint fingerprint = RequestFingerprint.builder()
                .visitorHashDay("hash")
                .referrerHost("google.com")
                .referrerCategory("search")
                .language("en-us")
                .deviceType("desktop")
                .osFamily("macOS")
                .osVersion("14.4")
                .browserFamily("Chrome")
                .browserVersion("123.0.0")
                .country("United States")
                .region("California")
                .city("Mountain View")
                .timezone("America/Los_Angeles")
                .asn("AS15169")
                .provider("Google LLC")
                .networkType("fixed")
                .proxyStatus("false")
                .vpnStatus("false")
                .torStatus("false")
                .clientHintPlatform("macOS")
                .clientHintPlatformVersion("14.4")
                .clientHintMobile("desktop")
                .clientHintModel("unknown")
                .bot(false)
                .build();
        when(requestFingerprintService.fingerprint(any(LocalDate.class), eq(request))).thenReturn(fingerprint);
        when(dailyUniqueVisitorRepository.tryInsert(eq("abc123"), any(LocalDate.class), eq("hash"))).thenReturn(1);

        service.recordRedirect("abc123", 302, request);

        verify(clickEventRepository).save(clickEventCaptor.capture());
        ClickEvent event = clickEventCaptor.getValue();
        assertThat(event.getUrlHash()).isEqualTo("abc123");
        assertThat(event.getReferrerCategory()).isEqualTo("search");
        assertThat(event.getBrowserVersion()).isEqualTo("123.0.0");
        assertThat(event.getCountry()).isEqualTo("United States");
        assertThat(event.getAsn()).isEqualTo("AS15169");
        assertThat(event.getClientHintPlatform()).isEqualTo("macOS");
        assertThat(event.getHourOfDay()).isBetween(0, 23);
        assertThat(event.getDayOfWeek()).isNotBlank();

        verify(dailyLinkStatsRepository).ensureRow(eq("abc123"), any(LocalDate.class));
        verify(dailyLinkStatsRepository).upsertIncrement(eq("abc123"), any(LocalDate.class), eq(1L));
        verify(dailyLinkStatsDimRepository, atLeastOnce()).upsertIncrement(
                eq("abc123"),
                any(LocalDate.class),
                typeCaptor.capture(),
                valueCaptor.capture()
        );

        Map<String, List<String>> dimensions = toMultimap(typeCaptor.getAllValues(), valueCaptor.getAllValues());
        assertThat(dimensions.get(AnalyticsDimensions.COUNTRY)).contains("United States");
        assertThat(dimensions.get(AnalyticsDimensions.CITY)).contains("Mountain View");
        assertThat(dimensions.get(AnalyticsDimensions.ASN)).contains("AS15169");
        assertThat(dimensions.get(AnalyticsDimensions.REFERRER_CATEGORY)).contains("search");
        assertThat(dimensions.get(AnalyticsDimensions.CLIENT_HINT_PLATFORM)).contains("macOS");
        assertThat(dimensions.get(AnalyticsDimensions.PROXY_STATUS)).contains("false");
        assertThat(dimensions).containsKey(AnalyticsDimensions.HOUR_OF_DAY);
        assertThat(dimensions).containsKey(AnalyticsDimensions.DAY_OF_WEEK);
    }

    private Map<String, List<String>> toMultimap(List<String> types, List<String> values) {
        Map<String, List<String>> result = new HashMap<>();
        for (int i = 0; i < types.size(); i++) {
            result.computeIfAbsent(types.get(i), ignored -> new java.util.ArrayList<>()).add(values.get(i));
        }
        return result;
    }
}
