package com.alpsbte.plotsystem.api.event;

import com.alpsbte.plotsystem.api.model.PlotAbandonReason;
import com.alpsbte.plotsystem.api.model.PlotSnapshot;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class PlotAbandonedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final PlotSnapshot plot;
    private final PlotAbandonReason reason;

    public PlotAbandonedEvent(@NotNull PlotSnapshot plot, @NotNull PlotAbandonReason reason) {
        this.plot = Objects.requireNonNull(plot, "plot");
        this.reason = Objects.requireNonNull(reason, "reason");
    }

    public @NotNull PlotSnapshot getPlot() {
        return plot;
    }

    public @NotNull PlotAbandonReason getReason() {
        return reason;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
