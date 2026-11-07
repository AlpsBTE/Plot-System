package com.alpsbte.plotsystem.core.integration;

import com.alpsbte.plotsystem.api.model.PlotDifficulty;
import com.alpsbte.plotsystem.api.model.PlotReviewOutcome;
import com.alpsbte.plotsystem.api.model.PlotSnapshot;
import com.alpsbte.plotsystem.api.model.PlotStatus;
import com.alpsbte.plotsystem.api.model.PlotType;
import com.alpsbte.plotsystem.api.model.ReviewSnapshot;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.review.PlotReview;
import com.alpsbte.plotsystem.utils.conversion.CoordinateConversion;
import com.alpsbte.plotsystem.utils.conversion.projection.OutOfProjectionBoundsException;
import com.sk89q.worldedit.math.BlockVector3;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

public final class PlotEventSnapshots {
    private PlotEventSnapshots() {}

    public static @NotNull PlotSnapshot plot(@NotNull AbstractPlot plot) {
        Double[] coordinates = coordinates(plot);
        if (plot instanceof Plot typedPlot) {
            ZoneId zone = ZoneId.systemDefault();
            LocalDate abandonmentDate = typedPlot.getAbandonmentDate();
            return PlotSnapshot.builder()
                    .id(typedPlot.getId())
                    .owner(typedPlot.getPlotOwner() == null ? null : typedPlot.getPlotOwner().getUUID())
                    .status(PlotStatus.valueOf(typedPlot.getStatus().name().toUpperCase(Locale.ROOT)))
                    .type(PlotType.valueOf(typedPlot.getPlotType().name()))
                    .difficulty(PlotDifficulty.valueOf(typedPlot.getDifficulty().name()))
                    .cityProjectId(typedPlot.getCityProject().getId())
                    .countryCode(typedPlot.getCityProject().getCountry().getCode())
                    .lastActivity(typedPlot.getLastActivity())
                    .members(typedPlot.getPlotMembers().stream().map(member -> member.getUUID()).toList())
                    .longitude(coordinates[0])
                    .latitude(coordinates[1])
                    .reviewOutcome(typedPlot.getLatestReview()
                            .map(review -> review.getRating().isRejected()
                                    ? PlotReviewOutcome.REJECTED
                                    : PlotReviewOutcome.APPROVED)
                            .orElse(null))
                    .lastActivityAt(typedPlot.getLastActivityTime() == null ? null
                            : typedPlot.getLastActivityTime().atZone(zone).toInstant())
                    .abandonmentAt(abandonmentDate == null ? null
                            : abandonmentDate.atStartOfDay(zone).toInstant())
                    .build();
        }

        return PlotSnapshot.builder()
                .id(plot.getId())
                .owner(plot.getPlotOwner() == null ? null : plot.getPlotOwner().getUUID())
                .status(PlotStatus.valueOf(plot.getStatus().name().toUpperCase(Locale.ROOT)))
                .type(PlotType.valueOf(plot.getPlotType().name()))
                .lastActivity(plot.getLastActivity())
                .longitude(coordinates[0])
                .latitude(coordinates[1])
                .build();
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
        Plot currentPlot = DataProvider.PLOT.getPlotById(review.getPlot().getId());
        return new ReviewSnapshot(
                review.getReviewId(),
                plot(currentPlot == null ? review.getPlot() : currentPlot),
                review.getReviewerUUID(),
                review.getScore(),
                review.getSplitScore(),
                review.getFeedback());
    }
}
