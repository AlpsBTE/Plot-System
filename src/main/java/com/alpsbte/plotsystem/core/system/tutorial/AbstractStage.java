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

import com.alpsbte.plotsystem.core.holograms.TutorialHologram;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public abstract class AbstractStage {
    protected final TutorialPlot plot;
    protected final Player player;
    protected final TutorialHologram hologram;

    private final List<String> messages;
    private final StageTimeline taskTimeline;
    public AbstractStage(TutorialPlot plot, TutorialHologram hologram) throws SQLException, IOException {
        this.plot = plot;
        this.player = plot.getPlotOwner().getPlayer();
        this.hologram = hologram;

        messages = setMessages();
        taskTimeline = setTasks();
    }

    protected abstract List<String> setMessages();
    protected abstract StageTimeline setTasks() throws IOException;

    public List<String> getMessages() {
        return messages;
    }
    public StageTimeline getTaskTimeline() {
        return taskTimeline;
    }
}
