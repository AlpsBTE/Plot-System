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

package github.BTEPlotSystem.commands.admin;

import com.sk89q.worldedit.WorldEditException;
import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.core.system.plot.PlotManager;
import github.BTEPlotSystem.utils.Utils;
import github.BTEPlotSystem.utils.enums.Status;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

public class CMD_CleanPlot implements CommandExecutor {

    private Plot[] plots;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(sender instanceof Player) {
            if(sender.hasPermission("alpsbte.admin")) {
                Player player = (Player)sender;
                if(args.length == 0 && PlotManager.isPlotWorld(player.getWorld())) {
                    try {
                        plots[0] = PlotManager.getPlotByWorld(player.getWorld());
                    } catch (SQLException ex) {
                        Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                        player.sendMessage(Utils.getErrorMessageFormat("An error occurred! Please try again!"));
                    }
                } else if(args.length == 1 && Utils.TryParseInt(args[0]) != null) {
                    int plotID = Integer.parseInt(args[0]);
                    if (PlotManager.plotExists(plotID)) {
                        try {
                            plots[0] = new Plot(plotID);
                        } catch (SQLException ex) {
                            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                            player.sendMessage(Utils.getErrorMessageFormat("An error occurred! Please try again!"));
                        }
                    } else {
                        player.sendMessage(Utils.getErrorMessageFormat("This plot does not exist!"));
                    }
                } else if(args.length == 1 && args[0].equalsIgnoreCase("all")) {
                    try {
                        plots = PlotManager.getPlots().toArray(new Plot[0]);
                    } catch (SQLException ex) {
                        Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                        player.sendMessage(Utils.getErrorMessageFormat("An error occurred! Please try again!"));
                    }
                } else {
                    player.sendMessage(Utils.getErrorMessageFormat("Usage: /cleanplot, /cleanplot <ID>"));
                }

                if(plots != null) {
                    try {
                        checkForFinishedSchematic();
                    } catch (SQLException | IOException | WorldEditException ex) {
                        Bukkit.getLogger().log(Level.SEVERE, "An error occurred!", ex);
                        player.sendMessage(Utils.getErrorMessageFormat("An error occurred! Please try again!"));
                    }
                } else {
                    player.sendMessage(Utils.getErrorMessageFormat("Usage: /cleanplot, /cleanplot <ID>"));
                }
            }
        }
        return true;
    }

    private void checkForFinishedSchematic() throws SQLException, IOException, WorldEditException {
        for(Plot plot : plots) {
            if(plot.getStatus() == Status.complete && !plot.isPasted()) {
                if(plot.getFinishedSchematic().length() == 0) {
                    PlotManager.savePlotAsSchematic(plot);
                }
            }
        }
    }
}
