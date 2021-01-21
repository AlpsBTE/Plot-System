package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.utils.conversion.CoordinateConversion;
import github.BTEPlotSystem.utils.conversion.projection.OutOfProjectionBoundsException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMDCoords implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {

        Player player = (Player)sender;
        try {
            double[] coords = CoordinateConversion.convertToGeo(player.getLocation().getX(), player.getLocation().getZ());

            System.out.println(CoordinateConversion.formatGeoCoordinates(coords));
            player.sendMessage(CoordinateConversion.formatGeoCoordinates(coords));
        } catch (OutOfProjectionBoundsException e) {
            e.printStackTrace();
        }

        return true;
    }
}
