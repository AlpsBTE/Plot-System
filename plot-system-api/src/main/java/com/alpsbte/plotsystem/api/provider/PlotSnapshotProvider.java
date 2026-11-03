package com.alpsbte.plotsystem.api.provider;

import com.alpsbte.plotsystem.api.model.PlotSnapshot;
import com.alpsbte.plotsystem.api.model.PlotStatus;

import java.util.List;
import java.util.Optional;

/**
 * Provides the current persisted snapshot of a plot.
 *
 * <p>Retrieve the provider from Bukkit's {@code ServicesManager}. Unlike
 * lifecycle events, lookups also work for plots that existed before an
 * integration started. These are blocking database reads; call them off the
 * server thread. Tutorial plots are not included.</p>
 *
 * <p>Integrations should register listeners before enumerating snapshots on
 * startup, reconcile their persisted reminders against these snapshots, and
 * re-fetch a plot before sending a reminder. Reconcile periodically as well
 * to recover missed events or changes to the abandonment-policy configuration.
 * Delivery times, time zones, deduplication, and reminder persistence belong
 * to the integration, not Plot System.</p>
 */
public interface PlotSnapshotProvider {
    Optional<PlotSnapshot> getPlotSnapshot(int plotId);

    /**
     * Returns current snapshots matching any supplied status, or all plots if
     * no statuses are supplied. For inactivity reminders use {@code UNFINISHED}
     * and check {@code owner}, {@code lastActivityAt}, and {@code abandonmentAt}.
     * A null deadline does not prevent integration-owned reminders, but means
     * Plot System will not currently abandon the plot for inactivity.
     */
    List<PlotSnapshot> getPlotSnapshots(PlotStatus... statuses);
}
