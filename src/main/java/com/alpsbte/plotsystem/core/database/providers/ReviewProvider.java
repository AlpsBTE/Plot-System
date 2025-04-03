package com.alpsbte.plotsystem.core.database.providers;

import com.alpsbte.plotsystem.core.system.review.PlotReview;
import com.alpsbte.plotsystem.core.system.review.ReviewRating;
import com.alpsbte.plotsystem.core.system.review.ToggleCriteria;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ReviewProvider {
    public Optional<PlotReview> getReview(int reviewId) {
        // TODO: implement
        return null;
    }

    public Optional<PlotReview> getLatestReview(int plotId) {
        return null;
    }

    public List<PlotReview> getPlotReviewHistory(int plotId) {
        // TODO: implement
        return List.of();
    }

    public boolean updateFeedback(int reviewId, String newFeedback) {
        // TODO: implement
        return false;
    }

    public List<ToggleCriteria> getBuildTeamToggleCriteria(int buildTeamId) {
        // TODO: implement
        return List.of();
    }

    public boolean createReview(ReviewRating rating, int score, UUID reviewerUUID, boolean isRejected) {
        // TODO: implement
        // also create feedback notification
        return false;
    }

    public boolean undoReview() {
        return false;
    }
}
