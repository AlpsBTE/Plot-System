package com.alpsbte.plotsystem.api.event;

import com.alpsbte.plotsystem.api.model.PlotNotificationReason;
import com.alpsbte.plotsystem.api.model.PlotSnapshot;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Fired by an integration immediately before it would deliver a plot
 * notification. Cancel to veto that delivery only; lifecycle work has already
 * completed and is unaffected.
 *
 * <p>Plot System publishes observe-only lifecycle events and does not send
 * Discord or similar notifications. Integrations that notify must call this
 * event (on the server thread) and skip delivery when cancelled:</p>
 *
 * <pre>{@code
 * PlotNotificationIntentEvent intent = new PlotNotificationIntentEvent(
 *         plot.id(), PlotNotificationReason.SUBMITTED, plot);
 * Bukkit.getPluginManager().callEvent(intent);
 * if (intent.isCancelled()) {
 *     return;
 * }
 * // deliver notification
 * }</pre>
 *
 * <p>Prefer config toggles for permanent disablement; use cancel for dynamic
 * third-party veto. Do not make lifecycle events cancellable for this purpose —
 * that would couple notification preference to the whole side-effect chain.</p>
 */
public final class PlotNotificationIntentEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final int plotId;
    private final PlotNotificationReason reason;
    private final PlotSnapshot plot;
    private boolean cancelled;

    /**
     * @param plotId plot the notification concerns
     * @param reason why the integration intends to notify
     * @param plot   optional snapshot when the caller already has one; may be
     *               {@code null}. When non-null, {@code plot.id()} must equal
     *               {@code plotId}
     */
    public PlotNotificationIntentEvent(
            int plotId,
            @NotNull PlotNotificationReason reason,
            @Nullable PlotSnapshot plot
    ) {
        this.plotId = plotId;
        this.reason = Objects.requireNonNull(reason, "reason");
        this.plot = plot;
        if (plot != null && plot.id() != plotId) {
            throw new IllegalArgumentException(
                    "plot.id() " + plot.id() + " does not match plotId " + plotId);
        }
    }

    public PlotNotificationIntentEvent(int plotId, @NotNull PlotNotificationReason reason) {
        this(plotId, reason, null);
    }

    public PlotNotificationIntentEvent(
            @NotNull PlotSnapshot plot,
            @NotNull PlotNotificationReason reason
    ) {
        this(Objects.requireNonNull(plot, "plot").id(), reason, plot);
    }

    public int getPlotId() {
        return plotId;
    }

    public @NotNull PlotNotificationReason getReason() {
        return reason;
    }

    /**
     * Snapshot supplied by the notifier, or {@code null} when only the plot id
     * is known. Listeners that need full plot data may look up
     * {@link com.alpsbte.plotsystem.api.provider.PlotSnapshotProvider}.
     */
    public @Nullable PlotSnapshot getPlot() {
        return plot;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
