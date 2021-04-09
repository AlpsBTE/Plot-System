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

package github.BTEPlotSystem.commands.admin;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.holograms.HolographicDisplay;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

public class CMD_Reload implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender.hasPermission("alpsbte.admin")){
            try {
                BTEPlotSystem.getPlugin().saveConfig();
                BTEPlotSystem.getPlugin().reloadConfig();
                sender.sendMessage(Utils.getInfoMessageFormat("Successfully reloaded config!"));

                BTEPlotSystem.getHolograms().forEach(HolographicDisplay::updateLeaderboard);

                sender.sendMessage(Utils.getInfoMessageFormat("Successfully reloaded holograms!"));
            } catch (Exception ex) {
                sender.sendMessage(Utils.getErrorMessageFormat("An error occurred while reloading!"));
                Bukkit.getLogger().log(Level.SEVERE, "An error occurred while reloading!", ex);
            }
        }
        return true;
    }
}
