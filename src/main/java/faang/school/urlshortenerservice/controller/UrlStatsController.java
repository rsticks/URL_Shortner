package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.analytics.UrlStatsResponse;
import faang.school.urlshortenerservice.service.analytics.UrlStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("${api-version}/url")
@RequiredArgsConstructor
public class UrlStatsController {
    private final UrlStatsService urlStatsService;

    @GetMapping("/{hash}/stats")
    public UrlStatsResponse stats(
            @PathVariable("hash") String hash,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "top", defaultValue = "10") int top
    ) {
        return urlStatsService.getStatsForOwner(hash, from, to, top);
    }
}


