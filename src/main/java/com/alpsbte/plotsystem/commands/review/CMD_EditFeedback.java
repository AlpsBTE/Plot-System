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
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.logging.Level;

public class CMD_EditFeedback extends BaseCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender.hasPermission(getPermission())) {
            try {
                if (args.length > 1 && AlpsUtils.TryParseInt(args[0]) != null){
                    int plotID = Integer.parseInt(args[0]);
                    if(PlotManager.plotExists(plotID)) {
                        Plot plot = new Plot(Integer.parseInt(args[0]));
                        if (plot.isReviewed() || plot.isRejected()) {
                            if (getPlayer(sender) == null || sender.hasPermission("plotsystem.admin") || plot.getReview().getReviewer().getUUID().equals(((Player)sender).getUniqueId())) {
                                StringBuilder feedback = new StringBuilder();
                                for(int i = 2; i <= args.length; i++) {
                                    feedback.append(args.length == 2 ? "" : " ").append(args[i - 1]);
                                }
                                plot.getReview().setFeedback(feedback.toString());

                                sender.sendMessage(Utils.ChatUtils.getInfoMessageFormat(langUtil.get(sender, LangPaths.Message.Info.UPDATED_PLOT_FEEDBACK, plot.getID() + "")));
                            } else {
                                sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat(langUtil.get(sender, LangPaths.Message.Error.CANNOT_SEND_FEEDBACK)));
                            }
                        } else {
                            sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat(langUtil.get(sender, LangPaths.Message.Error.PLOT_EITHER_UNCLAIMED_OR_UNREVIEWED)));
                        }
                    } else {
                        sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat(langUtil.get(sender, LangPaths.Message.Error.PLOT_DOES_NOT_EXIST)));
                    }
                } else {
                    sendInfo(sender);
                }
            } catch (SQLException ex) {
                sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat(langUtil.get(sender, LangPaths.Message.Error.ERROR_OCCURRED)));
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        } else {
            sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat(langUtil.get(sender, LangPaths.Message.Error.PLAYER_HAS_NO_PERMISSIONS)));
        }
        return true;
    }

    @Override
    public String[] getNames() {
        return new String[] { "editfeedback" };
    }

    @Override
    public String getDescription() {
        return "Updates the feedback of a plot.";
    }

    @Override
    public String[] getParameter() {
        return new String[] { "ID", "Feedback" };
    }

    @Override
    public String getPermission() {
        return "plotsystem.review.editfeedback";
    }
}
