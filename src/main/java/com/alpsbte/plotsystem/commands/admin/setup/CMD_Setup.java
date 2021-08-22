package com.alpsbte.plotsystem.commands.admin.setup;

import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMD_Setup implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length > 0) {
                //onCommand(player, args);
            } else {
                // Show commands!
                player.sendMessage(Utils.getErrorMessageFormat("Use one of the following commands:"));
                player.sendMessage("§8--------------------------");
                player.sendMessage(" §6> §f/pss server");
                player.sendMessage(" §6> §f/pss ftp");
                player.sendMessage(" §6> §f/pss country");
                player.sendMessage(" §6> §f/pss city");
                player.sendMessage("§8--------------------------");
            }
        }
        return true;
    }
}
