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

import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.logging.Level;

public class CMD_SendFeedback extends BaseCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender.hasPermission(getPermission())) {
            try {
                if (args.length > 1 && Utils.TryParseInt(args[0]) != null){
                    int plotID = Integer.parseInt(args[0]);
                    if(PlotManager.plotExists(plotID)) {
                        Plot plot = new Plot(Integer.parseInt(args[0]));
                        if (plot.isReviewed() || plot.isRejected()) {
                            if (getPlayer(sender) == null || sender.hasPermission("alpsbte.admin") || plot.getReview().getReviewer().getUUID().equals(((Player)sender).getUniqueId())) {
                                StringBuilder feedback = new StringBuilder();
                                for(int i = 2; i <= args.length; i++) {
                                    feedback.append(args.length == 2 ? "" : " ").append(args[i - 1]);
                                }
                                plot.getReview().setFeedback(feedback.toString());

                                sender.sendMessage(Utils.getInfoMessageFormat("The feedback for the plot §6#" + plot.getID() + " §ahas been updated!"));
                            } else {
                                sender.sendMessage(Utils.getErrorMessageFormat("You cannot send feedback to a plot that you haven't reviewed yourself!"));
                            }
                        } else {
                            sender.sendMessage(Utils.getErrorMessageFormat("Plot is either unclaimed or hasn't been reviewed yet!"));
                        }
                    } else {
                        sender.sendMessage(Utils.getErrorMessageFormat("This plot does not exist!"));
                    }
                } else {
                    sendInfo(sender);
                }
            } catch (SQLException ex) {
                sender.sendMessage(Utils.getErrorMessageFormat("An error occurred while executing command!"));
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        } else {
            sender.sendMessage(Utils.getErrorMessageFormat("You don't have permission to use this command!"));
        }
        return true;
    }

    @Override
    public String[] getNames() {
        return new String[] { "sendFeedback" };
    }

    @Override
    public String getDescription() {
        return "Updates feedback of a plot.";
    }

    @Override
    public String[] getParameter() {
        return new String[] { "ID", "Name" };
    }

    @Override
    public String getPermission() {
        return "plotsystem.review.sendfeedback";
    }
}
