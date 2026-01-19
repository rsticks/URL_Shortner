package faang.school.urlshortenerservice.service.url;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class UtmParser {
    private UtmParser() {
    }

    public static Map<String, String> parseUtmParams(String url) {
        if (url == null || url.isBlank()) {
            return Collections.emptyMap();
        }
        int q = url.indexOf('?');
        if (q < 0 || q == url.length() - 1) {
            return Collections.emptyMap();
        }
        String query = url.substring(q + 1);
        int hash = query.indexOf('#');
        if (hash >= 0) {
            query = query.substring(0, hash);
        }
        if (query.isBlank()) {
            return Collections.emptyMap();
        }

        Map<String, String> out = new HashMap<>();
        for (String part : query.split("&")) {
            if (part.isBlank()) continue;
            String[] kv = part.split("=", 2);
            String key = decode(kv[0]).toLowerCase();
            if (!isUtmKey(key)) continue;
            String value = kv.length > 1 ? decode(kv[1]) : "";
            if (value.length() > 255) {
                value = value.substring(0, 255);
            }
            out.put(key, value);
        }
        return out;
    }

    private static boolean isUtmKey(String key) {
        return "utm_source".equals(key)
                || "utm_medium".equals(key)
                || "utm_campaign".equals(key)
                || "utm_content".equals(key)
                || "utm_term".equals(key);
    }

    private static String decode(String s) {
        if (s == null) return "";
        return URLDecoder.decode(s.replace("+", "%2B"), StandardCharsets.UTF_8);
    }
}


