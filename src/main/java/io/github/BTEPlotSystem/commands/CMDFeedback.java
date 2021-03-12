package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.core.menus.FeedbackMenu;
import github.BTEPlotSystem.core.plots.Plot;
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
                    try {
                        Plot plot = new Plot(Integer.parseInt(args[0]));

                        if(PlotManager.plotExists(plot.getID())) {
                            if(plot.isReviewed() || plot.wasRejected()) {
                                if(plot.getBuilder().getUUID().equals(((Player) sender).getUniqueId()) || sender.hasPermission("alpsbte.review")) {
                                    ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT idreview FROM plots WHERE idplot = '" + plot.getID() + "'");
                                    rs.next();

                                    new FeedbackMenu((Player) sender, rs.getInt(1));
                                } else {
                                    sender.sendMessage(Utils.getErrorMessageFormat("You don't have permission to see this feedback!"));
                                }
                            } else {
                                sender.sendMessage(Utils.getErrorMessageFormat("This plot has not yet been reviewed!"));
                            }
                        } else {
                            sender.sendMessage(Utils.getErrorMessageFormat("Could not find plot with ID #" + plot.getID() + "!"));
                        }
                    } catch (SQLException ex) {
                        sender.sendMessage(Utils.getErrorMessageFormat("An error occurred! Please try again!"));
                        Bukkit.getLogger().log(Level.SEVERE, "An SQL error occurred!", ex);
                    }
                } else {
                    sender.sendMessage(Utils.getErrorMessageFormat("Please enter a valid ID!"));
                }
            } else {
                sender.sendMessage(Utils.getErrorMessageFormat("§lUsage: §c/feedback <ID>"));
            }
        }
        return true;
    }
}
