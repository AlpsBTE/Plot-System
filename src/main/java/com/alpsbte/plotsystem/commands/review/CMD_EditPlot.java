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

package com.alpsbte.plotsystem.commands.review;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.utils.io.config.ConfigPaths;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.language.LangPaths;
import com.alpsbte.plotsystem.utils.io.language.LangUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.logging.Level;

public class CMD_EditPlot extends BaseCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (PlotSystem.getPlugin().getConfigManager().getCommandsConfig().getBoolean(ConfigPaths.EDITPLOT_ENABLED)) {
            if(sender.hasPermission(getPermission())) {
                try {
                    Plot plot;
                    if (args.length > 0 && Utils.TryParseInt(args[0]) != null) {
                        int plotID = Integer.parseInt(args[0]);
                        if (PlotManager.plotExists(plotID)) {
                            plot = new Plot(plotID);
                        } else {
                            sender.sendMessage(Utils.getErrorMessageFormat(LangUtil.get(sender, LangPaths.Message.Error.PLOT_DOES_NOT_EXIST)));
                            return true;
                        }
                    } else if (getPlayer(sender) != null && PlotManager.isPlotWorld(getPlayer(sender).getWorld())) {
                        plot = PlotManager.getCurrentPlot(Builder.byUUID(getPlayer(sender).getUniqueId()));
                    } else {
                        sendInfo(sender);
                        return true;
                    }

                    if(plot.getPermissions().hasReviewerPerms()) {
                        plot.getPermissions().removeReviewerPerms().save(Builder.byUUID(getPlayer(sender).getUniqueId()));
                        sender.sendMessage(Utils.getInfoMessageFormat(LangUtil.get(sender, LangPaths.Message.Info.DISABLED_PLOT_PERMISSIONS,plot.getID() + "")));
                    } else {
                        plot.getPermissions().addReviewerPerms().save(Builder.byUUID(getPlayer(sender).getUniqueId()));
                        sender.sendMessage(Utils.getInfoMessageFormat(LangUtil.get(sender, LangPaths.Message.Info.ENABLED_PLOT_PERMISSIONS, plot.getID() + "")));
                    }
                } catch (SQLException ex) {
                    sender.sendMessage(Utils.getErrorMessageFormat(LangUtil.get(sender, LangPaths.Message.Error.ERROR_OCCURRED)));
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                }
            } else {
                sender.sendMessage(Utils.getErrorMessageFormat(LangUtil.get(sender, LangPaths.Message.Error.PLAYER_HAS_NO_PERMISSIONS)));
            }
        } else {
            sender.sendMessage(Utils.getErrorMessageFormat(LangUtil.get(sender, LangPaths.Message.Error.COMMAND_DISABLED)));
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
