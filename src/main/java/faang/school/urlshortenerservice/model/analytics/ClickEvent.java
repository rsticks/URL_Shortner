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

    @Column(name = "is_bot", nullable = false)
    private boolean bot;

    @Column(name = "http_status", nullable = false)
    private int httpStatus;

    @Column(name = "result", nullable = false)
    private String result;
}


