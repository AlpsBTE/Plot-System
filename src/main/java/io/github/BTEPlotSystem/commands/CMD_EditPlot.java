package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.core.plots.PlotManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMD_EditPlot implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(sender instanceof Player) {
            if(sender.hasPermission("alpsbte.review") || sender.hasPermission("alpsbte.admin")) {
                Player player = (Player)sender;

                if(PlotManager.isPlotWorld(player.getWorld())) {

                }
            }
        }
        return true;
    }
}
