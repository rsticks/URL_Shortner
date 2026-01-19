package faang.school.urlshortenerservice.service.url.impl;

import faang.school.urlshortenerservice.dto.ResponseDto;
import faang.school.urlshortenerservice.dto.UserLinkDto;
import faang.school.urlshortenerservice.exception.AuthenticationRequiredException;
import faang.school.urlshortenerservice.exception.SubscriptionRequiredException;
import faang.school.urlshortenerservice.model.AppUser;
import faang.school.urlshortenerservice.model.Url;
import faang.school.urlshortenerservice.model.UrlUtm;
import faang.school.urlshortenerservice.model.UserUrl;
import faang.school.urlshortenerservice.repository.UserUrlRepository;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import faang.school.urlshortenerservice.repository.UrlUtmRepository;
import faang.school.urlshortenerservice.service.hash.HashCache;
import faang.school.urlshortenerservice.service.user.AppUserService;
import faang.school.urlshortenerservice.service.url.UrlService;
import faang.school.urlshortenerservice.service.url.UtmParser;
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
    private final UrlUtmRepository urlUtmRepository;
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
    public ResponseDto createShortUrl(String originalUrl, boolean saveUtm, HttpServletRequest request) {
        log.info("Start creating short for url: {}", originalUrl);

        Url url = urlCacheRepository.save(new Url(hashCache.getHash(), originalUrl, LocalDateTime.now()));
        if (saveUtm) {
            // saving UTM is a paid feature: requires authentication + active subscription
            AppUser currentUser;
            try {
                currentUser = appUserService.getCurrentUser();
            } catch (EntityNotFoundException ex) {
                throw new AuthenticationRequiredException("Authentication required to save UTM");
            }
            if (!appUserService.isSubscribed(currentUser)) {
                throw new SubscriptionRequiredException("Subscription required to create short links with UTM");
            }
            UserUrl userUrl = new UserUrl();
            userUrl.setUrlHash(url.getHash());
            userUrl.setUser(currentUser);
            userUrlRepository.save(userUrl);

            var utm = UtmParser.parseUtmParams(originalUrl);
            if (!utm.isEmpty()) {
                UrlUtm meta = new UrlUtm();
                meta.setUrlHash(url.getHash());
                meta.setUtmSource(utm.get("utm_source"));
                meta.setUtmMedium(utm.get("utm_medium"));
                meta.setUtmCampaign(utm.get("utm_campaign"));
                meta.setUtmContent(utm.get("utm_content"));
                meta.setUtmTerm(utm.get("utm_term"));
                urlUtmRepository.save(meta);
            }
        } else {
            try {
                AppUser currentUser = appUserService.getCurrentUser();
                UserUrl userUrl = new UserUrl();
                userUrl.setUrlHash(url.getHash());
                userUrl.setUser(currentUser);
                userUrlRepository.save(userUrl);
            } catch (EntityNotFoundException ex) {
                // user not authenticated -> link remains anonymous
            }
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
