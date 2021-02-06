package github.BTEPlotSystem.core.plots;

import com.sk89q.worldedit.Vector;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlotHandler {

    public static void TeleportPlayer(Plot plot, Player player) {
        Vector plotCoordinates = PlotManager.CalculatePlotCoordinates(plot.getID());
        player.teleport(new Location(player.getWorld(),
                plotCoordinates.getX() - (PlotManager.getPlotSize() / 2) + 0.5,
                plotCoordinates.getY() + 20,
                plotCoordinates.getZ() + (PlotManager.getPlotSize() / 2) + 0.5,
                -90,
                90));
    }
}
