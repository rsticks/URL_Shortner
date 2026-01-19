package faang.school.urlshortenerservice.service.analytics.impl;

import faang.school.urlshortenerservice.properties.AnalyticsProperties;
import faang.school.urlshortenerservice.service.analytics.RequestFingerprint;
import faang.school.urlshortenerservice.service.analytics.RequestFingerprintService;
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

    @Override
    public RequestFingerprint fingerprint(LocalDate dayUtc, HttpServletRequest request) {
        String userAgent = header(request, "User-Agent");
        String ip = clientIp(request);

        boolean bot = isBot(userAgent);
        String deviceType = bot ? "bot" : deviceType(userAgent);

        String referrerHost = referrerHost(request);
        String language = language(request);

        String osFamily = osFamily(userAgent);
        String browserFamily = browserFamily(userAgent);

        String visitorHashDay = hmacHex(properties.getVisitorHmacSecret(),
                dayUtc + "|" + normalizeIp(ip) + "|" + normalizeUserAgent(userAgent));

        return RequestFingerprint.builder()
                .visitorHashDay(visitorHashDay)
                .referrerHost(referrerHost)
                .language(language)
                .deviceType(deviceType)
                .osFamily(osFamily)
                .browserFamily(browserFamily)
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

    private static String deviceType(String ua) {
        if (ua == null) return "unknown";
        String s = ua.toLowerCase(Locale.ROOT);
        if (s.contains("ipad") || s.contains("tablet")) return "tablet";
        if (s.contains("mobi") || s.contains("android") || s.contains("iphone")) return "mobile";
        return "desktop";
    }

    private static String osFamily(String ua) {
        if (ua == null) return "unknown";
        String s = ua.toLowerCase(Locale.ROOT);
        if (s.contains("windows")) return "Windows";
        if (s.contains("android")) return "Android";
        if (s.contains("iphone") || s.contains("ipad") || s.contains("ios")) return "iOS";
        if (s.contains("mac os x") || s.contains("macintosh")) return "macOS";
        if (s.contains("linux")) return "Linux";
        return "Other";
    }

    private static String browserFamily(String ua) {
        if (ua == null) return "unknown";
        String s = ua.toLowerCase(Locale.ROOT);
        if (s.contains("edg/") || s.contains("edge/")) return "Edge";
        if (s.contains("opr/") || s.contains("opera")) return "Opera";
        if (s.contains("firefox/")) return "Firefox";
        if (s.contains("chrome/") && !s.contains("edg/") && !s.contains("opr/")) return "Chrome";
        if (s.contains("safari/") && !s.contains("chrome/") && !s.contains("chromium")) return "Safari";
        return "Other";
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


