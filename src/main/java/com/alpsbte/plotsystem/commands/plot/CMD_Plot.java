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

import alpsbte.plotsystem.core.system.plot.Plot;
import alpsbte.plotsystem.core.system.plot.PlotHandler;
import alpsbte.plotsystem.core.system.plot.PlotManager;
import alpsbte.plotsystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class CMD_Plot implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(sender instanceof Player) {
            if(sender.hasPermission("alpsbte.review")) {
                if(args.length == 1 || args.length == 2) {
                    if(Utils.TryParseInt(args[0]) != null) {
                        int ID = Integer.parseInt(args[0]);
                        if(PlotManager.plotExists(ID)) {
                            try {
                                PlotHandler.teleportPlayer(new Plot(ID), (Player) sender);
                            } catch (Exception ex) {
                                sender.sendMessage(Utils.getErrorMessageFormat("An error occurred! Please try again!"));
                                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                            }
                        } else {
                            sender.sendMessage(Utils.getErrorMessageFormat("Could not find plot with ID #" + ID + "!"));
                        }
                    } else {
                        sender.sendMessage(Utils.getErrorMessageFormat("Please enter a valid ID!"));
                    }
                } else {
                    sender.sendMessage(Utils.getErrorMessageFormat("§lUsage: §c/plot <ID>"));
                }
            }
        }
        return true;
    }
}
