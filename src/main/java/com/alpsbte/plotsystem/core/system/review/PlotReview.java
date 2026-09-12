package com.alpsbte.plotsystem.core.system.review;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.enums.Slot;
import com.alpsbte.plotsystem.utils.enums.Status;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlotReview {
    private final int reviewId;
    private final Plot plot;
    private final ReviewRating rating;
    private final int score;
    private final int splitScore;
    private final UUID reviewedBy;
    @Nullable
    private String feedback;

    public PlotReview(int reviewId, int plotId, ReviewRating rating, int score, @Nullable String feedback, UUID reviewedBy) {
        this(reviewId, DataProvider.PLOT.getPlotById(plotId), rating, score, feedback, reviewedBy);
    }

    public PlotReview(int reviewId, Plot plot, ReviewRating rating, int score, @Nullable String feedback, UUID reviewedBy) {
        this.reviewId = reviewId;
        this.plot = plot;
        this.rating = rating;
        this.score = score;
        this.splitScore = plot.getPlotMembers().isEmpty() ? -1 : (int) Math.floor(score / (plot.getPlotMembers().size() + 1d));
        this.feedback = feedback;
        this.reviewedBy = reviewedBy;
    }

    public int getReviewId() {
        return reviewId;
    }

    public ReviewRating getRating() {
        return rating;
    }

    public int getScore() {
        return score;
    }

    public int getSplitScore() {return splitScore;}

    public @Nullable String getFeedback() {
        return feedback;
    }

    public Plot getPlot() {
        return plot;
    }

    public Builder getReviewer() {
        return DataProvider.BUILDER.getBuilderByUUID(reviewedBy);
    }

    public UUID getReviewerUUID() {
        return reviewedBy;
    }

    public boolean updateFeedback(String feedback) {
        if (DataProvider.REVIEW.updateFeedback(reviewId, feedback)) {
            this.feedback = feedback;
            return true;
        }
        return false;
    }

    public boolean undoReview() {
        // remove owner score and remove plot from slot
        if (!plot.getPlotOwner().addScore(splitScore == -1 ? -score : -splitScore)) return false;
        if (!restorePlotSlot(plot.getPlotOwner())) return false;

        // remove member's score and remove plot from slot
        for (Builder member : plot.getPlotMembers()) {
            if (!member.addScore(-splitScore)) return false;
            if (!restorePlotSlot(member)) return false;
        }

        boolean successful = true;
        if (!plot.setStatus(Status.unreviewed)) {
            successful = false;
            PlotSystem.getPlugin().getComponentLogger().error("Failed to set plot status to unreviewed while undoing review for plot ID {}", plot.getId());
        }

        if (!plot.setPasted(false)) {
            successful = false;
            PlotSystem.getPlugin().getComponentLogger().error("Failed to set plot pasted status to false while undoing review for plot ID {}", plot.getId());
        }

        if (!DataProvider.REVIEW.removeReview(reviewId)) {
            successful = false;
            PlotSystem.getPlugin().getComponentLogger().error("Failed to remove plot review with ID {} from database!", reviewId);
        }

        return successful;
    }

    /**
     * Tries to assign the plot to a slot again if possible.
     * If all slots are occupied, the plot won't be assigned to a slot, but still accessible via /plots
     *
     * @param builder target builder (owner or member)
     * @return True if the slot was assigned successfully, false otherwise
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean restorePlotSlot(Builder builder) {
        Slot slot = builder.getSlotByPlotId(plot.getId()); // get slot if plot is still in slots (rejected)
        if (slot == null) slot = builder.getFreeSlot(); // get new slot otherwise (completed)
        if (slot == null) {
            PlotSystem.getPlugin().getComponentLogger().warn("Skipping slot restore for plot #{} and builder {}. All slots are occupied!", plot.getId(), builder.getName());
            return true;
        }

        return builder.setSlot(slot, plot.getId());
    }
}
