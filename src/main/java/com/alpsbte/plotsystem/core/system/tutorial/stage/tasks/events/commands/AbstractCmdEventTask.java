/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

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

    public AbstractCmdEventTask(Player player, String expectedCommand, Component assignmentMessage, int totalAssignments, boolean cancelCmdEvent) {
        this(player, expectedCommand, null, assignmentMessage, totalAssignments, cancelCmdEvent);
    }

    public AbstractCmdEventTask(Player player, String expectedCommand, String[] args1, Component assignmentMessage, int totalAssignments, boolean cancelCmdEvent) {
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
        if (event instanceof PlayerCommandPreprocessEvent cmdEvent) {
            if (cmdEvent.getMessage().toLowerCase().startsWith(expectedCommand.toLowerCase())) {
                if (isCancelCmdEvent) cmdEvent.setCancelled(true);

                // Check if the expected args are used
                String[] args = cmdEvent.getMessage().toLowerCase().replaceFirst(expectedCommand.toLowerCase(), "").trim().split(" ");
                if (args1 != null && args1.length > 0) {
                    if (args.length == 0 || Arrays.stream(args1).noneMatch(arg -> arg.equalsIgnoreCase(args[0]))) {
                        onCommand(false, args);
                        return;
                    }
                }
                onCommand(true, args);
            }
        }
    }
}
