package faang.school.urlshortenerservice.service.analytics;

import jakarta.servlet.http.HttpServletRequest;

public interface ClickAnalyticsService {
    void recordRedirect(String urlHash, int httpStatus, HttpServletRequest request);
}


