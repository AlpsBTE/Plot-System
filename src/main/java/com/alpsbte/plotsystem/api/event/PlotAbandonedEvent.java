package com.alpsbte.plotsystem.api.event;

import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class PlotAbandonedEvent extends Event {
    public enum Reason {
        INACTIVITY,
        MANUAL,
        COMMAND,
        SYSTEM
    }

    private static final HandlerList HANDLERS = new HandlerList();
    private final AbstractPlot plot;
    private final Reason reason;

    public PlotAbandonedEvent(@NotNull AbstractPlot plot, @NotNull Reason reason) {
        this.plot = Objects.requireNonNull(plot, "plot");
        this.reason = Objects.requireNonNull(reason, "reason");
    }

    public @NotNull AbstractPlot getPlot() {
        return plot;
    }

    public @NotNull Reason getReason() {
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
