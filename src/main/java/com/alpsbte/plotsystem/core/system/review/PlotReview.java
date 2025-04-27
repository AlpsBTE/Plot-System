/*
 * The MIT License (MIT)
 *
 *  Copyright © 2025, Alps BTE <bte.atchli@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.alpsbte.plotsystem.core.system.review;

import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
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
    @Nullable private String feedback;

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

    public int getSplitScore() { return splitScore; }

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
        if (plot.getPlotOwner().getSlotByPlotId(plot.getID()) != null
                && !plot.getPlotOwner().setSlot(plot.getPlotOwner().getSlotByPlotId(plot.getID()), plot.getID()))
            return false;

        // remove members score and remove plot from slot
        for (Builder member : plot.getPlotMembers()) {
            if (!member.addScore(-splitScore)) return false;
            if (member.getSlotByPlotId(plot.getID()) != null && !member.setSlot(member.getSlotByPlotId(plot.getID()), plot.getID()))
                return false;
        }

        plot.setStatus(Status.unreviewed);
        plot.setPasted(false);

        DataProvider.PLOT.setCompletedSchematic(plot.getID(), null);
        return DataProvider.REVIEW.removeReview(reviewId);
    }
}
