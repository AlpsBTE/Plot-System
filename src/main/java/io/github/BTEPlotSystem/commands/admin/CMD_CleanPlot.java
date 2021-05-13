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

import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.core.system.plot.PlotGenerator;
import github.BTEPlotSystem.core.system.plot.PlotHandler;
import github.BTEPlotSystem.core.system.plot.PlotManager;
import github.BTEPlotSystem.utils.Utils;
import github.BTEPlotSystem.utils.enums.Status;
import org.apache.commons.multiverse.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

public class CMD_CleanPlot implements CommandExecutor {

    private Plot[] plots = new Plot[1];

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(sender instanceof Player) {
            if(sender.hasPermission("alpsbte.admin")) {
                Player player = (Player)sender;
                if(args.length == 1 && PlotManager.isPlotWorld(player.getWorld())) {
                    try {
                        plots[0] = PlotManager.getPlotByWorld(player.getWorld());
                    } catch (SQLException ex) {
                        Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                        player.sendMessage(Utils.getErrorMessageFormat("An error occurred! Please try again!"));
                    }
                } else if(args.length == 2 && Utils.TryParseInt(args[1]) != null) {
                    int plotID = Integer.parseInt(args[1]);
                    if (PlotManager.plotExists(plotID)) {
                        try {
                            plots[0] = new Plot(plotID);
                        } catch (SQLException ex) {
                            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                            player.sendMessage(Utils.getErrorMessageFormat("An error occurred! Please try again!"));
                        }
                    }
                } else if(args.length == 2 && args[1].equalsIgnoreCase("all")) {
                    try {
                        plots = PlotManager.getPlots().toArray(new Plot[0]);
                    } catch (SQLException ex) {
                        Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                        player.sendMessage(Utils.getErrorMessageFormat("An error occurred! Please try again!"));
                    }
                } else {
                    player.sendMessage(Utils.getErrorMessageFormat("Usage: /cleanplot, /cleanplot <ID>"));
                }

                if(plots[0] != null) {
                    switch (args[0].toLowerCase()) {
                        case "permission":
                            cleanUpPlotPermissions();
                            break;
                        case "files":
                            cleanUpPlotFiles();
                            break;
                        default:
                            return true;
                    }
                    player.sendMessage(Utils.getInfoMessageFormat("Successfully cleaned §6" + plots.length + " §aplots!"));
                    player.playSound(player.getLocation(), Utils.Done, 1, 1);
                }
            }
        }
        return true;
    }

    private void cleanUpPlotPermissions() {
        for(Plot plot : plots) {
            try {
                if(plot.getStatus() == Status.unfinished) {
                    plot.clearAllPerms().save();
                    ProtectedRegion region = plot.getPlotRegion();

                    // Add builder perms to plot
                    DefaultDomain owner = region.getOwners();
                    owner.addPlayer(plot.getBuilder().getUUID());
                    region.setOwners(owner);

                    // Refresh plot command permissions
                    region.setFlag(DefaultFlag.BLOCKED_CMDS, PlotGenerator.blockedCommandsNonBuilder);
                    region.setFlag(DefaultFlag.BLOCKED_CMDS.getRegionGroupFlag(), RegionGroup.NON_OWNERS);

                    region.setFlag(DefaultFlag.ALLOWED_CMDS, PlotGenerator.allowedCommandsBuilder);
                    region.setFlag(DefaultFlag.ALLOWED_CMDS.getRegionGroupFlag(), RegionGroup.OWNERS);

                    // Save & Unload plot
                    PlotHandler.unloadPlot(plot);
                }
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        }
    }

    private void cleanUpPlotFiles() {
        for(Plot plot : plots) {
            try {
                if(plot.getStatus() == Status.unclaimed) {
                    if(BTEPlotSystem.getMultiverseCore().getMVWorldManager().isMVWorld("P-" + plot.getID())) {
                        BTEPlotSystem.getMultiverseCore().getMVWorldManager().deleteWorld("P-" + plot.getID(), true, true);
                    }
                    BTEPlotSystem.getMultiverseCore().getMVWorldManager().removeWorldFromConfig("P-" + plot.getID());

                    FileUtils.deleteDirectory(new File(PlotHandler.getWorldGuardConfigPath(plot.getID())));
                    FileUtils.deleteDirectory(new File(PlotHandler.getMultiverseInventoriesConfigPath(plot.getID())));
                }
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            } catch (IOException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Could not clean up file!", ex);
            }
        }
    }
}
