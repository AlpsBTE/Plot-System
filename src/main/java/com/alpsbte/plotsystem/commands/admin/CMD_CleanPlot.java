/*
 * The MIT License (MIT)
 *
 *  Copyright © 2021, Alps BTE <bte.atchli@gmail.com>
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

import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class CMD_CleanPlot implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        List<Plot> plots = new ArrayList<>();
        if (sender instanceof Player) {
            if (!sender.hasPermission("alpsbte.admin")) {
                return true;
            }
        }

        // Get plot(s)
        try {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("all")) {
                    plots.addAll(PlotManager.getPlots(Status.unclaimed, Status.unfinished, Status.unreviewed));
                } else if (Utils.TryParseInt(args[0]) != null) {
                    int plotID = Integer.parseInt(args[0]);
                    if (PlotManager.plotExists(plotID)) {
                        plots.add(new Plot(plotID));
                    } else {
                        sender.sendMessage(Utils.getErrorMessageFormat("Could not find plot with ID #" + plotID + "!"));
                        return true;
                    }
                } else {
                    sender.sendMessage(Utils.getErrorMessageFormat("§lUsage: §c/cleanplot <ID> or /cleanplot all"));
                    return true;
                }
            } else {
                sender.sendMessage(Utils.getErrorMessageFormat("§lUsage: §c/cleanplot <ID> or /cleanplot all"));
                return true;
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            return true;
        }

        // Clean plot(s)
        int failed = 0;
        for (Plot plot : plots) {
            try {
                cleanPlot(plot);
            } catch (Exception ex) {
                Bukkit.getLogger().log(Level.SEVERE, "An error occurred while cleaning plot #" + plot.getID() + "!", ex);
                failed++;
            }
        }

        sender.sendMessage(Utils.getInfoMessageFormat("§aCleaned §f" + (plots.size() - failed) + " §aplot" + (plots.size() > 1 ? "s" : "") + ", §f" + failed + " §afailed!"));
        if (sender instanceof Player) ((Player) sender).playSound(((Player) sender).getLocation(), Utils.Done, 1, 1);
        return true;
    }

    private void cleanPlot(Plot plot) {
        // TODO: Implement clean plot code
        // Waiting for commands rework to prevent copy paste code.
    }
}
