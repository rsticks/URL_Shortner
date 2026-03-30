package faang.school.urlshortenerservice.service.analytics.impl;

import faang.school.urlshortenerservice.properties.AnalyticsProperties;
import faang.school.urlshortenerservice.service.analytics.IpMetadata;
import faang.school.urlshortenerservice.service.analytics.IpMetadataResolver;
import faang.school.urlshortenerservice.service.analytics.RequestFingerprint;
import faang.school.urlshortenerservice.service.analytics.UserAgentMetadata;
import faang.school.urlshortenerservice.service.analytics.UserAgentMetadataResolver;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestFingerprintServiceImplTest {

    @Test
    void shouldBuildExtendedFingerprint() {
        AnalyticsProperties properties = new AnalyticsProperties();
        properties.setVisitorHmacSecret("test-secret");

        IpMetadataResolver ipMetadataResolver = mock(IpMetadataResolver.class);
        when(ipMetadataResolver.resolve("8.8.8.8")).thenReturn(IpMetadata.builder()
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
                .build());

        UserAgentMetadataResolver userAgentMetadataResolver = mock(UserAgentMetadataResolver.class);
        when(userAgentMetadataResolver.resolve(org.mockito.ArgumentMatchers.any())).thenReturn(UserAgentMetadata.builder()
                .deviceType("desktop")
                .browserFamily("Chrome")
                .browserVersion("123.0.0")
                .osFamily("macOS")
                .osVersion("14.4")
                .clientHintPlatform("macOS")
                .clientHintPlatformVersion("14.4")
                .clientHintMobile("desktop")
                .clientHintModel("unknown")
                .build());

        RequestFingerprintServiceImpl service = new RequestFingerprintServiceImpl(
                properties,
                ipMetadataResolver,
                userAgentMetadataResolver
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.5");
        request.setServerName("sho.rt");
        request.addHeader("X-Forwarded-For", "8.8.8.8, 10.0.0.5");
        request.addHeader("User-Agent", "Mozilla/5.0 Chrome/123.0");
        request.addHeader("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8");
        request.addHeader("Referer", "https://www.google.com/search?q=url");

        RequestFingerprint fingerprint = service.fingerprint(LocalDate.of(2026, 3, 25), request);

        assertThat(fingerprint.visitorHashDay()).hasSize(64);
        assertThat(fingerprint.referrerHost()).isEqualTo("www.google.com");
        assertThat(fingerprint.referrerCategory()).isEqualTo("search");
        assertThat(fingerprint.language()).isEqualTo("ru-ru");
        assertThat(fingerprint.browserFamily()).isEqualTo("Chrome");
        assertThat(fingerprint.browserVersion()).isEqualTo("123.0.0");
        assertThat(fingerprint.osVersion()).isEqualTo("14.4");
        assertThat(fingerprint.country()).isEqualTo("United States");
        assertThat(fingerprint.city()).isEqualTo("Mountain View");
        assertThat(fingerprint.asn()).isEqualTo("AS15169");
        assertThat(fingerprint.provider()).isEqualTo("Google LLC");
        assertThat(fingerprint.clientHintPlatform()).isEqualTo("macOS");
        assertThat(fingerprint.clientHintMobile()).isEqualTo("desktop");
        assertThat(fingerprint.bot()).isFalse();
    }
}
