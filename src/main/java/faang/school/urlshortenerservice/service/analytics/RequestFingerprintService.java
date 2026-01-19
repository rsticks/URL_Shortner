package faang.school.urlshortenerservice.service.analytics;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;

public interface RequestFingerprintService {
    RequestFingerprint fingerprint(LocalDate dayUtc, HttpServletRequest request);
}


