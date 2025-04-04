package com.alpsbte.plotsystem.core.system.review;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.enums.Status;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

public class PlotReview {
    private final int reviewId;
    private final int plotId;
    private final ReviewRating rating;
    private final int score;
    private final UUID reviewedBy;
    @Nullable
    private String feedback;


    public PlotReview(int reviewId, int plotId, ReviewRating rating, int score, @Nullable String feedback, UUID reviewedBy) {
        this.reviewId = reviewId;
        this.plotId = plotId;
        this.rating = rating;
        this.score = score;
        this.feedback = feedback;
        this.reviewedBy = reviewedBy;
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

    public boolean undoReview() {
        Plot plot = DataProvider.PLOT.getPlotById(this.plotId);
        if (plot == null) {
            PlotSystem.getPlugin().getComponentLogger().error(text("Plot of review could not be found!"));
            return false;
        }

        // remove owner score and remove plot from slot
        plot.getPlotOwner().addScore(-plot.getSharedScore());
        if (plot.getPlotOwner().getFreeSlot() != null) {
            plot.getPlotOwner().setSlot(plot.getPlotOwner().getFreeSlot(), plot.getID());
        }

        // remove members score and remove plot from slot
        for (Builder member : plot.getPlotMembers()) {
            member.addScore(-plot.getSharedScore());

            if (member.getFreeSlot() != null) {
                member.setSlot(member.getFreeSlot(), plot.getID());
            }
        }

        plot.setStatus(Status.unreviewed);
        plot.setPasted(false);

        DataProvider.PLOT.setCompletedSchematic(plot.getID(), null);

        return DataProvider.REVIEW.removeReview(reviewId);
    }
}
