package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.core.plots.Plot;
import github.BTEPlotSystem.core.plots.PlotHandler;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sun.nio.ch.Util;

import java.sql.SQLException;

public class CMDLink implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (sender instanceof Player){
            Player player = (Player)sender;
            //Check if player is in plot world
            if (player.getWorld().getName().startsWith("P-")){
                int plotID = Integer.parseInt(player.getWorld().getName().substring(2));
                try {
                    PlotHandler.sendLinkMessages(new Plot(plotID),player);
                } catch (SQLException e) {
                    player.sendMessage(Utils.getErrorMessageFormat("SQL Error!"));
                }
            } else {
                player.sendMessage(Utils.getErrorMessageFormat("You must be on a plot to use this!"));
            }
        }
        return true;
    }
}
