package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.core.menus.FeedbackMenu;
import github.BTEPlotSystem.core.plots.PlotManager;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public class CMDFeedback implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(sender instanceof Player) {
            if(args.length == 1) {
                if(Utils.TryParseInt(args[0]) != null) {
                    int ID = Integer.parseInt(args[0]);

                    if(PlotManager.plotExists(ID)) {
                        try {
                            ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT idreview FROM plots WHERE idplot = '" + ID + "'");
                            rs.next();

                            new FeedbackMenu((Player) sender, rs.getInt(1));
                        } catch (SQLException ex) {
                            sender.sendMessage(Utils.getErrorMessageFormat("An error occurred! Please try again!"));
                            Bukkit.getLogger().log(Level.SEVERE, "An SQL error occurred!", ex);
                        }
                    } else {
                        sender.sendMessage(Utils.getErrorMessageFormat("Could not find plot with ID #" + ID + "!"));
                    }
                } else {
                    sender.sendMessage(Utils.getErrorMessageFormat("Please enter a valid ID!"));
                }
            }
        }
        return true;
    }
}
