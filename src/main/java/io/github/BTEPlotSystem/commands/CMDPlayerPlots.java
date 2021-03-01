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

            switch (strings.length){
                case 0:
                    //Open own plots menu
                    break;
                case 1:
                    if (player.hasPermission("alpsbte.review")){
                        //open plots menu of the given player
                    }
                    break;
                default:
                    if (player.hasPermission("alpsbte.review")){
                        player.sendMessage(Utils.getErrorMessageFormat("Invalid command usage! Use /plots <Player>"));
                    } else {
                        //Open own plots menu
                    }
                    break;
            }
        }
        return true;
    }
}
