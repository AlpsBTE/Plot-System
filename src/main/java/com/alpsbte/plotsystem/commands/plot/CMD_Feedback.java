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

package alpsbte.plotsystem.commands.plot;

import alpsbte.plotsystem.core.menus.FeedbackMenu;
import alpsbte.plotsystem.core.system.Builder;
import alpsbte.plotsystem.core.system.plot.Plot;
import alpsbte.plotsystem.core.system.plot.PlotManager;
import alpsbte.plotsystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.logging.Level;

public class CMD_Feedback implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(sender instanceof Player) {
            if(sender.hasPermission("alpsbte.plot")) {
                if(args.length == 1) {
                    if(Utils.TryParseInt(args[0]) != null) {
                        try {
                            Plot plot = new Plot(Integer.parseInt(args[0]));

                            if(PlotManager.plotExists(plot.getID())) {
                                if(plot.isReviewed() || plot.isRejected()) {
                                    Builder builder = new Builder(((Player) sender).getUniqueId());
                                    if(plot.getPlotOwner().getUUID().equals(builder.getUUID()) || plot.getPlotMembers().stream().anyMatch(m -> m.getUUID().equals(builder.getUUID())) || sender.hasPermission("alpsbte.review")) {
                                        new FeedbackMenu((Player) sender, plot.getID());
                                    } else {
                                        sender.sendMessage(Utils.getErrorMessageFormat("You don't have permission to see this feedback!"));
                                    }
                                } else {
                                    sender.sendMessage(Utils.getErrorMessageFormat("This plot has not yet been reviewed!"));
                                }
                            } else {
                                sender.sendMessage(Utils.getErrorMessageFormat("Could not find plot feedback with ID #" + plot.getID() + "!"));
                            }
                        } catch (SQLException ex) {
                            sender.sendMessage(Utils.getErrorMessageFormat("An error occurred! Please try again!"));
                            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                        }
                    } else {
                        sender.sendMessage(Utils.getErrorMessageFormat("Please enter a valid ID!"));
                    }
                } else {
                    sender.sendMessage(Utils.getErrorMessageFormat("§lUsage: §c/feedback <ID>"));
                }
            }
        }
        return true;
    }
}
