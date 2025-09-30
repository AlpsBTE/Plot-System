package com.alpsbte.plotsystem.core.system.tutorial.stage;

import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.AbstractTask;

public interface TutorialTimeline {

    /**
     * This method is called when a stage task is completed.
     * If there is no next task, the timeline will be stopped.
     *
     * @param task completed task
     */
    void onTaskDone(AbstractTask task);

    /**
     * This method is called when a player assignment is updated.
     * Assignments are not tasks and do not inherit from AbstractTask.
     * They are in a task and are used to track the player's progress.
     *
     * @param task task of updated assignment
     */
    void onAssignmentUpdate(AbstractTask task);

    /**
     * This method is called when the task timeline is stopped.
     * Every ongoing task will end and the tip holograms will be removed.
     */
    void onStopTimeLine();
}
