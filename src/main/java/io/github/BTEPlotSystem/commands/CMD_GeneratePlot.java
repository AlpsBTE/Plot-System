package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.core.plots.Plot;
import github.BTEPlotSystem.core.plots.PlotGenerator;
import github.BTEPlotSystem.utils.Builder;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.logging.Level;

public class CMD_GeneratePlot implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(sender instanceof Player) {
                if(sender.hasPermission("alpsbte.admin")) {
                    if(Utils.TryParseInt(args[0]) != null) {
                        try {
                            new PlotGenerator(new Plot(Integer.parseInt(args[0])), new Builder(((Player) sender).getUniqueId()));
                        } catch (SQLException ex) {
                            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                        }
                    }
                }
        }
        return true;
    }
}
