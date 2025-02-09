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
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.conversion.CoordinateConversion;
import com.alpsbte.plotsystem.utils.conversion.projection.OutOfProjectionBoundsException;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static net.kyori.adventure.text.Component.text;

public class CMD_Tpll extends BaseCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.PLAYER_HAS_NO_PERMISSIONS)));
            return true;
        }

        if (getPlayer(sender) == null) {
            Bukkit.getConsoleSender().sendMessage(text("This command can only be used as a player!", NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;
        World playerWorld = player.getWorld();

        if (!PlotUtils.isPlotWorld(playerWorld)) {
            player.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.CANNOT_TELEPORT_OUTSIDE_PLOT)));
            return true;
        }

        if (args == null || args.length < 2 || args.length > 3) {
            sendInfo(sender);
            return true;
        }

        String[] splitCoords = args[0].split(",");
        if (splitCoords.length == 2) {
            args = splitCoords;
        }

        if (args[0].endsWith(",")) {
            args[0] = args[0].substring(0, args[0].length() - 1);
        }
        if (args[1].endsWith(",")) {
            args[1] = args[1].substring(0, args[1].length() - 1);
        }

        // Parse coordinates to doubles
        double lat;
        double lon;
        try {
            lat = Double.parseDouble(args[0]);
            lon = Double.parseDouble(args[1]);
        } catch (Exception ignore) {
            sendInfo(sender);
            return true;
        }

        CompletableFuture.runAsync(() -> {
            try {
                // Get the terra coordinates from the irl coordinates
                double[] terraCoords = CoordinateConversion.convertFromGeo(lon, lat);

                // Get plot, that the player is in
                AbstractPlot plot = PlotUtils.getCurrentPlot(Builder.byUUID(player.getUniqueId()), Status.unfinished, Status.unreviewed, Status.completed);

                // Convert terra coordinates to plot relative coordinates
                CompletableFuture<double[]> plotCoordsFuture = plot != null ? PlotUtils.convertTerraToPlotXZ(plot, terraCoords) : null;

                if (plotCoordsFuture == null) {
                    player.sendMessage(Utils.ChatUtils.getAlertFormat(langUtil.get(sender, LangPaths.Message.Error.CANNOT_TELEPORT_OUTSIDE_PLOT)));
                    return;
                }

                double[] plotCoordinates = plotCoordsFuture.get();

                Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
                    int highestY = getHighestY(playerWorld, plotCoordinates);

                    player.teleport(new Location(playerWorld, plotCoordinates[0], highestY + 1, plotCoordinates[1], player.getLocation().getYaw(), player.getLocation().getPitch()));

                    DecimalFormat df = new DecimalFormat("##.#####");
                    df.setRoundingMode(RoundingMode.FLOOR);
                    player.sendMessage(Utils.ChatUtils.getInfoFormat(langUtil.get(sender, LangPaths.Message.Info.TELEPORTING_TPLL, df.format(lat), df.format(lon))));
                });
            } catch (IOException | OutOfProjectionBoundsException ex) {
                PlotSystem.getPlugin().getComponentLogger().error(text("A coordinate conversion error occurred!"), ex);
                player.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.ERROR_OCCURRED)));
            } catch (InterruptedException | ExecutionException ex) {
                sendInfo(sender);
            }
        });
        return true;
    }

    private static int getHighestY(World playerWorld, double[] plotCoordinates) {
        int highestY = 0;
        Location block = new Location(playerWorld, plotCoordinates[0], 0, plotCoordinates[1]);
        for (int i = 1; i < 256; i++) {
            block.add(0, 1, 0);
            if (!block.getBlock().isEmpty()) {
                highestY = i;
            }
        }
        if (highestY < PlotWorld.MIN_WORLD_HEIGHT) {
            highestY = PlotWorld.MIN_WORLD_HEIGHT;
        }
        return highestY;
    }

    @Override
    public String[] getNames() {
        return new String[]{"tpll"};
    }

    @Override
    public String getDescription() {
        return "Teleport to a specific RL coordinate.";
    }

    @Override
    public String[] getParameter() {
        return new String[]{"Lat", "Lon"};
    }

    @Override
    public String getPermission() {
        return "plotsystem.tpll";
    }
}