package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.dto.PurchaseSubscriptionRequest;
import faang.school.urlshortenerservice.model.AppUser;
import faang.school.urlshortenerservice.service.user.AppUserService;
import faang.school.urlshortenerservice.service.user.dto.PurchaseResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api-version}/subscription")
@RequiredArgsConstructor
public class SubscriptionController {
    private final AppUserService appUserService;

    @PostMapping("/purchase")
    public PurchaseResult purchase(@Valid @RequestBody PurchaseSubscriptionRequest request) {
        return appUserService.purchaseSubscription(request.plan());
    }

    @GetMapping("/me")
    public PurchaseResult me() {
        AppUser user = appUserService.getCurrentUser();
        return new PurchaseResult(user.getUsername(), user.getSubscriptionExpiresAt(), appUserService.isSubscribed(user));
    }
}


