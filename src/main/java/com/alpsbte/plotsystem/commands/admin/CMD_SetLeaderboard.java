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

package com.alpsbte.plotsystem.commands.admin;

import com.alpsbte.alpslib.hologram.HolographicDisplay;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.core.holograms.LeaderboardManager;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CMD_SetLeaderboard extends BaseCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        if (!PlotSystem.DependencyManager.isHolographicDisplaysEnabled()) {
            sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("Holograms (Holographic Displays) extension is not loaded!"));
            return true;
        }

        if (!sender.hasPermission(getPermission())){
            sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("You don't have permission to use this command!"));
            return true;
        }

        if (getPlayer(sender) == null){
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "This command can only be used as a player!");
            return true;
        }

        Player player = (Player)sender;
        if (args.length != 1) {
            sendInfo(sender);
            player.sendMessage("§8------- §6§lHolograms §8-------");
            for(HolographicDisplay holo : LeaderboardManager.getLeaderboards()) {
                player.sendMessage(" §6> §f" + holo.getId());
            }
            player.sendMessage("§8--------------------------");
            return true;
        }

        // Find hologram by name
        HolographicDisplay hologram = LeaderboardManager.getLeaderboards().stream()
                .filter(holo -> holo.getId().equalsIgnoreCase(args[0]))
                .findFirst()
                .orElse(null);

        // Update hologram location
        if(hologram == null) {
            player.sendMessage(Utils.ChatUtils.getErrorMessageFormat("Hologram could not be found!"));
            return true;
        }
        LeaderboardManager.savePosition(hologram.getId(), getPlayer(sender).getLocation());
        player.sendMessage(Utils.ChatUtils.getInfoMessageFormat("Successfully updated hologram location!"));
        player.playSound(player.getLocation(), Utils.SoundUtils.DONE_SOUND,1,1);

        LeaderboardManager.reloadLeaderboards();
        return true;
    }

    @Override
    public String[] getNames() {
        return new String[] { "setleaderboard" };
    }

    @Override
    public String getDescription() {
        return "Sets the position of a leaderboard.";
    }

    @Override
    public String[] getParameter() {
        return new String[] { "Name" };
    }

    @Override
    public String getPermission() {
        return "plotsystem.admin.setleaderboard";
    }
}