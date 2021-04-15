package github.BTEPlotSystem.commands;

import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.core.system.plot.PlotManager;
import github.BTEPlotSystem.utils.Utils;
import github.BTEPlotSystem.utils.conversion.CoordinateConversion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMD_Tpll implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(sender instanceof Player) {
            if(sender.hasPermission("alpsbte.plot")) {
                Player player = (Player) sender;
                World playerWorld = player.getWorld();

                try {
                    if (PlotManager.isPlotWorld(playerWorld)) {

                        // TODO: Support NSEW coordinate format
                        String[] splitCoords = args[0].split(",");
                        if (splitCoords.length == 2 && args.length < 3) {
                            args = splitCoords;
                        }
                        if (args[0].endsWith(",")) {
                            args[0] = args[0].substring(0, args[0].length() - 1);
                        }
                        if (args.length > 1 && args[1].endsWith(",")) {
                            args[1] = args[1].substring(0, args[1].length() - 1);
                        }
                        if (args.length != 2 && args.length != 3) {
                            player.sendMessage(Utils.getErrorMessageFormat("Usage: /tpll <lat> <lon>"));
                            return true;
                        }

                        double lon;
                        double lat;

                        try {
                            lat = Double.parseDouble(args[0]);
                            lon = Double.parseDouble(args[1]);
                        } catch (Exception e) {
                            player.sendMessage(Utils.getErrorMessageFormat("Usage: /tpll <lat> <lon>"));
                            return true;
                        }

                        // TODO: Allow teleportation only in the plot schematic region
                        // Get the terra coordinates from the irl coordinates
                        double[] terraCoords = CoordinateConversion.convertFromGeo(lon, lat);
                        System.out.println("Terra Coords: " + terraCoords.toString());

                        // Get plot, that the player is in
                        Plot plot = PlotManager.getPlotByWorld(playerWorld);

                        double[] plotCoords = PlotManager.convertTerraToPlotXZ(plot, terraCoords);
                        System.out.println("Plot Coords: " + plotCoords.toString());

                        // Convert terra coordinates to plot relative coordinates

                        // Get Highest Y
                        int highestY = 0;
                        Location block = new Location(playerWorld, plotCoords[0], 0, plotCoords[1]);
                        for (int i = 1; i < 256; i++) {
                            block.add(0,1,0);
                            if (!block.getBlock().isEmpty()){
                                highestY = i;
                            }
                        }
                        if (highestY < 10) { highestY = 10; }

                        player.teleport(new Location(playerWorld, plotCoords[0], highestY+1, plotCoords[1]));
                        player.sendMessage(Utils.getInfoMessageFormat("Teleporting...")); // TODO: Improve player output
                    } else {
                        player.sendMessage(Utils.getErrorMessageFormat("You can only use /tpll on a plot!"));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    sender.sendMessage(Utils.getErrorMessageFormat("§lUsage: §c/tpll <coordinates>"));
                }
            }
        }
        return true;
    }
}