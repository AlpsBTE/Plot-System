package com.alpsbte.plotsystem.api.event;

import com.alpsbte.plotsystem.api.model.ReviewSnapshot;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class PlotReviewUndoneEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final ReviewSnapshot review;

    public PlotReviewUndoneEvent(@NotNull ReviewSnapshot review) {
        this.review = review;
    }

    public @NotNull ReviewSnapshot getReview() {
        return review;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
