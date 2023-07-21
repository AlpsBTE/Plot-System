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

package com.alpsbte.plotsystem.core.system.tutorial;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.holograms.TutorialHologram;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.*;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.events.TeleportPointEventTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class StageTimeline {
    private final Player player;
    private final TutorialHologram hologram;

    public List<AbstractTask> tasks = new ArrayList<>();
    private AbstractTask currentTask;
    public int lastTaskId;

    public void StartTimeline() throws InterruptedException {
        Bukkit.getScheduler().runTaskAsynchronously(PlotSystem.getPlugin(), () -> {
            AtomicBoolean isLastTask = new AtomicBoolean(false);
            boolean isActionStarted = false;

            for (int i = 0; i < tasks.size(); i++) {
                currentTask = tasks.get(i);
                Bukkit.getLogger().log(Level.INFO, "Starting task " + currentTask.toString() + " [" + (i + 1) + " of " + tasks.size() + "] for player " + player.getName());
                currentTask.performTask();

                BukkitTask task = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (currentTask.isTaskDone()) this.cancel();
                    }
                }.runTaskTimerAsynchronously(PlotSystem.getPlugin(), 0, 0);

                while (true) {
                    if (task.isCancelled()) {
                        if (i == tasks.size() - 1) {
                            if (!isActionStarted) {
                                hologram.onFooterClickEvent(action -> isLastTask.set(true));
                                isActionStarted = true;
                                continue;
                            } else if (!isLastTask.get()) continue;
                        }
                        lastTaskId = i;
                        break;
                    }
                }
            }
        });
    }

    public StageTimeline(Player player, TutorialHologram hologram) {
        this.player = player;
        this.hologram = hologram;
    }

    public StageTimeline addTask(AbstractTask task) {
        tasks.add(task);
        return this;
    }

    public StageTimeline updateHologramContent(List<String> content, Sound soundEffect) {
        tasks.add(new HologramMessageTask(player, hologram, soundEffect, content));
        return this;
    }

    public StageTimeline sendChatMessage(String message, Sound soundEffect) {
        tasks.add(new ChatMessageTask(player, message, soundEffect));
        return this;
    }

    public StageTimeline teleportPlayer(Location location) {
        tasks.add(new TeleportPlayerTask(player, location));
        return this;
    }

    public StageTimeline addTeleportEvent(Player player, List<double[]> teleportPoint, int offsetRange, TeleportPointEventTask.ITeleportPointAction onTeleportAction) {
        tasks.add(new TeleportPointEventTask(player, teleportPoint, offsetRange, onTeleportAction));
        return this;
    }

    public StageTimeline delay(long seconds) {
        tasks.add(new WaitTask(null, seconds));
        return this;
    }
}
