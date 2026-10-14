package com.alpsbte.plotsystem.api.event;

import com.alpsbte.plotsystem.api.model.PlotAbandonReason;
import com.alpsbte.plotsystem.api.model.PlotSnapshot;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * Fired after a plot has been successfully archived (abandoned).
 */
public final class PlotArchivedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final PlotSnapshot plot;
    private final PlotAbandonReason source;
    private final UUID actor;

    public PlotArchivedEvent(
            @NotNull PlotSnapshot plot,
            @NotNull PlotAbandonReason source,
            @Nullable UUID actor
    ) {
        this.plot = Objects.requireNonNull(plot, "plot");
        this.source = Objects.requireNonNull(source, "source");
        this.actor = actor;
    }

    public @NotNull PlotSnapshot getPlot() {
        return plot;
    }

    public @NotNull PlotAbandonReason getSource() {
        return source;
    }

    public @Nullable UUID getActor() {
        return actor;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
