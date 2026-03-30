package faang.school.urlshortenerservice.service.analytics;

public final class AnalyticsDimensions {
    public static final String REFERRER = "referrer_host";
    public static final String REFERRER_CATEGORY = "referrer_category";
    public static final String LANGUAGE = "language";
    public static final String DEVICE = "device_type";
    public static final String OS = "os_family";
    public static final String OS_VERSION = "os_version";
    public static final String BROWSER = "browser_family";
    public static final String BROWSER_VERSION = "browser_version";
    public static final String BOT = "is_bot";
    public static final String COUNTRY = "country";
    public static final String REGION = "region";
    public static final String CITY = "city";
    public static final String TIMEZONE = "timezone";
    public static final String ASN = "asn";
    public static final String PROVIDER = "provider";
    public static final String NETWORK_TYPE = "network_type";
    public static final String PROXY_STATUS = "proxy_status";
    public static final String VPN_STATUS = "vpn_status";
    public static final String TOR_STATUS = "tor_status";
    public static final String HOUR_OF_DAY = "hour_of_day";
    public static final String DAY_OF_WEEK = "day_of_week";
    public static final String CLIENT_HINT_PLATFORM = "client_hint_platform";
    public static final String CLIENT_HINT_PLATFORM_VERSION = "client_hint_platform_version";
    public static final String CLIENT_HINT_MOBILE = "client_hint_mobile";
    public static final String CLIENT_HINT_MODEL = "client_hint_model";

    private AnalyticsDimensions() {
    }
}
