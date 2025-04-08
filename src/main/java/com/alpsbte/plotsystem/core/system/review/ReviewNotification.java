package com.alpsbte.plotsystem.core.system.review;

import java.util.UUID;

public class ReviewNotification {
    private final int reviewId;
    private final UUID uuid;

    public ReviewNotification(int reviewId, UUID uuid) {
        this.reviewId = reviewId;
        this.uuid = uuid;
    }

    public int getReviewId() {
        return reviewId;
    }

    public UUID getUuid() {
        return uuid;
    }
}
