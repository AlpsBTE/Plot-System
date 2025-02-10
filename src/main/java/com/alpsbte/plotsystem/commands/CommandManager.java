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

package com.alpsbte.plotsystem.commands;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.admin.CMD_DeletePlot;
import com.alpsbte.plotsystem.commands.admin.CMD_PReload;
import com.alpsbte.plotsystem.commands.admin.CMD_SetLeaderboard;
import com.alpsbte.plotsystem.commands.admin.setup.CMD_Setup;
import com.alpsbte.plotsystem.commands.plot.CMD_Plot;
import com.alpsbte.plotsystem.commands.review.CMD_EditPlot;
import com.alpsbte.plotsystem.commands.review.CMD_Review;
import com.alpsbte.plotsystem.commands.review.CMD_EditFeedback;
import com.alpsbte.plotsystem.commands.review.CMD_UndoReview;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommandManager {

    public final List<BaseCommand> baseCommands = new ArrayList<>() {{
        // Default Commands
        add(new CMD_CancelChat());
        add(new CMD_Companion());
        add(new CMD_Plots());
        add(new CMD_Tpll());

        // Plot Commands
        add(new CMD_Plot());

        // Review Commands
        add(new CMD_Review());
        add(new CMD_UndoReview());
        add(new CMD_EditFeedback());
        add(new CMD_EditPlot());

        // Admin Commands
        add(new CMD_DeletePlot());
        add(new CMD_SetLeaderboard());
        add(new CMD_PReload());

        // Admin Setup Commands
        add(new CMD_Setup());
        add(new CMD_Tutorial());
    }};

    public void init() {
        for (BaseCommand baseCmd : baseCommands) {
            for (String baseName : baseCmd.getNames()) {
                Objects.requireNonNull(PlotSystem.getPlugin().getCommand(baseName)).setExecutor(baseCmd);
            }
        }
    }

    public List<BaseCommand> getBaseCommands() {
        return baseCommands;
    }
}
