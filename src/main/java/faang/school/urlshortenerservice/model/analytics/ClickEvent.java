package faang.school.urlshortenerservice.model.analytics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "click_event")
public class ClickEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url_hash", nullable = false, length = 6)
    private String urlHash;

    @Column(name = "clicked_at", nullable = false)
    private OffsetDateTime clickedAt;

    @Column(name = "visitor_hash_day", nullable = false, length = 64)
    private String visitorHashDay;

    @Column(name = "referrer_host")
    private String referrerHost;

    @Column(name = "language")
    private String language;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "os_family")
    private String osFamily;

    @Column(name = "browser_family")
    private String browserFamily;

    @Column(name = "browser_version")
    private String browserVersion;

    @Column(name = "os_version")
    private String osVersion;

    @Column(name = "country")
    private String country;

    @Column(name = "region_name")
    private String region;

    @Column(name = "city")
    private String city;

    @Column(name = "time_zone")
    private String timezone;

    @Column(name = "asn")
    private String asn;

    @Column(name = "provider")
    private String provider;

    @Column(name = "network_type")
    private String networkType;

    @Column(name = "proxy_status")
    private String proxyStatus;

    @Column(name = "vpn_status")
    private String vpnStatus;

    @Column(name = "tor_status")
    private String torStatus;

    @Column(name = "referrer_category")
    private String referrerCategory;

    @Column(name = "client_hint_platform")
    private String clientHintPlatform;

    @Column(name = "client_hint_platform_version")
    private String clientHintPlatformVersion;

    @Column(name = "client_hint_mobile")
    private String clientHintMobile;

    @Column(name = "client_hint_model")
    private String clientHintModel;

    @Column(name = "hour_of_day")
    private Integer hourOfDay;

    @Column(name = "day_of_week")
    private String dayOfWeek;

    @Column(name = "is_bot", nullable = false)
    private boolean bot;

    @Column(name = "http_status", nullable = false)
    private int httpStatus;

    @Column(name = "result", nullable = false)
    private String result;
}


