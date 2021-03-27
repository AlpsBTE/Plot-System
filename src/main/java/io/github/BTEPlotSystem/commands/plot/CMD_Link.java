package github.BTEPlotSystem.commands.plot;

import github.BTEPlotSystem.core.system.plot.PlotHandler;
import github.BTEPlotSystem.core.system.plot.PlotManager;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.logging.Level;

public class CMD_Link implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player){
            if(sender.hasPermission("alpsbte.plot")) {
                Player player = (Player)sender;
                if (PlotManager.isPlotWorld(player.getWorld())){
                    try {
                        PlotHandler.sendLinkMessages(PlotManager.getPlotByWorld(player.getWorld()),player);
                    } catch (SQLException ex) {
                        player.sendMessage(Utils.getErrorMessageFormat("An error occurred! Please try again!"));
                        Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                    }
                } else {
                    player.sendMessage(Utils.getErrorMessageFormat("You must be on a plot to use this command!"));
                }
            }
        }
        return true;
    }
}
