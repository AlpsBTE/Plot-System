package com.alpsbte.plotsystem.api.event;

import com.alpsbte.plotsystem.core.system.plot.Plot;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class PlotSubmittedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Plot plot;

    public PlotSubmittedEvent(@NotNull Plot plot) {
        this.plot = plot;
    }

    public @NotNull Plot getPlot() {
        return plot;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
