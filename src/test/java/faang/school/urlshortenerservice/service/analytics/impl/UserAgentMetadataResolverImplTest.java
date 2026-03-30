package faang.school.urlshortenerservice.service.analytics.impl;

import faang.school.urlshortenerservice.service.analytics.UserAgentMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class UserAgentMetadataResolverImplTest {

    private final UserAgentMetadataResolverImpl resolver = new UserAgentMetadataResolverImpl();

    @Test
    void shouldUseClientHintsWhenAvailable() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Sec-CH-UA-Platform", "\"Android\"");
        request.addHeader("Sec-CH-UA-Platform-Version", "\"14.0.0\"");
        request.addHeader("Sec-CH-UA-Mobile", "?1");
        request.addHeader("Sec-CH-UA-Model", "\"Pixel 8\"");

        UserAgentMetadata metadata = resolver.resolve(request);

        assertThat(metadata.deviceType()).isEqualTo("mobile");
        assertThat(metadata.osFamily()).isEqualTo("Android");
        assertThat(metadata.osVersion()).isEqualTo("14.0.0");
        assertThat(metadata.clientHintPlatform()).isEqualTo("Android");
        assertThat(metadata.clientHintMobile()).isEqualTo("mobile");
        assertThat(metadata.clientHintModel()).isEqualTo("Pixel 8");
    }

    @Test
    void shouldParseBrowserAndOsFromUserAgent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36"
        );

        UserAgentMetadata metadata = resolver.resolve(request);

        assertThat(metadata.deviceType()).isEqualTo("desktop");
        assertThat(metadata.browserFamily()).containsIgnoringCase("Chrome");
        assertThat(metadata.browserVersion()).startsWith("123");
        assertThat(metadata.osFamily()).containsIgnoringCase("Mac");
    }
}
