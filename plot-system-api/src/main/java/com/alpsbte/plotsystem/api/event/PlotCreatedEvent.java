package com.alpsbte.plotsystem.api.event;

import com.alpsbte.plotsystem.api.model.PlotSnapshot;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class PlotCreatedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final PlotSnapshot plot;

    public PlotCreatedEvent(@NotNull PlotSnapshot plot) {
        this.plot = plot;
    }

    public @NotNull PlotSnapshot getPlot() {
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
