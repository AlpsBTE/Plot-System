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

import com.alpsbte.plotsystem.core.menus.PlayerPlotsMenu;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.logging.Level;

public class CMD_Plots extends BaseCommand {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender.hasPermission(getPermission())) {
            if(getPlayer(sender) != null) {
                Player player = (Player)sender;
                try {
                    if(args.length >= 1) {
                        Builder builder = Builder.getBuilderByName(args[0]);
                        if (builder != null){
                            new PlayerPlotsMenu(player, builder);
                        } else {
                            player.sendMessage(Utils.ChatUtils.getErrorMessageFormat(langUtil.get(sender, LangPaths.Message.Error.PLAYER_NOT_FOUND)));
                        }
                    } else {
                        new PlayerPlotsMenu(player, Builder.byUUID(player.getUniqueId()));
                    }
                } catch (SQLException ex) {
                    sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat(langUtil.get(sender, LangPaths.Message.Error.ERROR_OCCURRED)));
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                }
            } else {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "This command can only be used as a player!");
            }
        } else {
            sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat(langUtil.get(sender, LangPaths.Message.Error.PLAYER_HAS_NO_PERMISSIONS)));
        }
        return true;
    }

    @Override
    public String[] getNames() {
        return new String[] { "plots" } ;
    }

    @Override
    public String getDescription() {
        return "Shows all plots of the given player.";
    }

    @Override
    public String[] getParameter() {
        return new String[] { "Player" };
    }

    @Override
    public String getPermission() {
        return "plotsystem.plots";
    }
}
