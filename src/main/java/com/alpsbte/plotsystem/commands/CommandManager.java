package com.alpsbte.plotsystem.commands;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.admin.CMD_DeletePlot;
import com.alpsbte.plotsystem.commands.admin.CMD_PReload;
import com.alpsbte.plotsystem.commands.admin.CMD_SetLeaderboard;
import com.alpsbte.plotsystem.commands.admin.setup.CMD_Setup;
import com.alpsbte.plotsystem.commands.plot.CMD_Plot;
import com.alpsbte.plotsystem.commands.review.CMD_EditFeedback;
import com.alpsbte.plotsystem.commands.review.CMD_EditPlot;
import com.alpsbte.plotsystem.commands.review.CMD_Review;
import com.alpsbte.plotsystem.commands.review.CMD_UndoReview;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommandManager {
    public final List<BaseCommand> baseCommands = new ArrayList<>();
    
    public CommandManager() {
        // Default Commands
        baseCommands.add(new CMD_CancelChat());
        baseCommands.add(new CMD_Companion());
        baseCommands.add(new CMD_Plots());
        baseCommands.add(new CMD_Tpll());

        // Plot Commands
        baseCommands.add(new CMD_Plot());

        // Review Commands
        baseCommands.add(new CMD_Review());
        baseCommands.add(new CMD_UndoReview());
        baseCommands.add(new CMD_EditFeedback());
        if (ConfigUtil.getInstance().configs[1].getBoolean(ConfigPaths.EDITPLOT_ENABLED)) {
            baseCommands.add(new CMD_EditPlot());
        }

        // Admin Commands
        baseCommands.add(new CMD_DeletePlot());
        baseCommands.add(new CMD_SetLeaderboard());
        baseCommands.add(new CMD_PReload());

        // Admin Setup Commands
        baseCommands.add(new CMD_Setup());
        baseCommands.add(new CMD_Tutorial());
    }

    public void init() {
        for (BaseCommand baseCmd : baseCommands) {
            for (String baseName : baseCmd.getNames()) {
                Objects.requireNonNull(PlotSystem.getPlugin().getCommand(baseName)).setExecutor(baseCmd);
            }
        }
    }
}
