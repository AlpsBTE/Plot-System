package com.alpsbte.plotsystem.api.event;

import com.alpsbte.plotsystem.core.system.plot.Plot;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Objects;

public final class PlotInactivityWarningEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Plot plot;
    private final LocalDate abandonmentDate;

    public PlotInactivityWarningEvent(@NotNull Plot plot, @NotNull LocalDate abandonmentDate) {
        this.plot = Objects.requireNonNull(plot, "plot");
        this.abandonmentDate = Objects.requireNonNull(abandonmentDate, "abandonmentDate");
    }

    public @NotNull Plot getPlot() {
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
