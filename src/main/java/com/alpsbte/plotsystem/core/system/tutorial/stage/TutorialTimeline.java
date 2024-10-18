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
