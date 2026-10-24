package com.alpsbte.plotsystem.core.integration;

import com.alpsbte.plotsystem.api.model.PlotDifficulty;
import com.alpsbte.plotsystem.api.model.PlotReviewOutcome;
import com.alpsbte.plotsystem.api.model.PlotSnapshot;
import com.alpsbte.plotsystem.api.model.PlotStatus;
import com.alpsbte.plotsystem.api.model.PlotType;
import com.alpsbte.plotsystem.api.model.ReviewSnapshot;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.review.PlotReview;
import com.alpsbte.plotsystem.utils.conversion.CoordinateConversion;
import com.alpsbte.plotsystem.utils.conversion.projection.OutOfProjectionBoundsException;
import com.sk89q.worldedit.math.BlockVector3;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public final class PlotEventSnapshots {
    private PlotEventSnapshots() {}

    public static @NotNull PlotSnapshot plot(@NotNull AbstractPlot plot) {
        Double[] coordinates = coordinates(plot);
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
                    typedPlot.getPlotMembers().stream().map(member -> member.getUUID()).toList(),
                    coordinates[0],
                    coordinates[1],
                    typedPlot.getLatestReview()
                            .map(review -> review.getRating().isRejected()
                                    ? PlotReviewOutcome.REJECTED
                                    : PlotReviewOutcome.APPROVED)
                            .orElse(null));
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
                List.of(),
                coordinates[0],
                coordinates[1],
                null);
    }

    private static Double[] coordinates(@NotNull AbstractPlot plot) {
        try {
            BlockVector3 minecraftCoordinates = plot.getCoordinates();
            double[] coordinates = CoordinateConversion.convertToGeo(minecraftCoordinates.x(), minecraftCoordinates.z());
            return new Double[]{coordinates[0], coordinates[1]};
        } catch (IOException exception) {
            PlotSystem.getPlugin().getComponentLogger().warn(
                    "Could not load geographic coordinates for plot #{}.", plot.getId(), exception);
        } catch (OutOfProjectionBoundsException exception) {
            PlotSystem.getPlugin().getComponentLogger().warn(
                    "Could not convert geographic coordinates for plot #{}.", plot.getId(), exception);
        }
        return new Double[]{null, null};
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
