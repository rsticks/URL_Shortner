package faang.school.urlshortenerservice.service.user;

import faang.school.urlshortenerservice.model.AppUser;
import faang.school.urlshortenerservice.service.user.dto.PurchaseResult;
import faang.school.urlshortenerservice.service.user.dto.RegisterResult;
import faang.school.urlshortenerservice.service.user.dto.SubscriptionPlan;

public interface AppUserService {
    RegisterResult register(String username, String rawPassword);

    AppUser getCurrentUser();

    boolean isSubscribed(AppUser user);

    void ensureSubscribed();

    PurchaseResult purchaseSubscription(SubscriptionPlan plan);
}


