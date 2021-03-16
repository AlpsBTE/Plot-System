package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.core.plots.Plot;
import github.BTEPlotSystem.utils.Builder;
import github.BTEPlotSystem.utils.Utils;
import github.BTEPlotSystem.utils.enums.Status;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class CMD_SendFeedback implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender instanceof Player){
            Player player = (Player)sender;
            if (player.hasPermission("alpsbte.review")){
                if (args.length == 2){
                    if (Utils.TryParseInt(args[0]) != null){
                        try {
                            Plot plot = new Plot(Integer.parseInt(args[0]));
                            if (plot.getReview().getReviewer().equals(new Builder(player.getUniqueId())) || player.hasPermission("alpsbte.admin")){
                                if (plot.getStatus().equals(Status.complete) || plot.getStatus().equals(Status.unfinished) && plot.isReviewed()){
                                    plot.getReview().setFeedback(args[1]);
                                    player.sendMessage(Utils.getInfoMessageFormat("The feedback for your plot #"+plot.getID()+" has been updated! [Click to view]"));
                                    //TODO: Click event
                                } else {
                                    Error(player,"Plot is either unclaimed or hasn't been reviewed yet!");
                                }
                            } else {
                                Error(player,"You cannot send feedback to a plot that you haven't reviewed yourself!");
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                            Error(player,"SQL Error! /sendfeedback <id> <text>");
                        }
                    }
                } else {
                    Error(player,"Invalid arguments! /sendfeedback <id> <text>");
                }
            }
        }
        return true;
    }

    private void Error(Player player, String text){
        player.sendMessage(Utils.getErrorMessageFormat(text));
        player.playSound(player.getLocation(), Utils.ErrorSound, 1, 1);
    }
}
