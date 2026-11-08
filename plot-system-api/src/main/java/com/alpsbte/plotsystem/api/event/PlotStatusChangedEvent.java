package com.alpsbte.plotsystem.api.event;

import com.alpsbte.plotsystem.api.model.PlotSnapshot;
import com.alpsbte.plotsystem.api.model.PlotStatus;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Emitted on the server thread after a persisted status transition. This covers
 * transitions outside the high-level lifecycle events, such as reopening a plot.
 * It is not a signal that a multi-step operation has completed.
 */
public final class PlotStatusChangedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final PlotSnapshot plot;
    private final PlotStatus previousStatus;

    public PlotStatusChangedEvent(@NotNull PlotSnapshot plot, @NotNull PlotStatus previousStatus) {
        this.plot = Objects.requireNonNull(plot, "plot");
        this.previousStatus = Objects.requireNonNull(previousStatus, "previousStatus");
    }

    public @NotNull PlotSnapshot getPlot() {
        return plot;
    }

    public @NotNull PlotStatus getPreviousStatus() {
        return previousStatus;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
