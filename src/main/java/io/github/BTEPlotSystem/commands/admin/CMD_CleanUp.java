package github.BTEPlotSystem.commands.admin;

import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.core.plots.Plot;
import github.BTEPlotSystem.core.plots.PlotManager;
import github.BTEPlotSystem.utils.enums.Status;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public class CMD_CleanUp implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(sender.hasPermission("alpsbte.admin")) {
            // Clean Up Review System
            try {
                for (Plot plot : PlotManager.getPlots()) {
                    if(plot.getStatus() != Status.unclaimed && plot.getLastActivity() == null) {
                        PreparedStatement ps = DatabaseConnection.prepareStatement("UPDATE plots set lastActivity = ? WHERE idplot = '" + plot.getID() + "'");
                        ps.setDate(1, Date.valueOf(java.time.LocalDate.now()));
                        ps.executeUpdate();
                    }
                }
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        }
        return true;
    }
}
