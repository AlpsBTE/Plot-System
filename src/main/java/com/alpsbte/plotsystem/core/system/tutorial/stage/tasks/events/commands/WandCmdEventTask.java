package com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.events.commands;

import com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class WandCmdEventTask extends AbstractCmdEventTask {
    public WandCmdEventTask(Player player, Component assignmentMessage) {
        super(player, "//wand", assignmentMessage, 1, false);
    }

    @Override
    protected void onCommand(boolean isValid, String[] args) {
        if (!isValid) return;
        updateAssignments();
        player.playSound(player.getLocation(), TutorialUtils.Sound.ASSIGNMENT_COMPLETED, 1f, 1f);
        setTaskDone();
    }
}
