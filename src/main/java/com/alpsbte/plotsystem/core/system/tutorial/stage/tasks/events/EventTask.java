package com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.events;

import org.bukkit.event.Event;

public interface EventTask {
    /**
     * Performs the event check in the given task
     *
     * @param event The event to check
     */
    void performEvent(Event event);
}
