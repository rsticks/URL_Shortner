package faang.school.urlshortenerservice.service.analytics;

import faang.school.urlshortenerservice.dto.analytics.UrlStatsResponse;

import java.time.LocalDate;

public interface UrlStatsService {
    UrlStatsResponse getStatsForOwner(String urlHash, LocalDate from, LocalDate to, int topLimit);
}


