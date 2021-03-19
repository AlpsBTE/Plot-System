package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.core.plots.Plot;
import github.BTEPlotSystem.core.plots.PlotManager;
import github.BTEPlotSystem.utils.Utils;
import github.BTEPlotSystem.utils.enums.Status;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.logging.Level;

public class CMD_SendFeedback implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender instanceof Player){
            Player player = (Player)sender;
            if (player.hasPermission("alpsbte.review")){
                if (args.length >= 2){
                    if (Utils.TryParseInt(args[0]) != null){
                        try {
                            if(PlotManager.plotExists(Integer.parseInt(args[0]))) {
                                Plot plot = new Plot(Integer.parseInt(args[0]));
                                if (plot.isReviewed() || plot.wasRejected()) {
                                    if (plot.getReview().getReviewer().getUUID().equals(player.getUniqueId()) || player.hasPermission("alpsbte.admin")){

                                        StringBuilder feedback = new StringBuilder();

                                        for(int i = 2; i <= args.length; i++) {
                                            feedback.append(args.length == 2 ? "" : " ").append(args[i - 1]);
                                        }

                                        plot.getReview().setFeedback(feedback.toString());

                                        player.sendMessage(Utils.getInfoMessageFormat("The feedback for the plot §6#" + plot.getID() + " §ahas been updated!"));


                                    } else {
                                        sender.sendMessage(Utils.getErrorMessageFormat("You cannot send feedback to a plot that you haven't reviewed yourself!"));
                                    }
                                } else {
                                    sender.sendMessage(Utils.getErrorMessageFormat("Plot is either unclaimed or hasn't been reviewed yet!"));
                                }
                            } else {
                                sender.sendMessage(Utils.getErrorMessageFormat("This plot does not exist!"));
                            }
                        } catch (SQLException ex) {
                            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                        }
                    }
                } else {
                    sender.sendMessage(Utils.getErrorMessageFormat("§lUsage: §c/sendfeedback <ID> <Text>"));
                }
            }
        }
        return true;
    }
}
