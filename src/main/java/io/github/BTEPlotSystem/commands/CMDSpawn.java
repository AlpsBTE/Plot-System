package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMDSpawn implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (sender instanceof Player){
            Player player = (Player)sender;
            if (sender.hasPermission("alpsbte.companion")){
                player.teleport(Utils.getSpawnPoint());
                player.sendMessage("§l§7>> §aTeleported to spawn");
                player.playSound(player.getLocation(), Utils.TeleportSound,1,1);
            }
        }
        return true;
    }
}
