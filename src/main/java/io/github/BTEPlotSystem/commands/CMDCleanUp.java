package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.core.plots.Plot;
import github.BTEPlotSystem.core.plots.PlotManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;

public class CMDCleanUp implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(sender.hasPermission("alpsbte.cleanup")) {

            // Clean Up Review System
            try {
                for (Plot plot : PlotManager.getPlots()) {
                    if(!plot.isReviewed()) {
                        // TODO: Add plot to review table and reference it
                    }

                    if(plot.getLastActivity() == null) {
                        // TODO: Set last activity to current date
                    }
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
            }

        }
        return true;
    }
}
