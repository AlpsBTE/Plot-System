package com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.events;

import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.TutorialEventListener;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.AbstractTask;
import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class NpcInteractEventTask extends AbstractTask implements EventTask {
    private final int entityId;

    public NpcInteractEventTask(Player player, int entityId, Component assignmentMessage) {
        super(player, assignmentMessage, 1);
        this.entityId = entityId;
    }

    @Override
    public void performTask() {
        TutorialEventListener.runningEventTasks.put(player.getUniqueId().toString(), this);
    }

    @Override
    public void performEvent(Event event) {
        if (AbstractTutorial.isPlayerIsOnInteractCoolDown(player.getUniqueId())) return;
        if (event instanceof PlayerUseUnknownEntityEvent && entityId == ((PlayerUseUnknownEntityEvent) event).getEntityId()) {
            updateAssignments();
            setTaskDone();
        }
    }
}
