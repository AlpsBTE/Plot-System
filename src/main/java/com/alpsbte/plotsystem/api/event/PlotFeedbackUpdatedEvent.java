package com.alpsbte.plotsystem.api.event;

import com.alpsbte.plotsystem.core.system.review.PlotReview;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class PlotFeedbackUpdatedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final PlotReview review;

    public PlotFeedbackUpdatedEvent(@NotNull PlotReview review) {
        this.review = review;
    }

    public @NotNull PlotReview getReview() {
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
