package faang.school.urlshortenerservice.service.analytics.impl;

import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.AnonymousIpResponse;
import com.maxmind.geoip2.model.AsnResponse;
import com.maxmind.geoip2.model.CityResponse;
import faang.school.urlshortenerservice.properties.AnalyticsProperties;
import faang.school.urlshortenerservice.service.analytics.IpMetadata;
import faang.school.urlshortenerservice.service.analytics.IpMetadataResolver;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaxMindIpMetadataResolver implements IpMetadataResolver {
    private static final List<String> MOBILE_PROVIDER_MARKERS = List.of(
            "mobile", "wireless", "cellular", "telecom", "lte", "5g", "4g"
    );
    private static final List<String> HOSTING_PROVIDER_MARKERS = List.of(
            "amazon", "aws", "google cloud", "digitalocean", "microsoft", "azure", "ovh", "cloudflare", "hetzner", "linode", "vultr"
    );

    private final AnalyticsProperties properties;

    private DatabaseReader cityReader;
    private DatabaseReader asnReader;
    private DatabaseReader anonymousReader;

    @PostConstruct
    void init() {
        cityReader = open(properties.getGeoIp().getCityDbPath(), "city");
        asnReader = open(properties.getGeoIp().getAsnDbPath(), "asn");
        anonymousReader = open(properties.getGeoIp().getAnonymousDbPath(), "anonymous");
    }

    @PreDestroy
    void close() {
        closeQuietly(cityReader);
        closeQuietly(asnReader);
        closeQuietly(anonymousReader);
    }

    @Override
    public IpMetadata resolve(String ip) {
        InetAddress address = parse(ip);
        if (address == null || isLocal(address)) {
            return IpMetadata.unknown();
        }

        String country = "unknown";
        String region = "unknown";
        String city = "unknown";
        String timezone = "unknown";
        String asn = "unknown";
        String provider = "unknown";
        String networkType = "unknown";
        String proxyStatus = "unknown";
        String vpnStatus = "unknown";
        String torStatus = "unknown";
        boolean hostingProvider = false;

        if (cityReader != null) {
            try {
                CityResponse cityResponse = cityReader.city(address);
                country = preferred(cityResponse.getCountry().getName(), cityResponse.getRegisteredCountry().getName());
                region = preferred(cityResponse.getMostSpecificSubdivision().getName(), null);
                city = preferred(cityResponse.getCity().getName(), null);
                timezone = preferred(cityResponse.getLocation().getTimeZone(), null);
            } catch (Exception e) {
                log.debug("GeoIP city lookup failed for {}", ip, e);
            }
        }

        if (asnReader != null) {
            try {
                AsnResponse asnResponse = asnReader.asn(address);
                Long asnNumber = asnResponse.getAutonomousSystemNumber();
                if (asnNumber != null) {
                    asn = "AS" + asnNumber;
                }
                provider = normalize(asnResponse.getAutonomousSystemOrganization(), 255);
            } catch (Exception e) {
                log.debug("GeoIP ASN lookup failed for {}", ip, e);
            }
        }

        if (anonymousReader != null) {
            try {
                AnonymousIpResponse anonymousIpResponse = anonymousReader.anonymousIp(address);
                hostingProvider = anonymousIpResponse.isHostingProvider();
                proxyStatus = boolStatus(anonymousIpResponse.isAnonymous() || anonymousIpResponse.isPublicProxy() || hostingProvider);
                vpnStatus = boolStatus(anonymousIpResponse.isAnonymousVpn());
                torStatus = boolStatus(anonymousIpResponse.isTorExitNode());
            } catch (Exception e) {
                log.debug("GeoIP anonymous lookup failed for {}", ip, e);
            }
        }

        networkType = inferNetworkType(provider, proxyStatus, hostingProvider);

        return IpMetadata.builder()
                .country(normalize(country, 128))
                .region(normalize(region, 128))
                .city(normalize(city, 128))
                .timezone(normalize(timezone, 64))
                .asn(normalize(asn, 32))
                .provider(normalize(provider, 255))
                .networkType(normalize(networkType, 32))
                .proxyStatus(normalize(proxyStatus, 16))
                .vpnStatus(normalize(vpnStatus, 16))
                .torStatus(normalize(torStatus, 16))
                .build();
    }

    private static DatabaseReader open(String pathValue, String label) {
        if (pathValue == null || pathValue.isBlank()) {
            return null;
        }
        Path path = Path.of(pathValue.trim());
        if (!Files.exists(path)) {
            log.warn("Analytics GeoIP {} DB not found: {}", label, path);
            return null;
        }
        try {
            return new DatabaseReader.Builder(path.toFile())
                    .withCache(new CHMCache())
                    .build();
        } catch (IOException e) {
            log.warn("Failed to open analytics GeoIP {} DB: {}", label, path, e);
            return null;
        }
    }

    private static void closeQuietly(DatabaseReader reader) {
        if (reader == null) {
            return;
        }
        try {
            reader.close();
        } catch (IOException e) {
            log.debug("Failed to close GeoIP reader", e);
        }
    }

    private static InetAddress parse(String ip) {
        if (ip == null || ip.isBlank()) {
            return null;
        }
        try {
            return InetAddress.getByName(ip.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isLocal(InetAddress address) {
        return address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress();
    }

    private static String preferred(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback == null || fallback.isBlank() ? "unknown" : fallback;
    }

    private static String normalize(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        String trimmed = value.trim();
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) : trimmed;
    }

    private static String boolStatus(boolean value) {
        return value ? "true" : "false";
    }

    private static String inferNetworkType(String provider, String proxyStatus, boolean hostingProvider) {
        if ("true".equals(proxyStatus)) {
            return hostingProvider ? "hosting" : "proxy";
        }
        String normalizedProvider = provider == null ? "" : provider.toLowerCase(Locale.ROOT);
        if (containsAny(normalizedProvider, HOSTING_PROVIDER_MARKERS)) {
            return "hosting";
        }
        if (containsAny(normalizedProvider, MOBILE_PROVIDER_MARKERS)) {
            return "mobile";
        }
        return normalizedProvider.isBlank() || "unknown".equals(normalizedProvider) ? "unknown" : "fixed";
    }

    private static boolean containsAny(String source, List<String> markers) {
        for (String marker : markers) {
            if (source.contains(marker)) {
                return true;
            }
        }
        return false;
    }
}
