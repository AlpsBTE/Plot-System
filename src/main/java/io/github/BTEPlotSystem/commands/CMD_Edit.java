package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.core.plots.Plot;
import github.BTEPlotSystem.core.plots.PlotHandler;
import github.BTEPlotSystem.core.plots.PlotManager;
import github.BTEPlotSystem.utils.Builder;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class CMD_Edit implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(sender instanceof Player) {
            if(sender.hasPermission("alpsbte.review") || sender.hasPermission("alpsbte.admin")) {
                Player player = (Player) sender;
                if(args.length == 0) {
                    if(PlotManager.isPlotWorld(player.getWorld())) {
                        try {
                            Plot plot = PlotManager.getPlotByWorld(player.getWorld());

                            if(plot.hasReviewerPerms()) {
                                plot.removeReviewerPerms().save();
                                sender.sendMessage(Utils.getInfoMessageFormat("§6Disabled §abuild permissions for Reviewers on Plot §6#" + plot.getID()));
                            } else {
                                plot.addReviewerPerms().save();
                                sender.sendMessage(Utils.getInfoMessageFormat("§6Enabled §abuild permissions for Reviewers on Plot §6#" + plot.getID()));
                            }
                        } catch (SQLException ex) {
                            sender.sendMessage(Utils.getErrorMessageFormat("An error occurred! Please try again!"));
                            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                        }
                    }
                } else {
                    sender.sendMessage(Utils.getErrorMessageFormat("§lUsage: §c/edit"));
                }
            }
        }
        return true;
    }
}
