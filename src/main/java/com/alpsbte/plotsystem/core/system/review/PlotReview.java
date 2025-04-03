package com.alpsbte.plotsystem.core.system.review;

import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class PlotReview {
    private final int reviewId;
    private final int plotId;
    private final ReviewRating rating;
    private List<ToggleCriteria> toggleCriteria;
    private int score;
    @Nullable
    private String feedback;
    private UUID reviewedBy;

    public PlotReview(int reviewId, int plotId, ReviewRating rating, int score, @Nullable String feedback) {
        this.reviewId = reviewId;
        this.plotId = plotId;
        this.rating = rating;
        this.score = score;
        this.feedback = feedback;
    }

    public int getReviewId() {
        return reviewId;
    }

    public ReviewRating getRating() {
        return rating;
    }

    public @Nullable String getFeedback() {
        return feedback;
    }

    public Plot getPlot() {
        return DataProvider.PLOT.getPlotById(plotId);
    }

    public int getPlotId() {
        return plotId;
    }

    public Builder getReviewer() {
        return DataProvider.BUILDER.getBuilderByUUID(reviewedBy);
    }

    public UUID getReviewerUUID() {
        return reviewedBy;
    }

    public int getScore() {
        return score;
    }

    public boolean updateFeedback(String feedback) {
        if (DataProvider.REVIEW.updateFeedback(reviewId, feedback)) {
            this.feedback = feedback;
            return true;
        }
        return false;
    }
}
