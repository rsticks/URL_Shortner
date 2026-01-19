package faang.school.urlshortenerservice.dto.analytics;

public record UrlUtmDto(
        String utmSource,
        String utmMedium,
        String utmCampaign,
        String utmContent,
        String utmTerm
) {
}


