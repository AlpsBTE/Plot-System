package com.alpsbte.plotsystem.api.event;

import com.alpsbte.plotsystem.api.model.PlotSnapshot;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Objects;

public final class PlotInactivityWarningEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final PlotSnapshot plot;
    private final LocalDate abandonmentDate;

    public PlotInactivityWarningEvent(@NotNull PlotSnapshot plot, @NotNull LocalDate abandonmentDate) {
        this.plot = Objects.requireNonNull(plot, "plot");
        this.abandonmentDate = Objects.requireNonNull(abandonmentDate, "abandonmentDate");
    }

    public @NotNull PlotSnapshot getPlot() {
        return plot;
    }

    public @NotNull LocalDate getAbandonmentDate() {
        return abandonmentDate;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
