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

package com.alpsbte.plotsystem.commands;

import com.alpsbte.plotsystem.core.menus.CompanionMenu;
import com.alpsbte.plotsystem.core.menus.ReviewMenu;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMD_Tpp extends BaseCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender.hasPermission(getPermission())) {
            if(getPlayer(sender) != null) {
                Player player = (Player) sender;
                try {
                    Player targetPlayer = player.getServer().getPlayer(args[0]);
                    player.teleport(targetPlayer);
                    player.sendMessage(Utils.getInfoMessageFormat("Teleporting to player..."));

                    player.getInventory().setItem(8, CompanionMenu.getMenuItem());
                    if (player.hasPermission("alpsbte.review")) {
                        player.getInventory().setItem(7, ReviewMenu.getMenuItem());
                    }
                } catch (Exception ignore) {
                    sendInfo(sender);
                }
            } else {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "This command can only be used as a player!");
            }
        } else {
            sender.sendMessage(Utils.getErrorMessageFormat("You don't have permission to use this command!"));
        }

        return true;
    }

    @Override
    public String[] getNames() {
        return new String[] { "tpp" };
    }

    @Override
    public String getDescription() {
        return "Teleport to a specific player.";
    }

    @Override
    public String[] getParameter() {
        return new String[] { "Player" };
    }

    @Override
    public String getPermission() {
        return "plotsystem.tpp";
    }
}
