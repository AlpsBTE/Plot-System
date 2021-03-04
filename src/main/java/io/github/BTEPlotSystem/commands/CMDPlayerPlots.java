package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.core.menus.PlayerPlotsMenu;
import github.BTEPlotSystem.utils.Builder;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class CMDPlayerPlots implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (sender instanceof Player){
            Player player = (Player)sender;
            try {
                if (player.hasPermission("alpsbte.review")) {
                    switch (strings.length){
                        case 0:
                            new PlayerPlotsMenu(new Builder(player.getUniqueId()));
                            break;
                        case 1:
                            Builder builder = Builder.getBuilderByName(strings[0]);
                            if (builder != null){
                                new PlayerPlotsMenu(builder,player);
                            } else {
                                player.sendMessage(Utils.getErrorMessageFormat("Could not find that user! Use /plots <Player>"));
                            }
                            break;
                        default:
                            player.sendMessage(Utils.getErrorMessageFormat("Invalid command usage! Use /plots <Player>"));
                            break;
                    }
                } else {
                    new PlayerPlotsMenu(new Builder(player.getUniqueId()));
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                player.sendMessage(Utils.getErrorMessageFormat("SQL Error!"));
            }
        }
        return true;
    }
}
