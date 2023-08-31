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

package com.alpsbte.plotsystem.commands.plot;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.commands.SubCommand;
import com.alpsbte.plotsystem.core.menus.PlotMemberMenu;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.logging.Level;

public class CMD_Plot_Members extends SubCommand {

    public CMD_Plot_Members(BaseCommand baseCommand) {
        super(baseCommand);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        try {
            if (getPlayer(sender) != null) {
                Plot plot;
                // Get Plot
                if (args.length > 0 && AlpsUtils.TryParseInt(args[0]) != null) {
                    //plot members <id>
                    int plotID = Integer.parseInt(args[0]);
                    if (PlotManager.plotExists(plotID)) {
                        plot = new Plot(plotID);
                    } else {
                        sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("This plot does not exist!"));
                        return;
                    }
                } else if (PlotManager.isPlotWorld(getPlayer(sender).getWorld())) {
                    //plot members
                    plot = PlotManager.getCurrentPlot(Builder.byUUID(getPlayer(sender).getUniqueId()), Status.unfinished, Status.unreviewed);
                } else {
                    sendInfo(sender);
                    return;
                }

                if(plot.getPlotOwner().getUUID().equals(getPlayer(sender).getUniqueId()) || getPlayer(sender).hasPermission("plotsystem.admin")) {
                    new PlotMemberMenu(plot,getPlayer(sender));
                } else {
                    sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("You don't have permission to manage this plot's members!"));
                }
            } else {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "This command can only be used as a player!");
            }
        } catch (SQLException ex) {
            sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("An error occurred while executing command!"));
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
    }

    @Override
    public String[] getNames() {
        return new String[] { "members" };
    }

    @Override
    public String getDescription() {
        return "Opens the plot member menu.";
    }

    @Override
    public String[] getParameter() {
        return new String[] { "ID" };
    }

    @Override
    public String getPermission() {
        return "plotsystem.plot.members";
    }
}
