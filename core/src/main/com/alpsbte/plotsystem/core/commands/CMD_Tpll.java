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

package com.alpsbte.plotsystem.core.commands;

import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.core.utils.Utils;
import com.alpsbte.plotsystem.core.utils.conversion.CoordinateConversion;
import com.alpsbte.plotsystem.core.utils.conversion.projection.OutOfProjectionBoundsException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class CMD_Tpll extends BaseCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(sender.hasPermission(getPermission())) {
            if (getPlayer(sender) != null) {
                Player player = (Player) sender;
                World playerWorld = player.getWorld();

                if (PlotManager.isPlotWorld(playerWorld)) {
                    try {
                        // TODO: Support for NSEW geographic coordinates
                        String[] splitCoords = args[0].split(",");
                        if (splitCoords.length == 2 && args.length < 3) {
                            args = splitCoords;
                        }
                        if (args[0].endsWith(",")) {
                            args[0] = args[0].substring(0, args[0].length() - 1);
                        }
                        if (args.length > 1 && args[1].endsWith(",")) {
                            args[1] = args[1].substring(0, args[1].length() - 1);
                        }
                        if (args.length != 2 && args.length != 3) {
                            player.sendMessage(Utils.getErrorMessageFormat("§lUsage: §c/tpll <lat> <lon>"));
                            return true;
                        }

                        try {
                            // Parse coordinates to doubles
                            double lat;
                            double lon;
                            try {
                                lat = Double.parseDouble(args[0]);
                                lon = Double.parseDouble(args[1]);
                            } catch (Exception ignore) {
                                player.sendMessage(Utils.getErrorMessageFormat("§lUsage: §c/tpll <lat> <lon>"));
                                return true;
                            }

                            // Get the terra coordinates from the irl coordinates
                            double[] terraCoords = CoordinateConversion.convertFromGeo(lon, lat);

                            // Get plot, that the player is in
                            Plot plot = PlotManager.getPlotByWorld(playerWorld);

                            // Convert terra coordinates to plot relative coordinates
                            CompletableFuture<double[]> plotCoords = PlotManager.convertTerraToPlotXZ(plot, terraCoords);

                            if(plotCoords == null) {
                                player.sendMessage(Utils.getErrorMessageFormat("You can only teleport to your plot!"));
                                return true;
                            }

                            // TODO: Support to insert own height as parameter
                            // Get Highest Y
                            int highestY = 0;
                            Location block = new Location(playerWorld, plotCoords.get()[0], 0, plotCoords.get()[1]);
                            for (int i = 1; i < 256; i++) {
                                block.add(0, 1, 0);
                                if (!block.getBlock().isEmpty()) {
                                    highestY = i;
                                }
                            }
                            if (highestY < 10) {
                                highestY = 10;
                            }

                            player.teleport(new Location(playerWorld, plotCoords.get()[0], highestY + 1, plotCoords.get()[1]));

                            DecimalFormat df = new DecimalFormat("##.#####");
                            df.setRoundingMode(RoundingMode.FLOOR);
                            player.sendMessage(Utils.getInfoMessageFormat("Teleporting to §6" + df.format(lat) + "§a, §6" + df.format(lon)));

                        } catch (SQLException ex) {
                            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                            player.sendMessage(Utils.getErrorMessageFormat("A unknown error occurred! Please try again!"));
                        } catch (IOException | OutOfProjectionBoundsException ex) {
                            Bukkit.getLogger().log(Level.SEVERE, "A coordinate conversion error occurred!", ex);
                            player.sendMessage(Utils.getErrorMessageFormat("A unknown error occurred! Please try again!"));
                        }
                    } catch (Exception ignore) {
                        sendInfo(sender);
                    }
                } else {
                    player.sendMessage(Utils.getErrorMessageFormat("You can only use /tpll on a plot!"));
                }
            } else {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "This command can only be used as a player!");
            }
        } else {
            sender.sendMessage(Utils.getErrorMessageFormat("You don't have permission to use this command!"));
        }
        return true;
    }

    @Override
    public String[] getNames() {
        return new String[] { "tpll" };
    }

    @Override
    public String getDescription() {
        return "Teleport to a specific RL coordinate.";
    }

    @Override
    public String[] getParameter() {
        return new String[] { "Lat", "Lon" };
    }

    @Override
    public String getPermission() {
        return "plotsystem.tpll";
    }
}