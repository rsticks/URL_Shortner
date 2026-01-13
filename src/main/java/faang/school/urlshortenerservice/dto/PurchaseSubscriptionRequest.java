package faang.school.urlshortenerservice.dto;

import faang.school.urlshortenerservice.service.user.dto.SubscriptionPlan;
import jakarta.validation.constraints.NotNull;

public record PurchaseSubscriptionRequest(
        @NotNull SubscriptionPlan plan
) {
}


