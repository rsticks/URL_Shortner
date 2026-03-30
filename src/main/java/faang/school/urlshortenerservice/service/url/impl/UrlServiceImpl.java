package faang.school.urlshortenerservice.service.url.impl;

import faang.school.urlshortenerservice.dto.ResponseDto;
import faang.school.urlshortenerservice.dto.UserLinkDto;
import faang.school.urlshortenerservice.model.AppUser;
import faang.school.urlshortenerservice.model.Url;
import faang.school.urlshortenerservice.model.UserUrl;
import faang.school.urlshortenerservice.repository.UserUrlRepository;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import faang.school.urlshortenerservice.service.hash.HashCache;
import faang.school.urlshortenerservice.service.user.AppUserService;
import faang.school.urlshortenerservice.service.url.UrlService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class UrlServiceImpl implements UrlService {
    private static final String URL_FORMAT = "%s://%s:%d%s/%s";
    private final UrlCacheRepository urlCacheRepository;
    private final HashCache hashCache;
    private final AppUserService appUserService;
    private final UserUrlRepository userUrlRepository;
    private final UrlRepository urlRepository;
    @Value("${api-version}")
    private String apiVersion;

    @Override
    public List<Url> getAndDeleteOldUrls(LocalDateTime olderThan) {
        log.info("Deleting and return urls older than {}", olderThan);
        List<Url> urls = urlCacheRepository.deleteAndReturnByCreatedAtBefore(olderThan);
        log.info("Deleted urls {}", urls);
        return urls;
    }

    @Override
    @Transactional
    public ResponseDto createShortUrl(String originalUrl, HttpServletRequest request) {
        log.info("Start creating short for url: {}", originalUrl);

        Url url = urlCacheRepository.save(new Url(hashCache.getHash(), originalUrl, LocalDateTime.now()));
        try {
            AppUser currentUser = appUserService.getCurrentUser();
            UserUrl userUrl = new UserUrl();
            userUrl.setUrlHash(url.getHash());
            userUrl.setUser(currentUser);
            userUrlRepository.save(userUrl);
        } catch (EntityNotFoundException ex) {
            // user not authenticated -> link remains anonymous
        }
        String shortUrl = buildUrl(url, request);

        log.info("Created short url: {}", shortUrl);
        return new ResponseDto(shortUrl);
    }

    @Override
    public List<UserLinkDto> getMyUrls(HttpServletRequest request) {
        AppUser user = appUserService.getCurrentUser();
        List<UserUrl> links = userUrlRepository.findAllByUser_IdOrderByCreatedAtDesc(user.getId());
        if (links.isEmpty()) {
            return List.of();
        }

        List<String> hashes = links.stream()
                .map(UserUrl::getUrlHash)
                .filter(Objects::nonNull)
                .toList();
        Map<String, Url> urlsByHash = new HashMap<>();
        @SuppressWarnings("null")
        Iterable<Url> urls = urlRepository.findAllById(hashes);
        for (Url url : urls) {
            urlsByHash.put(url.getHash(), url);
        }

        return links.stream()
                .map(link -> urlsByHash.get(link.getUrlHash()))
                .filter(Objects::nonNull)
                .map(url -> new UserLinkDto(url.getHash(), url.getUrl(), buildUrl(url, request), url.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public String getUrlByHash(String hash) {
        log.info("Getting url by hash {}", hash);
        return urlCacheRepository.findByHash(hash).getUrl();
    }

    private String buildUrl(Url url, HttpServletRequest request) {
        return String.format(URL_FORMAT,
                request.getScheme(),
                request.getServerName(),
                request.getServerPort(),
                apiVersion,
                url.getHash());
    }
}
