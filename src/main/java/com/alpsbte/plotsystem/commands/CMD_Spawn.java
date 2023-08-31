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

import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMD_Spawn extends BaseCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender.hasPermission(getPermission())) {
            if (getPlayer(sender) != null) {
                Player player = (Player) sender;

                player.teleport(Utils.getSpawnLocation());
                player.sendMessage(Utils.ChatUtils.getInfoMessageFormat(langUtil.get(sender, LangPaths.Message.Info.TELEPORTING_SPAWN)));
                player.playSound(player.getLocation(), Utils.SoundUtils.TELEPORT_SOUND,1,1);
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
        return new String[] { "spawn" };
    }

    @Override
    public String getDescription() {
        return "Teleport to the spawn point.";
    }

    @Override
    public String[] getParameter() {
        return new String[0];
    }

    @Override
    public String getPermission() {
        return "plotsystem.spawn";
    }
}
