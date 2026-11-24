package com.alpsbte.plotsystem.api.event;

import com.alpsbte.plotsystem.api.model.PlotAbandonReason;
import com.alpsbte.plotsystem.api.model.PlotSnapshot;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public final class PlotAbandonedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final PlotSnapshot plot;
    private final PlotAbandonReason reason;
    private final UUID actor;

    public PlotAbandonedEvent(@NotNull PlotSnapshot plot, @NotNull PlotAbandonReason reason, @Nullable UUID actor) {
        this.plot = Objects.requireNonNull(plot, "plot");
        this.reason = Objects.requireNonNull(reason, "reason");
        this.actor = actor;
    }

    public @NotNull PlotSnapshot getPlot() {
        return plot;
    }

    public @NotNull PlotAbandonReason getReason() {
        return reason;
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
