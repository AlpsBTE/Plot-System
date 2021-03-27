package github.BTEPlotSystem.commands.plot;

import github.BTEPlotSystem.core.menus.PlayerPlotsMenu;
import github.BTEPlotSystem.core.system.Builder;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.logging.Level;

public class CMD_PlayerPlots implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender instanceof Player){
            if(sender.hasPermission("alpsbte.plot")) {
                Player player = (Player)sender;
                try {
                    if(args.length >= 1) {
                        Builder builder = Builder.getBuilderByName(args[0]);
                        if (builder != null){
                            new PlayerPlotsMenu(builder, player);
                        } else {
                            player.sendMessage(Utils.getErrorMessageFormat("Could not find that player!"));
                        }
                    } else {
                        new PlayerPlotsMenu(new Builder(player.getUniqueId()));
                    }
                } catch (SQLException ex) {
                    player.sendMessage(Utils.getErrorMessageFormat("An error occurred! Please try again!"));
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                }
            }
        }
        return true;
    }
}
