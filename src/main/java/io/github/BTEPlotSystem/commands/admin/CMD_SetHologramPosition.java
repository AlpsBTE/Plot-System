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
                }

            }
        }
        return true;
    }
}
