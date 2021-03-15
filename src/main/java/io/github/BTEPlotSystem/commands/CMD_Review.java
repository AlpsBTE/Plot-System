package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.menus.ReviewMenu;
import github.BTEPlotSystem.core.plots.PlotManager;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.logging.Level;

public class CMD_Review implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender instanceof Player){
            if (sender.hasPermission("alpsbte.review")){
                try {
                    Player player = (Player) sender;
                    if(PlotManager.isPlotWorld(player.getWorld())) {
                        Bukkit.getPluginManager().registerEvents(new ReviewMenu(player, PlotManager.getPlotByWorld(player.getWorld())), BTEPlotSystem.getPlugin());
                    } else {
                        Bukkit.getPluginManager().registerEvents(new ReviewMenu(player), BTEPlotSystem.getPlugin());
                    }
                } catch (SQLException ex) {
                    sender.sendMessage(Utils.getErrorMessageFormat("An error occurred! Please try again!"));
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                }
            }
        }
        return true;
    }
}
