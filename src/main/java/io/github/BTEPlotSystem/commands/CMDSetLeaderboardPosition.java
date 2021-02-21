package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class CMDSetLeaderboardPosition implements CommandExecutor {

    private final FileConfiguration config = BTEPlotSystem.getPlugin().getConfig();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender instanceof Player){
            Player player = (Player)sender;
            if (sender.hasPermission("alpsbte.leaderboard")){
                if (args.length == 1) {
                    if (args[0].equals("leaderboardScore") || args[0].equals("leaderboardParkour")) {
                        try {
                            config.set(args[0] + ".world",player.getWorld().getName());
                            config.set(args[0] + ".x",player.getLocation().getX());
                            config.set(args[0] + ".y",player.getLocation().getY());
                            config.set(args[0] + ".z",player.getLocation().getZ());
                            BTEPlotSystem.getPlugin().saveConfig();
                            player.playSound(player.getLocation(), Utils.Done,1,1);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } else {
                    player.sendMessage("§4Invalid command input!");
                }

            }
        }
        return true;
    }
}
