package faang.school.urlshortenerservice.job;

import faang.school.urlshortenerservice.properties.AnalyticsProperties;
import faang.school.urlshortenerservice.repository.analytics.ClickEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClickEventCleanerScheduler {
    private final AnalyticsProperties analyticsProperties;
    private final ClickEventRepository clickEventRepository;
    private final TransactionTemplate transactionTemplate;

    @Scheduled(cron = "${analytics.clean-click-events-cron}")
    public void cleanClickEvents() {
        transactionTemplate.executeWithoutResult(status -> {
            OffsetDateTime cutoff = OffsetDateTime.now(ZoneOffset.UTC)
                    .minus(analyticsProperties.getClickEventRetention());
            int deleted = clickEventRepository.deleteOlderThan(cutoff);
            log.info("Cleaned click_event older than {} (deleted={})", cutoff, deleted);
        });
    }
}


