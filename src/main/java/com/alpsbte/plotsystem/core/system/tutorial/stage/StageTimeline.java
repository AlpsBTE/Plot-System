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

import com.alpsbte.alpslib.hologram.HolographicDisplay;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.holograms.TutorialTipHologram;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.*;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.events.ChatEventTask;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.events.InteractNPCEventTask;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.events.TeleportPointEventTask;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.events.commands.ContinueCmdEventTask;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.hologram.PlaceHologramTask;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.hologram.RemoveHologramTask;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.message.ChatMessageTask;
import com.sk89q.worldedit.Vector;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import static net.md_5.bungee.api.ChatColor.GRAY;

public class StageTimeline implements TutorialTimeLine {
    public static List<TutorialTimeLine> activeTimelines = new ArrayList<>();

    private final List<AbstractTask> tasks = new ArrayList<>();
    private final Player player;
    private int currentTaskId = -1;
    private AbstractTask currentTask;
    private BukkitTask taskProgressTask;

    private final List<TutorialTipHologram> tipHolograms = new ArrayList<>();

    public StageTimeline(Player player) {
        this.player = player;
    }

    public void StartTimeline() {
        activeTimelines.add(this);
        nextTask();
    }

    private void nextTask() {
        currentTask = tasks.get(currentTaskId + 1);
        Bukkit.getLogger().log(Level.INFO, "Starting task " + currentTask.toString() + " [" + (currentTaskId + 2) + " of " + tasks.size() + "] for player " + player.getName());
        currentTaskId = currentTaskId + 1;

        // Check if task has progress and if yes, update action bar
        if (currentTask.hasProgress()) {
            AbstractTask.sendAssignmentMessage(player, currentTask.getAssignmentMessage());
            if (taskProgressTask != null) taskProgressTask.cancel();
            taskProgressTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (currentTask.getProgress() == currentTask.getTotalProgress()) {
                        this.cancel();
                    } else updatePlayerActionBar();
                }
            }.runTaskTimerAsynchronously(PlotSystem.getPlugin(), 0, 20);
        }

        currentTask.performTask();
    }

    @Override
    public void onTaskDone(UUID playerUUID, AbstractTask task) {
        if (!player.getUniqueId().toString().equals(playerUUID.toString()) && task != currentTask) return;
        if (!activeTimelines.contains(this)) return;

        if (currentTaskId >= tasks.size() - 1) {
            onStopTimeLine(playerUUID);
            for (int i = 0; i < AbstractTutorial.activeTutorials.size(); i++) AbstractTutorial.activeTutorials.get(i).onStageComplete(player.getUniqueId());
        } else nextTask();
    }

    @Override
    public void onAssignmentUpdate(UUID playerUUID, AbstractTask task) {
        if (!player.getUniqueId().toString().equals(playerUUID.toString()) && task != currentTask) return;
        updatePlayerActionBar();
    }

    @Override
    public void onStopTimeLine(UUID playerUUID) {
        if (!player.getUniqueId().toString().equals(playerUUID.toString())) return;

        activeTimelines.remove(this);
        if (taskProgressTask != null) taskProgressTask.cancel();
        if (currentTask != null) currentTask.setTaskDone();
        tipHolograms.forEach(HolographicDisplay::remove);
    }

    private void updatePlayerActionBar() {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(GRAY + " [" + currentTask.getProgress() + "/" + currentTask.getTotalProgress() + "] " + currentTask.getAssignmentMessage()));
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

    public StageTimeline addTeleportEvent(String assignmentMessage, List<Vector> teleportPoint, int offsetRange, AbstractTask.TaskAction<Vector> onTeleportAction) {
        tasks.add(new TeleportPointEventTask(player, assignmentMessage, teleportPoint, offsetRange, onTeleportAction));
        return this;
    }

    public StageTimeline addPlayerChatEvent(String assignmentMessage, int expectedValue, int offset, int maxAttempts, AbstractTask.BiTaskAction<Boolean, Integer> onChatAction) {
        tasks.add(new ChatEventTask(player, assignmentMessage, expectedValue, offset, maxAttempts, onChatAction));
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

    public StageTimeline interactNPC(String assignmentMessage) {
        tasks.add(new InteractNPCEventTask(player, assignmentMessage));
        return this;
    }

    public StageTimeline placeTipHologram(int tipId, String content) {
        PlaceHologramTask task = new PlaceHologramTask(player, tipId, content);
        tipHolograms.add(task.getHologram());
        tasks.add(task);
        return this;
    }

    public StageTimeline removeTipHologram(int tipId) {
        tasks.add(new RemoveHologramTask(player, tipId, tipHolograms));
        return this;
    }

    public StageTimeline removeTipHolograms() {
        for (int i = 0; i < tipHolograms.size(); i++) tasks.add(new RemoveHologramTask(player, Integer.parseInt(tipHolograms.get(i).getId()), tipHolograms));
        return this;
    }

    public StageTimeline delay(long seconds) {
        tasks.add(new WaitTask(player, seconds));
        return this;
    }
}
