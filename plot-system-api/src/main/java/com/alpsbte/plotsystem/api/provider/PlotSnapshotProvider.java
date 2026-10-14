package com.alpsbte.plotsystem.api.provider;

import com.alpsbte.plotsystem.api.model.PlotSnapshot;

import java.util.Optional;

/**
 * Provides the current persisted snapshot of a plot.
 *
 * <p>Retrieve the provider from Bukkit's {@code ServicesManager}. Unlike
 * lifecycle events, lookups also work for plots that existed before an
 * integration started.</p>
 */
@FunctionalInterface
public interface PlotSnapshotProvider {
    Optional<PlotSnapshot> getPlotSnapshot(int plotId);
}
