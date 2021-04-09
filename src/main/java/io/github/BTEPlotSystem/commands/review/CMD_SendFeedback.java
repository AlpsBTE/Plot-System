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

package github.BTEPlotSystem.commands.review;

import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.core.system.plot.PlotManager;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.logging.Level;

public class CMD_SendFeedback implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender instanceof Player){
            Player player = (Player)sender;
            if (player.hasPermission("alpsbte.review")){
                if (args.length >= 2){
                    if (Utils.TryParseInt(args[0]) != null){
                        try {
                            if(PlotManager.plotExists(Integer.parseInt(args[0]))) {
                                Plot plot = new Plot(Integer.parseInt(args[0]));
                                if (plot.isReviewed() || plot.isRejected()) {
                                    if (plot.getReview().getReviewer().getUUID().equals(player.getUniqueId()) || player.hasPermission("alpsbte.admin")){

                                        StringBuilder feedback = new StringBuilder();

                                        for(int i = 2; i <= args.length; i++) {
                                            feedback.append(args.length == 2 ? "" : " ").append(args[i - 1]);
                                        }

                                        plot.getReview().setFeedback(feedback.toString());

                                        player.sendMessage(Utils.getInfoMessageFormat("The feedback for the plot §6#" + plot.getID() + " §ahas been updated!"));


                                    } else {
                                        sender.sendMessage(Utils.getErrorMessageFormat("You cannot send feedback to a plot that you haven't reviewed yourself!"));
                                    }
                                } else {
                                    sender.sendMessage(Utils.getErrorMessageFormat("Plot is either unclaimed or hasn't been reviewed yet!"));
                                }
                            } else {
                                sender.sendMessage(Utils.getErrorMessageFormat("This plot does not exist!"));
                            }
                        } catch (SQLException ex) {
                            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                        }
                    }
                } else {
                    sender.sendMessage(Utils.getErrorMessageFormat("§lUsage: §c/sendfeedback <ID> <Text>"));
                }
            }
        }
        return true;
    }
}
