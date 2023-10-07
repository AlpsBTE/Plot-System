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

import com.alpsbte.plotsystem.core.system.tutorial.stage.StageTimeline;
import com.alpsbte.plotsystem.core.system.tutorial.TutorialEventListener;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import static net.md_5.bungee.api.ChatColor.*;

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

    private final String assignmentMessage;
    private int completedAssignments;
    private final int totalAssignments;
    private boolean isDone;

    /**
     * This constructor is used if the task has no progress.
     * @param player The player who is doing the task.
     */
    public AbstractTask(Player player) {
        this(player, null, 0);
    }

    /**
     * This constructor is used if the task has progress.
     * @param player The player who is doing the task.
     * @param assignmentMessage The message which is displayed in the action bar and chat.
     * @param totalAssignments The total assignment progress which is needed to complete the task.
     */
    public AbstractTask(Player player, String assignmentMessage, int totalAssignments) {
        this.player = player;
        this.assignmentMessage = assignmentMessage;
        this.totalAssignments = totalAssignments;
    }

    /**
     * This method executes the logic of the task one time.
     * @see StageTimeline#StartTimeline() for more information.
     */
    public abstract void performTask();

    /**
     * Call this method when the task is done.
     * If the task is not set to done, the stage timeline won't continue.
     */
    public void setTaskDone() {
        if (isTaskDone()) return;
        this.isDone = true;
        TutorialEventListener.runningEventTasks.remove(player.getUniqueId().toString());
        for (int i = 0; i < StageTimeline.getActiveTimelines().size(); i++) StageTimeline.getActiveTimelines().get(i).onTaskDone(player.getUniqueId(), this);
    }

    /**
     * If the task has assignments, call this method to mark one assigment as completed.
     */
    protected void updateAssignments() {
        if (completedAssignments + 1 <= totalAssignments) {
            completedAssignments++;
        }
        for (int i = 0; i < StageTimeline.getActiveTimelines().size(); i++) StageTimeline.getActiveTimelines().get(i).onAssignmentUpdate(player.getUniqueId(), this);
    }

    /**
     * Gets the message which is displayed in the action bar and chat.
     * @return assignmentMessage
     */
    public String getAssignmentMessage() {
        return YELLOW + assignmentMessage;
    }

    /**
     * Gets the current number of completed assignments. If there are no assignments, it will return 0.
     * @return completedAssignments
     */
    public int getCompletedAssignments() {
        return completedAssignments;
    }

    /**
     * Gets the total number of assignments for the task. If there are no assignments, it will return 0.
     * @return totalAssignments
     */
    public int getTotalAssignments() {
        return totalAssignments;
    }

    /**
     * Checks if the task is done.
     * @return isDone
     */
    public boolean isTaskDone() {
        return isDone;
    }

    /**
     * Check if the task has assignments.
     * @return true if there are assignments, otherwise false.
     */
    public boolean hasAssignments() {
        return totalAssignments > 0;
    }



    /**
     * Sends the assignment message to the player via chat.
     * @param player The player who is doing the task.
     * @param assignmentMessage The message which is displayed in the action bar and chat.
     */
    public static void sendAssignmentMessage(Player player, String assignmentMessage) {
        player.sendMessage("");
        player.sendMessage("§8[§6PS§8] " + assignmentMessage);
        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
    }
}
