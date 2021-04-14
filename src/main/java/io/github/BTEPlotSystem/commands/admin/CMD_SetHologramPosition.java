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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMD_SetHologramPosition implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender instanceof Player){
            Player player = (Player)sender;
            if (sender.hasPermission("alpsbte.admin")){
                if (args.length == 1) {
                    if(BTEPlotSystem.getHolograms().stream().anyMatch(holo -> holo.getHologramName().equalsIgnoreCase(args[0]))) {
                        HolographicDisplay hologram = BTEPlotSystem.getHolograms().stream().filter(holo -> holo.getHologramName().equalsIgnoreCase(args[0])).findFirst().get();
                        hologram.setLocation(player.getLocation());
                        player.sendMessage(Utils.getInfoMessageFormat("Successfully set new hologram location of §f" + hologram.getHologramName() + "§7!"));
                        player.playSound(player.getLocation(), Utils.Done,1,1);
                    } else {
                        player.sendMessage(Utils.getErrorMessageFormat("Could not find hologram with the name §f" + args[0] + "§7!"));
                    }
                } else {
                    player.sendMessage(Utils.getErrorMessageFormat("§lUsage: §c/sethologram <name>"));
                    player.sendMessage("§7------- §6§lHologram List §7-------");
                    for(HolographicDisplay holo : BTEPlotSystem.getHolograms()) {
                        player.sendMessage(Utils.getInfoMessageFormat(holo.getHologramName()));
                    }
                    player.sendMessage("§7-----------------------------");
                }

            }
        }
        return true;
    }
}
