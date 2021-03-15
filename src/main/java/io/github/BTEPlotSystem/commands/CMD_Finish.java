package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.plots.Plot;
import github.BTEPlotSystem.core.plots.PlotHandler;
import github.BTEPlotSystem.core.plots.PlotManager;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMD_Finish implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(sender instanceof Player) {
            if(sender.hasPermission("alpsbte.finish")) {
                Player player = (Player) sender;
                World playerWorld = player.getWorld();

                if(BTEPlotSystem.getMultiverseCore().getMVWorldManager().isMVWorld(playerWorld) && playerWorld.getName().startsWith("P-")) {
                    try {
                        Plot plot = PlotManager.getPlotByWorld(playerWorld);

                        if(plot.getBuilder().getUUID().equals(player.getUniqueId()) || player.hasPermission("alpsbte.review")) {
                            PlotHandler.finishPlot(plot);
                            Bukkit.broadcastMessage(Utils.getInfoMessageFormat("Plot §6#" + plot.getID() + " §aby §6" + plot.getBuilder().getName() + " §ahas been finished!"));
                            player.playSound(player.getLocation(), Utils.FinishPlotSound, 1, 1);
                        } else {
                            player.sendMessage(Utils.getErrorMessageFormat("You are not allowed to finish this plot!"));
                            player.playSound(player.getLocation(), Utils.AbandonPlotSound, 1, 1);
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
