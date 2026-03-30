package faang.school.urlshortenerservice.service.analytics;

import jakarta.servlet.http.HttpServletRequest;

public interface UserAgentMetadataResolver {
    UserAgentMetadata resolve(HttpServletRequest request);
}
