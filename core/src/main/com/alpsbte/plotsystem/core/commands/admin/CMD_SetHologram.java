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

package com.alpsbte.plotsystem.core.commands.admin;

import com.alpsbte.plotsystem.core.PlotSystem;
import com.alpsbte.plotsystem.core.commands.BaseCommand;
import com.alpsbte.plotsystem.core.holograms.HolographicDisplay;
import com.alpsbte.plotsystem.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMD_SetHologram extends BaseCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (PlotSystem.DependencyManager.isHolographicDisplaysEnabled()) {
            if (sender.hasPermission(getPermission())){
                if (getPlayer(sender) != null){
                    Player player = (Player)sender;
                    if (args.length == 1) {
                        // Find hologram by name
                        HolographicDisplay hologram = PlotSystem.getHolograms().stream()
                                .filter(holo -> holo.getHologramName().equalsIgnoreCase(args[0]))
                                .findFirst()
                                .orElse(null);

                        // Update hologram location
                        if(hologram != null) {
                            hologram.setLocation(player.getLocation());
                            player.sendMessage(Utils.getInfoMessageFormat("Successfully updated hologram location!"));
                            player.playSound(player.getLocation(), Utils.Done,1,1);

                            PlotSystem.reloadHolograms();
                        } else {
                            player.sendMessage(Utils.getErrorMessageFormat("Hologram could not be found!"));
                        }
                    } else {
                        sendInfo(sender);
                        player.sendMessage("§8------- §6§lHolograms §8-------");
                        for(HolographicDisplay holo : PlotSystem.getHolograms()) {
                            player.sendMessage(" §6> §f" + holo.getHologramName());
                        }
                        player.sendMessage("§8--------------------------");
                    }

                } else {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "This command can only be used as a player!");
                }
            } else {
                sender.sendMessage(Utils.getErrorMessageFormat("You don't have permission to use this command!"));
            }
        } else {
            sender.sendMessage(Utils.getErrorMessageFormat("Holograms (Holographic Displays) extension is not loaded!"));
        }
        return true;
    }

    @Override
    public String[] getNames() {
        return new String[] { "sethologram" };
    }

    @Override
    public String getDescription() {
        return "Sets the position of a hologram.";
    }

    @Override
    public String[] getParameter() {
        return new String[] { "Name" };
    }

    @Override
    public String getPermission() {
        return "plotsystem.admin.sethologram";
    }
}
