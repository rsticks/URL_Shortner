package faang.school.urlshortenerservice.service.user.dto;

public enum SubscriptionPlan {
    MONTHLY(30),
    YEARLY(365);

    private final int days;

    SubscriptionPlan(int days) {
        this.days = days;
    }

    public int getDays() {
        return days;
    }
}


