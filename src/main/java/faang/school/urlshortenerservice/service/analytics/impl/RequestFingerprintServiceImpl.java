package faang.school.urlshortenerservice.service.analytics.impl;

import faang.school.urlshortenerservice.properties.AnalyticsProperties;
import faang.school.urlshortenerservice.service.analytics.IpMetadata;
import faang.school.urlshortenerservice.service.analytics.IpMetadataResolver;
import faang.school.urlshortenerservice.service.analytics.RequestFingerprint;
import faang.school.urlshortenerservice.service.analytics.RequestFingerprintService;
import faang.school.urlshortenerservice.service.analytics.UserAgentMetadata;
import faang.school.urlshortenerservice.service.analytics.UserAgentMetadataResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RequestFingerprintServiceImpl implements RequestFingerprintService {
    private static final String HMAC_SHA_256 = "HmacSHA256";

    private final AnalyticsProperties properties;
    private final IpMetadataResolver ipMetadataResolver;
    private final UserAgentMetadataResolver userAgentMetadataResolver;

    @Override
    public RequestFingerprint fingerprint(LocalDate dayUtc, HttpServletRequest request) {
        String userAgent = header(request, "User-Agent");
        String ip = clientIp(request);
        IpMetadata ipMetadata = ipMetadataResolver.resolve(ip);
        UserAgentMetadata uaMetadata = userAgentMetadataResolver.resolve(request);

        boolean bot = isBot(userAgent);
        String referrerHost = referrerHost(request);
        String referrerCategory = referrerCategory(referrerHost, request.getServerName());
        String language = language(request);

        String visitorHashDay = hmacHex(properties.getVisitorHmacSecret(),
                dayUtc + "|" + normalizeIp(ip) + "|" + normalizeUserAgent(userAgent));

        return RequestFingerprint.builder()
                .visitorHashDay(visitorHashDay)
                .referrerHost(referrerHost)
                .referrerCategory(referrerCategory)
                .language(language)
                .deviceType(bot ? "bot" : uaMetadata.deviceType())
                .osFamily(uaMetadata.osFamily())
                .osVersion(uaMetadata.osVersion())
                .browserFamily(uaMetadata.browserFamily())
                .browserVersion(uaMetadata.browserVersion())
                .country(ipMetadata.country())
                .region(ipMetadata.region())
                .city(ipMetadata.city())
                .timezone(ipMetadata.timezone())
                .asn(ipMetadata.asn())
                .provider(ipMetadata.provider())
                .networkType(ipMetadata.networkType())
                .proxyStatus(ipMetadata.proxyStatus())
                .vpnStatus(ipMetadata.vpnStatus())
                .torStatus(ipMetadata.torStatus())
                .clientHintPlatform(uaMetadata.clientHintPlatform())
                .clientHintPlatformVersion(uaMetadata.clientHintPlatformVersion())
                .clientHintMobile(uaMetadata.clientHintMobile())
                .clientHintModel(uaMetadata.clientHintModel())
                .bot(bot)
                .build();
    }

    private static String header(HttpServletRequest request, String name) {
        String v = request.getHeader(name);
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    private static String referrerHost(HttpServletRequest request) {
        String ref = header(request, "Referer");
        if (ref == null) {
            return "direct";
        }
        try {
            URI uri = URI.create(ref);
            String host = uri.getHost();
            return (host == null || host.isBlank()) ? "direct" : host.toLowerCase(Locale.ROOT);
        } catch (Exception ignored) {
            return "direct";
        }
    }

    private String referrerCategory(String referrerHost, String serverName) {
        if (referrerHost == null || referrerHost.isBlank() || "direct".equals(referrerHost)) {
            return "direct";
        }
        String normalizedHost = referrerHost.toLowerCase(Locale.ROOT);
        String normalizedServer = normalizeHost(serverName);
        if (normalizedServer != null && (normalizedHost.equals(normalizedServer) || normalizedHost.endsWith("." + normalizedServer))) {
            return "internal";
        }
        if (matchesAny(normalizedHost, properties.getReferrer().getSearchHosts())) {
            return "search";
        }
        if (matchesAny(normalizedHost, properties.getReferrer().getSocialHosts())) {
            return "social";
        }
        return "external";
    }

    private static String language(HttpServletRequest request) {
        String al = header(request, "Accept-Language");
        if (al == null) {
            return "unknown";
        }
        // e.g. "ru-RU,ru;q=0.9,en-US;q=0.8"
        String first = al.split(",")[0].trim();
        String tag = first.split(";")[0].trim().toLowerCase(Locale.ROOT);
        if (tag.length() > 16) {
            tag = tag.substring(0, 16);
        }
        return tag.isBlank() ? "unknown" : tag;
    }

    private static boolean isBot(String ua) {
        if (ua == null) return false;
        String s = ua.toLowerCase(Locale.ROOT);
        return s.contains("bot") || s.contains("spider") || s.contains("crawler") || s.contains("slurp");
    }

    private static String clientIp(HttpServletRequest request) {
        String xff = header(request, "X-Forwarded-For");
        if (xff != null) {
            // first IP in list
            String first = xff.split(",")[0].trim();
            if (!first.isBlank()) return first;
        }
        String realIp = header(request, "X-Real-IP");
        if (realIp != null) return realIp;
        return request.getRemoteAddr();
    }

    private static String normalizeIp(String ip) {
        return ip == null ? "" : ip.trim();
    }

    private static String normalizeUserAgent(String ua) {
        if (ua == null) return "";
        String v = ua.trim();
        return v.length() > 512 ? v.substring(0, 512) : v;
    }

    private static String normalizeHost(String host) {
        if (host == null || host.isBlank()) {
            return null;
        }
        return host.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean matchesAny(String host, Iterable<String> candidates) {
        for (String candidate : candidates) {
            if (candidate == null || candidate.isBlank()) {
                continue;
            }
            String normalized = candidate.toLowerCase(Locale.ROOT);
            if (host.equals(normalized) || host.endsWith(normalized) || host.contains(normalized)) {
                return true;
            }
        }
        return false;
    }

    private static String hmacHex(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA_256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA_256));
            byte[] out = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return toHex(out);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute visitor HMAC", e);
        }
    }

    private static String toHex(byte[] bytes) {
        char[] hex = new char[bytes.length * 2];
        final char[] alphabet = "0123456789abcdef".toCharArray();
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hex[i * 2] = alphabet[v >>> 4];
            hex[i * 2 + 1] = alphabet[v & 0x0F];
        }
        return new String(hex);
    }
}


