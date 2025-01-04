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

package com.alpsbte.plotsystem.commands.review;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.core.menus.ReviewMenu;
import com.alpsbte.plotsystem.core.menus.ReviewPlotMenu;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class CMD_Review extends BaseCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        if (getPlayer(sender) == null) {
            Bukkit.getConsoleSender().sendMessage(Component.text("This command can only be used as a player!", RED));
            return true;
        }

        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.PLAYER_HAS_NO_PERMISSIONS)));
            return true;
        }

        if (args.length > 0 && AlpsUtils.tryParseInt(args[0]) == null) {
            sendInfo(sender);
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(PlotSystem.getPlugin(), () -> {
            Plot plot = null;
            if (args.length > 0) {
                int plotId = Integer.parseInt(args[0]);
                if (!PlotUtils.plotExists(plotId)) {
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender,
                            LangPaths.Message.Error.PLOT_DOES_NOT_EXIST)));
                    return;
                }
                plot = new Plot(plotId);
            }

            try {
                Builder.Reviewer builder = Builder.byUUID(getPlayer(sender).getUniqueId()).getAsReviewer();
                Player player = (Player) sender;

                // Check if the given plot is valid
                if (plot != null && plot.getStatus() != Status.unreviewed) {
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender,
                            LangPaths.Message.Error.PLOT_DOES_NOT_EXIST)));
                    return;
                }

                // Check if the reviewer is on the plot
                AbstractPlot currentPlot = PlotUtils.getCurrentPlot(Builder.byUUID(player.getUniqueId()), Status.unreviewed);
                boolean teleportPlayer = false;
                if (currentPlot instanceof Plot cp) {
                    if (plot != null && plot.getID() != currentPlot.getID()) teleportPlayer = true;
                    else plot = cp;
                } else if (plot == null) {
                    Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> new ReviewMenu(player));
                    return;
                } else teleportPlayer = true;

                // If the reviewer is not on the plot, teleport the player
                if (teleportPlayer) {
                    Plot finalPlot = plot;
                    Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> finalPlot.getWorld().teleportPlayer(player));
                    return;
                }

                // Check if player is allowed to review this plot (cannot be the owner or a member of this plot)
                if ((plot.getPlotOwner().getUUID().toString().equals(player.getUniqueId().toString()) ||
                        (!plot.getPlotMembers().isEmpty() && plot.getPlotMembers().stream()
                                .anyMatch(b -> b.getUUID().toString().equals(player.getUniqueId().toString())))) &&
                                !PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.DEV_MODE)) {
                    player.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(player,
                            LangPaths.Message.Error.CANNOT_REVIEW_OWN_PLOT)));
                    return;
                }

                // Check if the reviewer is allowed to review this plot in this city project
                int countryID = plot.getCity().getCountry().getID();
                if (builder.getCountries().stream().anyMatch(c -> c.getID() == countryID)) {
                    Plot finalPlot = plot;
                    Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> new ReviewPlotMenu(player, finalPlot));
                    return;
                }

                Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> new ReviewMenu(player));
            } catch (SQLException ex) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(langUtil.get(sender, LangPaths.Message.Error.ERROR_OCCURRED)));
                PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
            }
        });
        return true;
    }

    @Override
    public String[] getNames() {
        return new String[]{"review"};
    }

    @Override
    public String getDescription() {
        return "Opens the review menu or review plot menu.";
    }

    @Override
    public String[] getParameter() {
        return new String[]{"ID"};
    }

    @Override
    public String getPermission() {
        return "plotsystem.review";
    }
}
