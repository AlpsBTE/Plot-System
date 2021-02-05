package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.core.plots.Plot;
import github.BTEPlotSystem.core.plots.PlotManager;
import github.BTEPlotSystem.utils.Builder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.logging.Level;

public class CMDGeneratePlot implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(sender instanceof Player) {
            try {
                if(sender.hasPermission("alpsbte.generateplot")) {
                    int plotid = 0;
                    try {
                        plotid = Integer.parseInt(args[0]);
                    } catch (Exception e) {
                        Bukkit.getLogger().log(Level.SEVERE, "Could not convert plot id");
                    }
                    PlotManager.ClaimPlot(plotid, new Builder(((Player) sender).getUniqueId()));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return true;
    }
}
