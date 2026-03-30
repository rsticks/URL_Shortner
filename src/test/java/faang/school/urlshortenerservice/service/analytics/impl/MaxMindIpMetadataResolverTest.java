package faang.school.urlshortenerservice.service.analytics.impl;

import faang.school.urlshortenerservice.properties.AnalyticsProperties;
import faang.school.urlshortenerservice.service.analytics.IpMetadata;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaxMindIpMetadataResolverTest {

    @Test
    void shouldReturnUnknownWhenGeoIpDatabasesAreNotConfigured() {
        AnalyticsProperties properties = new AnalyticsProperties();
        MaxMindIpMetadataResolver resolver = new MaxMindIpMetadataResolver(properties);
        resolver.init();

        IpMetadata metadata = resolver.resolve("8.8.8.8");

        assertThat(metadata).isEqualTo(IpMetadata.unknown());
    }

    @Test
    void shouldSkipLocalAddresses() {
        AnalyticsProperties properties = new AnalyticsProperties();
        MaxMindIpMetadataResolver resolver = new MaxMindIpMetadataResolver(properties);
        resolver.init();

        IpMetadata metadata = resolver.resolve("127.0.0.1");

        assertThat(metadata.country()).isEqualTo("unknown");
        assertThat(metadata.proxyStatus()).isEqualTo("unknown");
        assertThat(metadata.networkType()).isEqualTo("unknown");
    }
}
