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
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorialHologram;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.*;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.events.ChatEventTask;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.events.NpcInteractEventTask;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.events.TeleportPointEventTask;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.events.commands.ContinueCmdEventTask;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.message.CreateHologramTask;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.message.DeleteHologramTask;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.message.ChatMessageTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public class StageTimeline implements TutorialTimeline {
    private static final Map<UUID, TutorialTimeline> activeTimelines = new HashMap<>();

    protected final Player player;

    protected final List<AbstractTask> tasks = new ArrayList<>();
    private AbstractTask currentTask;
    private int currentTaskId = -1;
    private BukkitTask assignmentProgressTask;
    private final AbstractTutorial tutorial;

    public StageTimeline(Player player) {
        this.player = player;
        tutorial = AbstractTutorial.getActiveTutorial(player.getUniqueId());
    }

    /**
     * This method starts the timeline.
     * It will start with the first task and add the timeline to the list of active timelines.
     */
    public void StartTimeline() {
        activeTimelines.put(player.getUniqueId(), this);
        nextTask();
    }

    /**
     * This method performs the next task in the timeline.
     * In addition, it keeps track of the task's assignment and updates the action bar if the task has progress.
     */
    private void nextTask() {
        try {
            currentTask = tasks.get(currentTaskId + 1);
            // Bukkit.getLogger().log(Level.INFO, "Starting task " + currentTask.toString() + " [" + (currentTaskId + 2) + " of " + tasks.size() + "] for player " + player.getName());
            currentTaskId = currentTaskId + 1;

            // Check if a task has progress and if yes, update action bar
            if (currentTask.hasAssignments()) {
                AbstractTask.sendAssignmentMessage(player, currentTask.getAssignmentMessage());
                if (assignmentProgressTask != null) assignmentProgressTask.cancel();
                assignmentProgressTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (currentTask.getCompletedAssignments() == currentTask.getTotalAssignments()) {
                            this.cancel();
                        } else updatePlayerActionBar();
                    }
                }.runTaskTimerAsynchronously(PlotSystem.getPlugin(), 0, 20);
            }

            // If the task has npc interaction show the hologram click info, otherwise hide it
            if (currentTask instanceof NpcInteractEventTask || currentTask instanceof ContinueCmdEventTask ||
                    (currentTask instanceof ChatMessageTask && ((ChatMessageTask) currentTask).isWaitToContinue())) {
                tutorial.getNPC().setInteractionPromptVisibility(player.getUniqueId(), true, currentTask instanceof NpcInteractEventTask);
            } else if (tutorial.getNPC().getHologram().isInteractionPromptVisible()) tutorial.getNPC().setInteractionPromptVisibility(player.getUniqueId(), false, false);

            currentTask.performTask();
        } catch (Exception ex) {
            tutorial.onException(ex);
        }
    }

    @Override
    public void onTaskDone(AbstractTask task) {
        if (task != currentTask || !activeTimelines.containsKey(player.getUniqueId())) return;

        if (currentTaskId >= tasks.size() - 1) {
            onStopTimeLine();
            tutorial.onStageComplete(player.getUniqueId());
        } else nextTask();
    }

    @Override
    public void onAssignmentUpdate(AbstractTask task) {
        if (task != currentTask) return;
        updatePlayerActionBar();
    }

    @Override
    public void onStopTimeLine() {
        activeTimelines.remove(player.getUniqueId());
        if (assignmentProgressTask != null) assignmentProgressTask.cancel();
        if (currentTask != null) currentTask.setTaskDone();
        tutorial.getActiveHolograms().forEach(AbstractTutorialHologram::delete);
        tutorial.getNPC().setInteractionPromptVisibility(player.getUniqueId(), false, false);
        tasks.clear();
    }

    /**
     * This method updates the player's action bar with the current task's assignment message.
     */
    private void updatePlayerActionBar() {
        player.sendActionBar(Component.text("[" + currentTask.getCompletedAssignments() + "/"
                + currentTask.getTotalAssignments() + "] ", NamedTextColor.GRAY).append(currentTask.getAssignmentMessage()));
    }

    /**
     * Adds a task to the timeline. If there is no function for a specific task available, use this method.
     *
     * @param task the task to be added
     */
    public StageTimeline addTask(AbstractTask task) {
        tasks.add(task);
        return this;
    }

    /**
     * Adds a ChatMessageTask to the timeline.
     *
     * @param message        the message to be sent
     * @param soundEffect    the sound effect to be played, can be null
     * @param waitToContinue whether the timeline should wait for the player to continue
     */
    public StageTimeline sendChatMessage(Component message, Sound soundEffect, boolean waitToContinue) {
        return sendChatMessage(new Object[]{message}, soundEffect, waitToContinue);
    }

    /**
     * Adds a ChatMessageTask to the timeline.
     *
     * @param message        the clickable message to be sent
     * @param soundEffect    the sound effect to be played, can be null
     * @param waitToContinue whether the timeline should wait for the player to continue
     */
    public StageTimeline sendChatMessage(ChatMessageTask.ClickableTaskMessage message, Sound soundEffect, boolean waitToContinue) {
        return sendChatMessage(new Object[]{message}, soundEffect, waitToContinue);
    }

    /**
     * Adds a ChatMessageTask to the timeline. Use this method if you want to send multiple messages at once.
     *
     * @param messages       the messages to be sent, can be a String or a ClickableTaskMessage
     * @param soundEffect    the sound effect to be played, can be null
     * @param waitToContinue whether the timeline should wait for the player to continue
     */
    public StageTimeline sendChatMessage(Object[] messages, Sound soundEffect, boolean waitToContinue) {
        tasks.add(new ChatMessageTask(player, messages, soundEffect, waitToContinue));
        // Add a task to wait for player to continue
        if (waitToContinue) tasks.add(new ContinueCmdEventTask(player, tutorial.getNPC().getNpc().getEntityId()));
        return this;
    }

    /**
     * Adds a TeleportPointEventTask to the timeline.
     *
     * @param assignmentMessage the task message to show in the action bar to the player
     * @param teleportPoint     a list of teleport points the player needs to teleport to
     * @param offsetRange       the range in which the player can teleport to the teleport point
     * @param onTeleportAction  the action to be performed when the player teleports to a teleport point
     */
    public StageTimeline addTeleportEvent(Component assignmentMessage, List<Vector> teleportPoint, int offsetRange, AbstractTask.BiTaskAction<Vector, Boolean> onTeleportAction) {
        tasks.add(new TeleportPointEventTask(player, assignmentMessage, teleportPoint, offsetRange, onTeleportAction));
        return this;
    }

    /**
     * Adds a ChatEventTask to the timeline.
     *
     * @param assignmentMessage the task message to show in the action bar to the player
     * @param expectedValue     the expected value the player needs to chat
     * @param offset            the offset in which the player can chat the expected value
     * @param maxAttempts       the maximum amount of attempts the player has to chat the expected value
     * @param onChatAction      the action to be performed when the player chats the expected value
     */
    public StageTimeline addPlayerChatEvent(Component assignmentMessage, int expectedValue, int offset, int maxAttempts, AbstractTask.BiTaskAction<Boolean, Integer> onChatAction) {
        tasks.add(new ChatEventTask(player, assignmentMessage, expectedValue, offset, maxAttempts, onChatAction));
        return this;
    }

    /**
     * Adds a TeleportTask to the timeline.
     *
     * @param tutorialWorldIndex the index of the tutorial world the player teleports to
     */
    public StageTimeline teleport(int tutorialWorldIndex) {
        tasks.add(new TeleportTask(player, tutorialWorldIndex));
        return this;
    }

    /**
     * Adds a InteractNPCEventTask to the timeline.
     *
     * @param assignmentMessage the task message to show in the action bar to the player
     */
    public StageTimeline interactNPC(Component assignmentMessage) {
        tasks.add(new NpcInteractEventTask(player, tutorial.getNPC().getNpc().getEntityId(), assignmentMessage));
        return this;
    }

    /**
     * Adds a CreateHologramTask to the timeline.
     * This function adds the task with no mark as read functionality.
     *
     * @param holograms holograms to show to the player, only the tutorial player can see them
     * @see AbstractStage#getHolograms()
     */
    public StageTimeline createHolograms(AbstractTutorialHologram... holograms) {
        tasks.add(new CreateHologramTask(player, new LinkedList<>(Arrays.asList(holograms))));
        return this;
    }

    /**
     * Adds a CreateHologramTask to the timeline.
     *
     * @param assignmentMessage the task message to show in the action bar to the player
     * @param holograms         holograms to show to the player, only the tutorial player can see them
     * @see AbstractStage#getHolograms()
     */
    public StageTimeline createHolograms(Component assignmentMessage, AbstractTutorialHologram... holograms) {
        tasks.add(new CreateHologramTask(player, assignmentMessage, new LinkedList<>(Arrays.asList(holograms)), true));
        return this;
    }

    /**
     * Adds a DeleteHologramTask to the timeline.
     * Deletes a specific tutorial hologram from the stage.
     *
     * @param hologram hologram to delete from the current stage
     * @see AbstractStage#getHolograms()
     */
    public StageTimeline deleteHologram(AbstractTutorialHologram hologram) {
        tasks.add(new DeleteHologramTask(player, Collections.singletonList(hologram)));
        return this;
    }

    /**
     * Adds a DeleteHologramTask to the timeline.
     * Deletes all tutorial holograms from the current stage.
     *
     * @see AbstractStage#getHolograms()
     */
    public StageTimeline deleteHolograms() {
        tasks.add(new DeleteHologramTask(player));
        return this;
    }

    /**
     * Adds a WaitTask to the timeline.
     *
     * @param seconds the number of seconds to wait
     */
    public StageTimeline delay(long seconds) {
        tasks.add(new DelayTask(player, seconds));
        return this;
    }


    /**
     * Gets the active tutorial timelines.
     *
     * @return timelines
     */
    public static Map<UUID, TutorialTimeline> getActiveTimelines() {
        return activeTimelines;
    }
}
