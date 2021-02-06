package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.core.plots.Plot;
import github.BTEPlotSystem.core.plots.PlotGenerator;
import github.BTEPlotSystem.utils.Builder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.logging.Level;

public class CMDGeneratePlot implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(sender instanceof Player) {
            try {
                if(sender.hasPermission("alpsbte.generateplot")) {
                    int plotid = 1;
                    try {
                        System.out.println(args[0]);
                        plotid = Integer.parseInt(args[0]);
                    } catch (Exception e) {
                        Bukkit.getLogger().log(Level.SEVERE, "Could not convert plot id");
                    }
                    new PlotGenerator(new Plot(plotid), new Builder(((Player) sender).getUniqueId())).generate();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return true;
    }
}
