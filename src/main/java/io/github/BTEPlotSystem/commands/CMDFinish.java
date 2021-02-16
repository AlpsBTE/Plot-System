package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.plots.Plot;
import github.BTEPlotSystem.core.plots.PlotHandler;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMDFinish implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(sender instanceof Player) {
            if(sender.hasPermission("alpsbte.finish")) {
                Player player = (Player) sender;
                World playerWorld = player.getWorld();

                if(BTEPlotSystem.getMultiverseCore().getMVWorldManager().isMVWorld(playerWorld) && playerWorld.getName().startsWith("Plot_")) {
                    try {
                        int ID = Integer.parseInt(playerWorld.getName().substring(5));
                        Plot plot = new Plot(ID);

                        if(plot.getBuilder().getUUID().equals(player.getUniqueId())) {
                            player.sendMessage("§7>> §aFinished plot with the ID §6#" + plot.getID());
                            PlotHandler.FinishPlot(plot);
                        } else {
                            player.sendMessage(Utils.getErrorMessageFormat("You are not allowed to finish this plot!"));
                            return true;
                        }
                    } catch (Exception ex) {
                        player.sendMessage(Utils.getErrorMessageFormat("An error occurred! Please try again!"));
                    }
                } else {
                    player.sendMessage(Utils.getErrorMessageFormat("An error occurred! Please try again!"));
                }
            }
        }
        return true;
    }
}
