package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.plots.Plot;
import github.BTEPlotSystem.core.plots.PlotHandler;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class CMDPlot implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(sender instanceof Player) {
            if(sender.hasPermission("alpsbte.plot")) {
                if(args.length == 1) {
                    int ID;
                    try {
                        ID = Integer.parseInt(args[0]);
                    } catch (Exception e) {
                        sender.sendMessage(Utils.getErrorMessageFormat("Please enter a valid ID!"));
                        return true;
                    }

                    String worldName = "Plot_" + ID;
                    if((BTEPlotSystem.getMultiverseCore().getMVWorldManager().getMVWorld(worldName) != null) || BTEPlotSystem.getMultiverseCore().getMVWorldManager().getUnloadedWorlds().contains(worldName)) {
                        try {
                            PlotHandler.teleportPlayer(new Plot(ID), (Player) sender);
                        } catch (SQLException ex) {
                            sender.sendMessage(Utils.getErrorMessageFormat("An error occurred! Please try again!"));
                            ex.printStackTrace();
                        }
                    } else {
                        sender.sendMessage(Utils.getErrorMessageFormat("Could not find plot with ID #" + ID + "!"));
                    }
                } else {
                    sender.sendMessage(Utils.getErrorMessageFormat("§lUsage: §c/plot <ID>"));
                }
            }
        }
        return true;
    }
}
