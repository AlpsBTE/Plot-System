package com.alpsbte.plotsystem.core.system.tutorial.tasks.events;

import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.Tutorial;
import com.alpsbte.plotsystem.core.system.tutorial.TutorialEventListener;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.AbstractTask;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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
    public void performEvent(Event event) {
        if (event instanceof PlayerInteractEntityEvent && checkForNPC((PlayerInteractEntityEvent) event)) {
            updateProgress();
            setTaskDone();
        }
    }

    public static boolean checkForNPC(PlayerInteractEntityEvent event) {
        Tutorial tutorial = AbstractTutorial.getTutorialByPlayer(event.getPlayer());
        if (tutorial == null) return false;
        if (tutorial.getNPC().tutorialNPC != null && tutorial.getNPC().tutorialNPC.getUniqueId().equals(event.getRightClicked().getUniqueId())) {
            event.setCancelled(true);
            return true;
        }
        return false;
    }
}
