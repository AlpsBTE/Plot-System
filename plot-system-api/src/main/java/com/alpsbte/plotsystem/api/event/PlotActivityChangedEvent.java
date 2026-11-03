package com.alpsbte.plotsystem.api.event;

import com.alpsbte.plotsystem.api.model.PlotSnapshot;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Objects;

/**
 * Emitted on the server thread after a plot's activity time is persisted,
 * including when it is cleared. No event is emitted for a failed write.
 * The snapshot contains the updated activity and abandonment deadline.
 */
public final class PlotActivityChangedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final PlotSnapshot plot;
    private final Instant previousActivityAt;

    public PlotActivityChangedEvent(@NotNull PlotSnapshot plot, @Nullable Instant previousActivityAt) {
        this.plot = Objects.requireNonNull(plot, "plot");
        this.previousActivityAt = previousActivityAt;
    }

    public @NotNull PlotSnapshot getPlot() {
        return plot;
    }

    public @Nullable Instant getPreviousActivityAt() {
        return previousActivityAt;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
