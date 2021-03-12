package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.core.plots.Plot;
import github.BTEPlotSystem.core.plots.PlotHandler;
import github.BTEPlotSystem.core.plots.PlotManager;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.logging.Level;

public class CMD_Plot implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(sender instanceof Player) {
            if(sender.hasPermission("alpsbte.admin")) {
                if(args.length == 1) {
                    if(Utils.TryParseInt(args[0]) != null) {
                        int ID = Integer.parseInt(args[0]);
                        if(PlotManager.plotExists(ID)) {
                            try {
                                PlotHandler.teleportPlayer(new Plot(ID), (Player) sender);
                            } catch (SQLException ex) {
                                sender.sendMessage(Utils.getErrorMessageFormat("An error occurred! Please try again!"));
                                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                            }
                        } else {
                            sender.sendMessage(Utils.getErrorMessageFormat("Could not find plot with ID #" + ID + "!"));
                        }
                    } else {
                        sender.sendMessage(Utils.getErrorMessageFormat("Please enter a valid ID!"));
                    }
                } else {
                    sender.sendMessage(Utils.getErrorMessageFormat("§lUsage: §c/plot <ID>"));
                }
            }
        }
        return true;
    }
}
