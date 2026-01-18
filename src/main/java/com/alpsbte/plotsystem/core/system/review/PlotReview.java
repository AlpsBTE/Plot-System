package com.alpsbte.plotsystem.core.system.review;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.DiscordUtil;
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
            DiscordUtil.getOpt(this.plot.getId()).ifPresent(event -> event.onPlotFeedback(feedback));
            return true;
        }
        return false;
    }

    public boolean undoReview() {
        // remove owner score and remove plot from slot
        if (!plot.getPlotOwner().addScore(splitScore == -1 ? -score : -splitScore)) return false;

        Slot slot = plot.getPlotOwner().getSlotByPlotId(plot.getId()); // get slot if plot is still in slots (rejected)
        if (slot == null) slot = plot.getPlotOwner().getFreeSlot(); // get new slot otherwise (completed)
        if (slot == null) return false;

        if (!plot.getPlotOwner().setSlot(slot, plot.getId())) return false;

        // remove members score and remove plot from slot
        for (Builder member : plot.getPlotMembers()) {
            if (!member.addScore(-splitScore)) return false;

            Slot memberSlot = member.getSlotByPlotId(plot.getId());
            if (memberSlot == null) memberSlot = member.getFreeSlot();
            if (memberSlot == null || member.setSlot(memberSlot, plot.getId())) return false;
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
        else {
            DiscordUtil.getOpt(this.plot.getId()).ifPresent(DiscordUtil.PlotEventAction::onPlotUndoReview);
        }

        return successful;
    }
}
