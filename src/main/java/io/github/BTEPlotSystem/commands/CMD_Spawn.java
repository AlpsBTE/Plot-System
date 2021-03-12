package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMD_Spawn implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender instanceof Player){
            Player player = (Player)sender;

            player.teleport(Utils.getSpawnPoint());
            player.sendMessage(Utils.getInfoMessageFormat("Teleporting to spawn..."));
            player.playSound(player.getLocation(), Utils.TeleportSound,1,1);
        }
        return true;
    }
}
