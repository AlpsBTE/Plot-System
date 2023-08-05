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

package com.alpsbte.plotsystem.core.system.tutorial.stage;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.*;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.events.ChatEventTask;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.events.TeleportPointEventTask;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.events.commands.ContinueCmdEventTask;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.message.ChatMessageTask;
import com.sk89q.worldedit.Vector;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class StageTimeline {
    private final Player player;
    private final TutorialPlot plot;

    public List<AbstractTask> tasks = new ArrayList<>();
    private AbstractTask currentTask;
    private BukkitTask timelineTask;

    public void StartTimeline() {
        timelineTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < tasks.size(); i++) {
                    currentTask = tasks.get(i);
                    Bukkit.getLogger().log(Level.INFO, "Starting task " + currentTask.toString() + " [" + (i + 1) + " of " + tasks.size() + "] for player " + player.getName());
                    currentTask.performTask();

                    BukkitTask task = new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (timelineTask.isCancelled()) {
                                currentTask.setTaskDone();
                                this.cancel();
                            } else if (currentTask.isTaskDone()) this.cancel();
                        }
                    }.runTaskTimerAsynchronously(PlotSystem.getPlugin(), 0, 0); // TODO: Fix performance issue with 0 tick delay

                    int lastProgress = -1;
                    while (true) {
                        // Check if task has progress and if yes, update hologram footer
                        if (currentTask.hasProgress() && currentTask.getProgress() > lastProgress) {
                            lastProgress = currentTask.getProgress();
                            // hologram.updateProgress(currentTask.getProgress(), currentTask.getTotalProgress());
                            continue;
                        }

                        if (timelineTask.isCancelled()) return;
                        if (task.isCancelled()) {
                            final int lastTaskId = i;
                            AbstractTutorial.activeTutorials.forEach(t -> t.onTaskDone(player, lastTaskId));
                            break;
                        }
                    }
                }
            }
        }.runTaskAsynchronously(PlotSystem.getPlugin());
    }

    public void StopTimeline() {
        if (timelineTask != null) timelineTask.cancel();
    }

    public StageTimeline(Player player, TutorialPlot plot) {
        this.player = player;
        this.plot = plot;
    }

    public StageTimeline addTask(AbstractTask task) {
        tasks.add(task);
        return this;
    }

    public StageTimeline playSound(Sound sound, float volume, float pitch) {
        tasks.add(new PlaySoundTask(player, sound, volume, pitch));
        return this;
    }

    public StageTimeline sendChatMessage(String message, Sound soundEffect, boolean waitToContinue) {
        return sendChatMessage(new Object[] {message} , soundEffect, waitToContinue);
    }

    public StageTimeline sendChatMessage(ChatMessageTask.ClickableTaskMessage message, Sound soundEffect, boolean waitToContinue) {
        return sendChatMessage(new Object[] {message} , soundEffect, waitToContinue);
    }

    public StageTimeline sendChatMessage(Object[] messages, Sound soundEffect, boolean waitToContinue) {
        tasks.add(new ChatMessageTask(player, messages, soundEffect, waitToContinue));
        if (waitToContinue) tasks.add(new ContinueCmdEventTask(player)); // Add task to wait for player to continue
        return this;
    }

    public StageTimeline addTeleportEvent(List<Vector> teleportPoint, int offsetRange, AbstractTask.TaskAction<Vector> onTeleportAction) {
        tasks.add(new TeleportPointEventTask(player, teleportPoint, offsetRange, onTeleportAction));
        return this;
    }

    public StageTimeline addPlayerChatEvent(int expectedValue, int offset, int maxAttempts, AbstractTask.BiTaskAction<Boolean, Integer> onChatAction) {
        tasks.add(new ChatEventTask(player, expectedValue, offset, maxAttempts, onChatAction));
        return this;
    }

    public StageTimeline teleport(int tutorialWorldIndex) {
        tasks.add(new TeleportTask(player, tutorialWorldIndex));
        return this;
    }

    public StageTimeline teleport(Location location) {
        tasks.add(new TeleportTask(player, location));
        return this;
    }

    public StageTimeline pasteSchematicOutline(int schematicId) {
        tasks.add(new PasteSchematicOutlinesTask(player, schematicId));
        return this;
    }

    public StageTimeline delay(long seconds) {
        tasks.add(new WaitTask(null, seconds));
        return this;
    }
}
