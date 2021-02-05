package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.Companion;
import github.BTEPlotSystem.utils.Leaderboard;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class CMDSetLeaderboardPosition implements CommandExecutor {

    private final FileConfiguration config = BTEPlotSystem.getPlugin().getConfig();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender instanceof Player){
            Player player = (Player)sender;
            if (sender.hasPermission("alpsbte.leaderboard")){
                try {
                    config.set("leaderboard.world",player.getWorld().getName());
                    config.set("leaderboard.x",player.getLocation().getX());
                    config.set("leaderboard.y",player.getLocation().getY());
                    config.set("leaderboard.z",player.getLocation().getZ());
                    BTEPlotSystem.getPlugin().saveConfig();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return true;
    }
}
