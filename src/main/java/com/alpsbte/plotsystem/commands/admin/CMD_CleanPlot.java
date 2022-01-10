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

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class CMD_CleanPlot extends BaseCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        List<Plot> plots = new ArrayList<>();
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(Utils.getErrorMessageFormat("You don't have permission to use this command!"));
            return true;
        }

        // Get plot(s)
        try {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("all")) {
                    plots.addAll(PlotManager.getPlots(Status.unfinished, Status.unreviewed));
                } else if (Utils.TryParseInt(args[0]) != null) {
                    int plotID = Integer.parseInt(args[0]);
                    if (PlotManager.plotExists(plotID)) {
                        plots.add(new Plot(plotID));
                    } else {
                        sender.sendMessage(Utils.getErrorMessageFormat("Could not find plot with ID #" + plotID + "!"));
                        return true;
                    }
                } else {
                    sendInfo(sender);
                    return true;
                }
            } else {
                sendInfo(sender);
                return true;
            }
        } catch (SQLException ex) {
            sender.sendMessage(Utils.getErrorMessageFormat("An error occurred while executing command!"));
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            return true;
        }

        // Clean plot(s)
        new BukkitRunnable() {
            int index = 0;
            int failed = 0;
            @Override
            public void run() {
                try {
                   cleanPlot(plots.get(index));
                } catch (Exception ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "An error occurred while cleaning plot #" + plots.get(index).getID() + "!", ex);
                    failed++;
                }

                if (index == plots.size() - 1) {
                    sender.sendMessage(Utils.getInfoMessageFormat("§aCleaned §f" + (plots.size() - failed) + " §aplot" + (plots.size() > 1 ? "s" : "") + ", §f" + failed + " §afailed!"));
                    if (sender instanceof Player) ((Player) sender).playSound(((Player) sender).getLocation(), Utils.Done, 1, 1);
                    cancel();
                } else {
                    index++;
                }
            }
        }.runTaskTimer(PlotSystem.getPlugin(), 0L, 40L);
        return true;
    }

    private void cleanPlot(Plot plot) {
        // TODO: Implement clean plot code
        // Waiting for commands rework to prevent copy paste code.
    }

    @Override
    public String[] getNames() {
        return new String[] { "cleanplot" };
    }

    @Override
    public String getDescription() {
        return "Clean up / Refresh a plot due to bugs or updates.";
    }

    @Override
    public String[] getParameter() {
        return new String[] { "All/ID" };
    }

    @Override
    public String getPermission() {
        return "plotsystem.admin.cleanplot";
    }
}
