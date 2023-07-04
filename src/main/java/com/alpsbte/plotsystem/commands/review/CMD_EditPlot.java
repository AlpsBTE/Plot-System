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
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.logging.Level;

public class CMD_EditPlot extends BaseCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (getPlayer(sender) != null) {
            if (ConfigUtil.getInstance().configs[1].getBoolean(ConfigPaths.EDITPLOT_ENABLED)) {
                if (sender.hasPermission(getPermission())) {
                    try {
                        Plot plot;
                        if (args.length > 0 && AlpsUtils.TryParseInt(args[0]) != null) {
                            int plotID = Integer.parseInt(args[0]);
                            if (PlotManager.plotExists(plotID)) {
                                plot = new Plot(plotID);
                            } else {
                                sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat(langUtil.get(sender, LangPaths.Message.Error.PLOT_DOES_NOT_EXIST)));
                                return true;
                            }
                        } else if (getPlayer(sender) != null && PlotManager.isPlotWorld(getPlayer(sender).getWorld())) {
                            plot = PlotManager.getCurrentPlot(Builder.byUUID(getPlayer(sender).getUniqueId()), Status.unfinished, Status.unreviewed);
                        } else {
                            sendInfo(sender);
                            return true;
                        }

                        if (plot != null && plot.getStatus() != Status.completed) {
                            Builder builder = Builder.byUUID(getPlayer(sender).getUniqueId());
                            int countryID = plot.getCity().getCountry().getID();
                            if (builder.isReviewer() && builder.getAsReviewer().getCountries().stream().anyMatch(c -> c.getID() == countryID) && plot.getPlotOwner().getUUID() != builder.getUUID() && plot.getPlotMembers().stream().noneMatch(b -> b.getUUID() == builder.getUUID())) {
                                if (plot.getPermissions().hasBuildingPerms(builder.getUUID())) {
                                    plot.getPermissions().removeBuilderPerms(builder.getUUID()).save();
                                    sender.sendMessage(Utils.ChatUtils.getInfoMessageFormat(langUtil.get(sender, LangPaths.Message.Info.DISABLED_PLOT_PERMISSIONS, plot.getID() + "")));
                                } else {
                                    plot.getPermissions().addBuilderPerms(builder.getUUID()).save();
                                    sender.sendMessage(Utils.ChatUtils.getInfoMessageFormat(langUtil.get(sender, LangPaths.Message.Info.ENABLED_PLOT_PERMISSIONS, plot.getID() + "")));
                                }
                                return true;
                            }
                        }
                        sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat(langUtil.get(sender, LangPaths.Message.Error.PLAYER_IS_NOT_ALLOWED)));
                    } catch (SQLException ex) {
                        sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat(langUtil.get(sender, LangPaths.Message.Error.ERROR_OCCURRED)));
                        Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                    }
                } else {
                    sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat(langUtil.get(sender, LangPaths.Message.Error.PLAYER_HAS_NO_PERMISSIONS)));
                }
            } else {
                sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat(langUtil.get(sender, LangPaths.Message.Error.COMMAND_DISABLED)));
            }
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "This command can only be used as a player!");
        }
        return true;
    }

    @Override
    public String[] getNames() {
        return new String[] { "editplot" };
    }

    @Override
    public String getDescription() {
        return "Enables/disables build permissions for reviewers on a plot.";
    }

    @Override
    public String[] getParameter() {
        return new String[] { "ID" };
    }

    @Override
    public String getPermission() {
        return "plotsystem.review.editplot";
    }
}
