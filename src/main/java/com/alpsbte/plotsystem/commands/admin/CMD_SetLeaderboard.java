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

public class CMD_SetLeaderboard extends BaseCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (PlotSystem.DependencyManager.isHolographicDisplaysEnabled()) {
            if (sender.hasPermission(getPermission())){
                if (getPlayer(sender) != null){
                    Player player = (Player)sender;
                    if (args.length == 1) {
                        // Find leaderboard by name
                        HolographicDisplay leaderboard = LeaderboardManager.getLeaderboards().stream()
                                .filter(holo -> holo.getId().equalsIgnoreCase(args[0]))
                                .findFirst()
                                .orElse(null);

                        // Update leaderboard location
                        if(leaderboard != null) {
                            LeaderboardManager.savePosition(leaderboard.getId(), getPlayer(sender).getLocation());
                            player.sendMessage(Utils.ChatUtils.getInfoMessageFormat("Successfully updated leaderboard location!"));
                            player.playSound(player.getLocation(), Utils.SoundUtils.DONE_SOUND,1,1);

                            LeaderboardManager.reloadLeaderboards();
                        } else {
                            player.sendMessage(Utils.ChatUtils.getErrorMessageFormat("Leaderboard could not be found!"));
                        }
                    } else {
                        sendInfo(sender);
                        player.sendMessage("§8------- §6§lLeaderboards §8-------");
                        for(HolographicDisplay leaderboard : LeaderboardManager.getLeaderboards()) {
                            player.sendMessage(" §6> §f" + leaderboard.getId());
                        }
                        player.sendMessage("§8-----------------------------");
                    }

                } else {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "This command can only be used as a player!");
                }
            } else {
                sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("You don't have permission to use this command!"));
            }
        } else {
            sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("Holograms (Holographic Displays) extension is not loaded!"));
        }
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