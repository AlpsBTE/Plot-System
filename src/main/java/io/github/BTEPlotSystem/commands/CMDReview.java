package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.menus.ReviewMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class CMDReview implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender instanceof Player){
            Player player = (Player)sender;
            if (sender.hasPermission("alpsbte.review")){
                try {
                    Bukkit.getPluginManager().registerEvents(new ReviewMenu(player), BTEPlotSystem.getPlugin());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return true;
    }
}
