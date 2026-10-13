package com.alpsbte.plotsystem.core.integration;

import com.alpsbte.plotsystem.api.model.PlotDifficulty;
import com.alpsbte.plotsystem.api.model.PlotSnapshot;
import com.alpsbte.plotsystem.api.model.PlotStatus;
import com.alpsbte.plotsystem.api.model.PlotType;
import com.alpsbte.plotsystem.api.model.ReviewSnapshot;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.review.PlotReview;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class PlotEventSnapshots {
    private PlotEventSnapshots() {}

    public static @NotNull PlotSnapshot plot(@NotNull AbstractPlot plot) {
        if (plot instanceof Plot typedPlot) {
            return new PlotSnapshot(
                    typedPlot.getId(),
                    typedPlot.getPlotOwner() == null ? null : typedPlot.getPlotOwner().getUUID(),
                    PlotStatus.valueOf(typedPlot.getStatus().name().toUpperCase()),
                    PlotType.valueOf(typedPlot.getPlotType().name()),
                    PlotDifficulty.valueOf(typedPlot.getDifficulty().name()),
                    typedPlot.getCityProject().getId(),
                    typedPlot.getCityProject().getCountry().getCode(),
                    typedPlot.getLastActivity(),
                    typedPlot.getPlotMembers().stream().map(member -> member.getUUID()).toList());
        }

        return new PlotSnapshot(
                plot.getId(),
                plot.getPlotOwner() == null ? null : plot.getPlotOwner().getUUID(),
                PlotStatus.valueOf(plot.getStatus().name().toUpperCase()),
                PlotType.valueOf(plot.getPlotType().name()),
                null,
                null,
                null,
                plot.getLastActivity(),
                List.of());
    }

    public static @NotNull ReviewSnapshot review(@NotNull PlotReview review) {
        return new ReviewSnapshot(
                review.getReviewId(),
                plot(review.getPlot()),
                review.getReviewerUUID(),
                review.getScore(),
                review.getSplitScore(),
                review.getFeedback());
    }
}
