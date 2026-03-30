package faang.school.urlshortenerservice.service.analytics.impl;

import faang.school.urlshortenerservice.service.analytics.UserAgentMetadata;
import faang.school.urlshortenerservice.service.analytics.UserAgentMetadataResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import ua_parser.Client;
import ua_parser.Parser;

import java.util.Locale;

@Service
public class UserAgentMetadataResolverImpl implements UserAgentMetadataResolver {
    private final Parser parser = new Parser();

    @Override
    public UserAgentMetadata resolve(HttpServletRequest request) {
        String userAgent = header(request, "User-Agent");
        String clientHintPlatform = structuredHeader(request, "Sec-CH-UA-Platform", 64);
        String clientHintPlatformVersion = structuredHeader(request, "Sec-CH-UA-Platform-Version", 64);
        String clientHintMobile = clientHintMobile(request);
        String clientHintModel = structuredHeader(request, "Sec-CH-UA-Model", 128);

        Client parsed = parse(userAgent);
        String browserFamily = parsed != null && parsed.userAgent != null
                ? normalize(parsed.userAgent.family, 64)
                : "unknown";
        String browserVersion = parsed != null && parsed.userAgent != null
                ? normalize(join(parsed.userAgent.major, parsed.userAgent.minor, parsed.userAgent.patch), 64)
                : "unknown";
        String osFamily = parsed != null && parsed.os != null
                ? normalize(parsed.os.family, 64)
                : "unknown";
        String osVersion = parsed != null && parsed.os != null
                ? normalize(join(parsed.os.major, parsed.os.minor, parsed.os.patch, parsed.os.patchMinor), 64)
                : "unknown";

        if ("unknown".equals(osFamily) && !"unknown".equals(clientHintPlatform)) {
            osFamily = clientHintPlatform;
        }
        if ("unknown".equals(osVersion) && !"unknown".equals(clientHintPlatformVersion)) {
            osVersion = clientHintPlatformVersion;
        }

        String deviceType = detectDeviceType(userAgent, clientHintMobile);

        return UserAgentMetadata.builder()
                .deviceType(deviceType)
                .browserFamily(browserFamily)
                .browserVersion(browserVersion)
                .osFamily(osFamily)
                .osVersion(osVersion)
                .clientHintPlatform(clientHintPlatform)
                .clientHintPlatformVersion(clientHintPlatformVersion)
                .clientHintMobile(clientHintMobile)
                .clientHintModel(clientHintModel)
                .build();
    }

    private Client parse(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return null;
        }
        try {
            return parser.parse(userAgent);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static String detectDeviceType(String userAgent, String clientHintMobile) {
        if ("mobile".equals(clientHintMobile)) {
            return "mobile";
        }
        if (userAgent == null || userAgent.isBlank()) {
            return "unknown";
        }
        String normalized = userAgent.toLowerCase(Locale.ROOT);
        if (normalized.contains("ipad") || normalized.contains("tablet")) {
            return "tablet";
        }
        if (normalized.contains("mobi") || normalized.contains("android") || normalized.contains("iphone")) {
            return "mobile";
        }
        return "desktop";
    }

    private static String clientHintMobile(HttpServletRequest request) {
        String header = structuredHeader(request, "Sec-CH-UA-Mobile", 16);
        if ("?1".equals(header) || "1".equals(header) || "true".equalsIgnoreCase(header)) {
            return "mobile";
        }
        if ("?0".equals(header) || "0".equals(header) || "false".equalsIgnoreCase(header)) {
            return "desktop";
        }
        return "unknown";
    }

    private static String structuredHeader(HttpServletRequest request, String headerName, int maxLength) {
        String raw = header(request, headerName);
        if (raw == null) {
            return "unknown";
        }
        String unquoted = raw.trim();
        if (unquoted.startsWith("\"") && unquoted.endsWith("\"") && unquoted.length() > 1) {
            unquoted = unquoted.substring(1, unquoted.length() - 1);
        }
        return normalize(unquoted, maxLength);
    }

    private static String header(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String join(String... values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                break;
            }
            if (builder.length() > 0) {
                builder.append('.');
            }
            builder.append(value);
        }
        return builder.length() == 0 ? "unknown" : builder.toString();
    }

    private static String normalize(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        String trimmed = value.trim();
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) : trimmed;
    }
}
