package github.BTEPlotSystem.commands.admin;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.holograms.HolographicDisplay;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

public class CMD_Reload implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender.hasPermission("alpsbte.admin")){
            try {
                BTEPlotSystem.getPlugin().saveConfig();
                BTEPlotSystem.getPlugin().reloadConfig();
                sender.sendMessage(Utils.getInfoMessageFormat("Successfully reloaded config!"));

                BTEPlotSystem.getHolograms().forEach(HolographicDisplay::updateLeaderboard);

                sender.sendMessage(Utils.getInfoMessageFormat("Successfully reloaded holograms!"));
            } catch (Exception ex) {
                sender.sendMessage(Utils.getErrorMessageFormat("An error occurred while reloading!"));
                Bukkit.getLogger().log(Level.SEVERE, "An error occurred while reloading!", ex);
            }
        }
        return true;
    }
}
