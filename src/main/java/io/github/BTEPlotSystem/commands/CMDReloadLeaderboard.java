package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.utils.Builder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class CMDReloadLeaderboard implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player){
            try {
                BTEPlotSystem.getPlugin().getScoreLeaderboard().updateHologram(Builder.getBuildersByScore(10));
                commandSender.sendMessage("§7>> §aReloaded Leaderboard");
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                commandSender.sendMessage("§4Error Reloading Score Leaderboard");
            }
        }
        return true;
    }
}
