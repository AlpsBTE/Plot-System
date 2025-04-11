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

import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.PlotTutorial;
import org.bukkit.entity.Player;

import java.io.IOException;

public class PlotSchematicPasteTask extends AbstractTask {
    private final int schematicId;

    public PlotSchematicPasteTask(Player player, int schematicId) {
        super(player);
        this.schematicId = schematicId;
    }

    @Override
    public void performTask() {
        PlotTutorial tutorial = (PlotTutorial) AbstractTutorial.getActiveTutorial(player.getUniqueId());
        if (tutorial != null) {
            try {
                tutorial.onPlotSchematicPaste(player.getUniqueId(), schematicId);
            } catch (IOException ex) {
                tutorial.onException(ex);
                return;
            }
        }
        setTaskDone();
    }
}
