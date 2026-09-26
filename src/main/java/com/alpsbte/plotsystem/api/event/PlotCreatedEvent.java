package com.alpsbte.plotsystem.api.event;

import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class PlotCreatedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final AbstractPlot plot;

    public PlotCreatedEvent(@NotNull AbstractPlot plot) {
        this.plot = plot;
    }

    public @NotNull AbstractPlot getPlot() {
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
