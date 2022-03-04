package com.alpsbte.plotsystem.core.commands.plot;

import com.alpsbte.plotsystem.core.commands.BaseCommand;
import com.alpsbte.plotsystem.core.commands.SubCommand;
import com.alpsbte.plotsystem.core.menus.PlotMemberMenu;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.logging.Level;

public class CMD_Plot_Members extends SubCommand {

    public CMD_Plot_Members(BaseCommand baseCommand) {
        super(baseCommand);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        try {
            if (getPlayer(sender) != null) {
                Plot plot;
                // Get Plot
                if (args.length > 0 && Utils.TryParseInt(args[0]) != null) {
                    //plot members <id>
                    int plotID = Integer.parseInt(args[0]);
                    if (PlotManager.plotExists(plotID)) {
                        plot = new Plot(plotID);
                    } else {
                        sender.sendMessage(Utils.getErrorMessageFormat("This plot does not exist!"));
                        return;
                    }
                } else if (PlotManager.isPlotWorld(getPlayer(sender).getWorld())) {
                    //plot members
                    plot = PlotManager.getPlotByWorld(getPlayer(sender).getWorld());
                } else {
                    sendInfo(sender);
                    return;
                }

                if(plot.getPlotOwner().getUUID().equals(getPlayer(sender).getUniqueId()) || getPlayer(sender).hasPermission("plotsystem.admin")) {
                    new PlotMemberMenu(plot,getPlayer(sender));
                } else {
                    sender.sendMessage(Utils.getErrorMessageFormat("You don't have permission to manage this plot's members!"));
                }
            } else {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "This command can only be used as a player!");
            }
        } catch (SQLException ex) {
            sender.sendMessage(Utils.getErrorMessageFormat("An error occurred while executing command!"));
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
    }

    @Override
    public String[] getNames() {
        return new String[] { "members" };
    }

    @Override
    public String getDescription() {
        return "Opens the plot member menu.";
    }

    @Override
    public String[] getParameter() {
        return new String[] { "ID" };
    }

    @Override
    public String getPermission() {
        return "plotsystem.plot.members";
    }
}
