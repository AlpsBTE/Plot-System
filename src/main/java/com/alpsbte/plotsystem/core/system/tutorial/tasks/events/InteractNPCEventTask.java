package com.alpsbte.plotsystem.core.system.tutorial.tasks.events;

import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.Tutorial;
import com.alpsbte.plotsystem.core.system.tutorial.TutorialEventListener;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.AbstractTask;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class InteractNPCEventTask extends AbstractTask implements EventTask {
    public InteractNPCEventTask(Player player, String assignmentMessage) {
        super(player, assignmentMessage, 1);
    }

    @Override
    public void performTask() {
        TutorialEventListener.runningEventTasks.put(player.getUniqueId().toString(), this);
    }

    @Override
    public void performEvent(PlayerEvent event) {
        if (event instanceof PlayerInteractEntityEvent) {
            Tutorial tutorial = AbstractTutorial.getTutorialByPlayer(player);
            if (tutorial == null) return;
            PlayerInteractEntityEvent interactEvent = (PlayerInteractEntityEvent) event;
            if (tutorial.getNPC().tutorialNPC != null && tutorial.getNPC().tutorialNPC.getUniqueId().equals(interactEvent.getRightClicked().getUniqueId())) {
                interactEvent.setCancelled(true);
                setTaskDone();
            }
        }
    }
}
