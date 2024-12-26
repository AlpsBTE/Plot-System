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

package com.alpsbte.plotsystem.core.system.tutorial.stage.tasks;

import com.alpsbte.plotsystem.core.system.tutorial.TutorialEventListener;
import com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialUtils;
import com.alpsbte.plotsystem.core.system.tutorial.stage.StageTimeline;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public abstract class AbstractTask {
    @FunctionalInterface
    public interface TaskAction<T> {
        void performAction(T t);
    }

    @FunctionalInterface
    public interface BiTaskAction<T, R> {
        void performAction(T t, R r);
    }

    protected final Player player;

    private final Component assignmentMessage;
    private int completedAssignments;
    private final int totalAssignments;
    private boolean isDone;

    /**
     * This constructor is used if the task has no progress.
     *
     * @param player The player who is doing the task.
     */
    public AbstractTask(Player player) {
        this(player, null, 0);
    }

    /**
     * This constructor is used if the task has progress.
     *
     * @param player            The player who is doing the task.
     * @param assignmentMessage The message which is displayed in the action bar and chat.
     * @param totalAssignments  The total assignment progress which is needed to complete the task.
     */
    public AbstractTask(Player player, Component assignmentMessage, int totalAssignments) {
        this.player = player;
        this.assignmentMessage = assignmentMessage;
        this.totalAssignments = totalAssignments;
    }

    /**
     * This method executes the logic of the task one time.
     *
     * @see StageTimeline#StartTimeline() for more information.
     */
    public abstract void performTask();

    /**
     * Call this method when the task is done.
     * If the task is not set to be done, the stage timeline won't continue.
     */
    public void setTaskDone() {
        if (isTaskDone()) return;
        this.isDone = true;
        TutorialEventListener.runningEventTasks.remove(player.getUniqueId().toString());
        if (StageTimeline.getActiveTimelines().containsKey(player.getUniqueId()))
            StageTimeline.getActiveTimelines().get(player.getUniqueId()).onTaskDone(this);
    }

    /**
     * If the task has assignments, call this method to mark one assigment as completed.
     */
    protected void updateAssignments() {
        if (completedAssignments + 1 <= totalAssignments) {
            completedAssignments++;
        }
        if (StageTimeline.getActiveTimelines().containsKey(player.getUniqueId()))
            StageTimeline.getActiveTimelines().get(player.getUniqueId()).onAssignmentUpdate(this);
    }

    /**
     * Gets the message which is displayed in the action bar and chat.
     *
     * @return assignmentMessage
     */
    public Component getAssignmentMessage() {
        return assignmentMessage.color(NamedTextColor.YELLOW);
    }

    /**
     * Gets the current number of completed assignments. If there are no assignments, it will return 0.
     *
     * @return completedAssignments
     */
    public int getCompletedAssignments() {
        return completedAssignments;
    }

    /**
     * Gets the total number of assignments for the task. If there are no assignments, it will return 0.
     *
     * @return totalAssignments
     */
    public int getTotalAssignments() {
        return totalAssignments;
    }

    /**
     * Checks if the task is done.
     *
     * @return isDone
     */
    public boolean isTaskDone() {
        return isDone;
    }

    /**
     * Check if the task has assignments.
     *
     * @return true if there are assignments, otherwise false.
     */
    public boolean hasAssignments() {
        return totalAssignments > 0;
    }


    /**
     * Sends the assignment message to the player via chat.
     *
     * @param player            The player who is doing the task.
     * @param assignmentMessage The message which is displayed in the action bar and chat.
     */
    public static void sendAssignmentMessage(Player player, Component assignmentMessage) {
        player.sendMessage(Component.text());
        player.sendMessage(TutorialUtils.CHAT_TASK_PREFIX_COMPONENT.append(assignmentMessage));
        player.sendMessage(Component.text());
        player.playSound(player.getLocation(), TutorialUtils.Sound.ASSIGNMENT_START, 1, 1);
    }
}