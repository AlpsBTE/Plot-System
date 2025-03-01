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

import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorialHologram;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.AbstractTask;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;

public abstract class AbstractStage {
    private final Player player;
    private final int id;
    private final int initWorldIndex;

    private final String title;
    private final List<String> messages;
    private final List<String> tasks;
    private final List<AbstractTutorialHologram> holograms;

    protected AbstractStage(Player player, int id, int initWorldIndex) {
        this.player = player;
        this.id = id;
        this.initWorldIndex = initWorldIndex;

        this.title = setTitle();
        this.messages = setMessages();
        this.tasks = setTasks();
        this.holograms = setHolograms();
    }

    /**
     * Sets the title of the tutorial stage
     *
     * @return title
     */
    protected abstract String setTitle();

    /**
     * Sets the messages of the tutorial stage
     *
     * @return messages
     */
    protected abstract List<String> setMessages();

    /**
     * Sets the tasks of the tutorial stage
     *
     * @return tasks
     * @see AbstractTask#getAssignmentMessage()
     */
    protected abstract List<String> setTasks();

    /**
     * Sets the tutorial holograms for the tutorial stage
     *
     * @return holograms
     */
    protected abstract List<AbstractTutorialHologram> setHolograms();

    /**
     * Gets the timeline of the tutorial stage
     *
     * @return timeline
     */
    public abstract StageTimeline getTimeline() throws IOException;

    public Player getPlayer() {
        return player;
    }

    public int getId() {
        return id;
    }

    public int getInitWorldIndex() {
        return initWorldIndex;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getMessages() {
        return messages;
    }

    public List<String> getTasks() {
        return tasks;
    }

    public List<AbstractTutorialHologram> getHolograms() {
        return holograms;
    }
}
