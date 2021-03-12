package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.plots.Plot;
import github.BTEPlotSystem.core.plots.PlotHandler;
import github.BTEPlotSystem.core.plots.PlotManager;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMD_Abandon implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(sender.hasPermission("alpsbte.abandon")) {
            Player player = (Player) sender;
            World playerWorld = player.getWorld();

            if(BTEPlotSystem.getMultiverseCore().getMVWorldManager().isMVWorld(playerWorld) && playerWorld.getName().startsWith("P-")) {
                try {
                    Plot plot = PlotManager.getPlotByWorld(playerWorld);

                    if(plot.getBuilder().getUUID().equals(player.getUniqueId()) || sender.hasPermission("alpsbte.review")) {
                        PlotHandler.abandonPlot(plot);
                        player.sendMessage(Utils.getInfoMessageFormat("Abandoned plot with the ID §6#" + plot.getID()));
                        player.playSound(player.getLocation(), Utils.AbandonPlotSound, 1, 1);
                    } else {
                        player.sendMessage(Utils.getErrorMessageFormat("You are not allowed to abandon this plot!"));
                        return true;
                    }
                } catch (Exception ex) {
                    player.sendMessage(Utils.getErrorMessageFormat("An error occurred! Please try again!"));
                }
            } else {
                player.sendMessage(Utils.getErrorMessageFormat("An error occurred! Please try again!"));
            }
        }
        return true;
    }
}
