package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.holograms.HolographicDisplay;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class CMDReloadHolograms implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender instanceof Player){
            if (sender.hasPermission("alpsbte.hologram")){
                Player player = (Player) sender;
                try {
                    BTEPlotSystem.getHolograms().forEach(HolographicDisplay::updateLeaderboard);
                    player.sendMessage(Utils.getInfoMessageFormat("Successfully reloaded holograms!"));
                    player.playSound(player.getLocation(), Utils.Done, 1, 1);
                } catch (Exception ex) {
                    player.sendMessage(Utils.getErrorMessageFormat("An error occurred while reloading holograms!"));
                    player.playSound(player.getLocation(), Utils.ErrorSound, 1, 1);
                    Bukkit.getLogger().log(Level.SEVERE, "An error occurred while reloading holograms!", ex);
                }
            }
        }
        return true;
    }
}
