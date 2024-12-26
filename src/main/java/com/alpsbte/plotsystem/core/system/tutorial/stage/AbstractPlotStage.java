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

import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import org.bukkit.entity.Player;

public abstract class AbstractPlotStage extends AbstractStage {
    private final TutorialPlot plot;
    private final int initSchematicId;

    protected AbstractPlotStage(Player player, int initWorldIndex, TutorialPlot plot, int initSchematicId) {
        super(player, plot.getID(), initWorldIndex);
        this.plot = plot;
        this.initSchematicId = initSchematicId;
    }

    public TutorialPlot getPlot() {
        return plot;
    }

    public int getInitSchematicId() {
        return initSchematicId;
    }
}
