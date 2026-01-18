package com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.events.commands;

import com.alpsbte.plotsystem.core.system.tutorial.TutorialEventListener;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.AbstractTask;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.events.EventTask;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;

public abstract class AbstractCmdEventTask extends AbstractTask implements EventTask {
    protected final String expectedCommand;
    protected final String[] args1;

    private final boolean isCancelCmdEvent;

    protected AbstractCmdEventTask(Player player, String expectedCommand, Component assignmentMessage, int totalAssignments, boolean cancelCmdEvent) {
        this(player, expectedCommand, null, assignmentMessage, totalAssignments, cancelCmdEvent);
    }

    protected AbstractCmdEventTask(Player player, String expectedCommand, String[] args1, Component assignmentMessage, int totalAssignments, boolean cancelCmdEvent) {
        super(player, assignmentMessage, totalAssignments);
        this.expectedCommand = expectedCommand;
        this.args1 = args1;
        this.isCancelCmdEvent = cancelCmdEvent;
    }

    @Override
    public void performTask() {
        TutorialEventListener.runningEventTasks.put(player.getUniqueId().toString(), this);
    }

    protected abstract void onCommand(boolean isValid, String[] args);

    @Override
    public void performEvent(Event event) {
        if (event instanceof PlayerCommandPreprocessEvent cmdEvent && cmdEvent.getMessage().toLowerCase().startsWith(expectedCommand.toLowerCase())) {
                if (isCancelCmdEvent) cmdEvent.setCancelled(true);

                // Check if the expected args are used
                String[] args = cmdEvent.getMessage().toLowerCase().replaceFirst(expectedCommand.toLowerCase(), "").trim().split(" ");
                if (args1 != null && args1.length > 0 && (args.length == 0 || Arrays.stream(args1).noneMatch(arg -> arg.equalsIgnoreCase(args[0])))) {
                        onCommand(false, args);
                        return;
                    }

            onCommand(true, args);
            }

    }
}
