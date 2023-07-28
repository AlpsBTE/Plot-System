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

package com.alpsbte.plotsystem.core.system.tutorial.tasks.events.worldedit;

import com.alpsbte.plotsystem.core.system.tutorial.TutorialEventListener;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.AbstractTask;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.events.EventTask;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;

public abstract class AbstractCmdEventTask extends AbstractTask implements EventTask {
    protected String expectedCommand;
    protected String[] args1;

    private final boolean isCancelCmdEvent;

    public AbstractCmdEventTask(Player player, String expectedCommand, int totalProgress, boolean cancelCmdEvent) {
        this(player, expectedCommand, new String[0], totalProgress, cancelCmdEvent);
    }

    public AbstractCmdEventTask(Player player, String expectedCommand, String[] args1, int totalProgress, boolean cancelCmdEvent) {
        super(player, totalProgress);
        this.expectedCommand = expectedCommand;
        this.args1 = args1;
        this.isCancelCmdEvent = cancelCmdEvent;
    }

    @Override
    public void performTask() {
        TutorialEventListener.runningEventTasks.put(player.getUniqueId().toString(), this);
    }

    protected abstract boolean onCommand(String[] args);

    @Override
    public void performEvent(PlayerEvent event) {
        if (event instanceof PlayerCommandPreprocessEvent) {
            PlayerCommandPreprocessEvent cmdEvent = (PlayerCommandPreprocessEvent) event;
            if (cmdEvent.getMessage().toLowerCase().startsWith(expectedCommand.toLowerCase())) {
                onCommand(cmdEvent.getMessage().replaceFirst(expectedCommand, "").trim().split(" "));
                if (isCancelCmdEvent) cmdEvent.setCancelled(true);
            }
        }
    }
}
