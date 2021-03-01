package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMDPlayerPlots implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (sender instanceof Player){
            Player player = (Player)sender;

            if (player.hasPermission("alpsbte.review")) {
                switch (strings.length){
                    case 0:
                        //Open own plots menu
                        break;
                    case 1:
                        //open plots menu of the given player
                        break;
                    default:
                        player.sendMessage(Utils.getErrorMessageFormat("Invalid command usage! Use /plots <Player>"));
                        break;
                }
            } else {
                //Open own plots menu
            }

        }
        return true;
    }
}
