package github.BTEPlotSystem.commands.review;

import github.BTEPlotSystem.core.system.Review;
import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.core.system.plot.PlotManager;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.logging.Level;

public class CMD_UndoReview implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(sender.hasPermission("alpsbte.review")) {
            if(args.length == 1 && Utils.TryParseInt(args[0]) != null) {
                try {
                    int plotID = Integer.parseInt(args[0]);
                    if(PlotManager.plotExists(plotID)) {
                        Plot plot = new Plot(plotID);
                        if(plot.isReviewed() && !plot.wasRejected()) {
                            if(plot.getReview().getReviewer().getUUID().equals(((Player)sender).getUniqueId()) || sender.hasPermission("alpsbte.admin")) {
                                Review.undoReview(plot.getReview());
                                sender.sendMessage((Utils.getInfoMessageFormat("Plot §6#" + plot.getID() + " §aby §6" + plot.getBuilder().getName() + " §ahas been unreviewed!")));
                            } else {
                                sender.sendMessage(Utils.getErrorMessageFormat("You cannot undo a review that you haven't reviewed yourself!"));
                            }
                        } else {
                            sender.sendMessage(Utils.getErrorMessageFormat("Plot is either unclaimed or hasn't been reviewed yet!"));
                        }
                    } else {
                        sender.sendMessage(Utils.getErrorMessageFormat("This plot does not exist!"));
                    }
                } catch (SQLException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                    sender.sendMessage(Utils.getErrorMessageFormat(""));
                }
            } else {
                sender.sendMessage(Utils.getErrorMessageFormat("§lUsage: §c/undoReview <ID>"));
            }
        }
        return true;
    }
}
