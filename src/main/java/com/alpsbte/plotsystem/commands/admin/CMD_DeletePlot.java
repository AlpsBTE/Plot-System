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

package com.alpsbte.plotsystem.commands.admin;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class CMD_DeletePlot extends BaseCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat("You don't have permission to use this command!"));
            return true;
        }

        if (!(args.length > 0 && AlpsUtils.tryParseInt(args[0]) != null)) {
            sendInfo(sender);
            return true;
        }

        int plotID = Integer.parseInt(args[0]);

        CompletableFuture.runAsync(() -> {
            Plot plot = DataProvider.PLOT.getPlotById(plotID);
            if (plot == null) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Could not find plot with ID #" + plotID + "!"));
                return;
            }

            sender.sendMessage(Utils.ChatUtils.getInfoFormat("Deleting plot..."));
            Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
                if (PlotUtils.Actions.deletePlot(plot)) {
                    sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully deleted plot with the ID §6#" + plotID + "§a!"));
                    if (getPlayer(sender) != null) getPlayer(sender).playSound(getPlayer(sender).getLocation(), Utils.SoundUtils.DONE_SOUND, 1f, 1f);
                } else sender.sendMessage(Utils.ChatUtils.getAlertFormat("An unexpected error has occurred!"));
            });
        });
        return true;
    }

    @Override
    public String[] getNames() {
        return new String[]{"deleteplot"};
    }

    @Override
    public String getDescription() {
        return "Delete a plot from the system.";
    }

    @Override
    public String[] getParameter() {
        return new String[]{"ID"};
    }

    @Override
    public String getPermission() {
        return "plotsystem.admin.deleteplot";
    }
}
