package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.ResponseDto;
import faang.school.urlshortenerservice.dto.UserLinkDto;
import faang.school.urlshortenerservice.dto.UrlDto;
import faang.school.urlshortenerservice.service.analytics.ClickAnalyticsService;
import faang.school.urlshortenerservice.service.url.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("${api-version}/")
@Validated
@RequiredArgsConstructor
public class UrlController {
    private final UrlService urlService;
    private final ClickAnalyticsService clickAnalyticsService;

    @PostMapping("/url")
    public ResponseDto createShortUrl(@Valid @RequestBody UrlDto url, HttpServletRequest request) {
        return urlService.createShortUrl(url.getUrl(), request);
    }

    @GetMapping("/url/me")
    public List<UserLinkDto> myUrls(HttpServletRequest request) {
        return urlService.getMyUrls(request);
    }

    @GetMapping("/{hash}")
    public ResponseEntity<Void> redirect(@PathVariable String hash, HttpServletRequest request) {
        String url = urlService.getUrlByHash(hash);
        clickAnalyticsService.recordRedirect(hash, HttpStatus.FOUND.value(), request);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, url)
                .build();
    }
}
