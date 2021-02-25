package github.BTEPlotSystem.core.plots;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.core.menus.CompanionMenu;
import github.BTEPlotSystem.utils.Utils;
import github.BTEPlotSystem.utils.enums.Status;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.util.logging.Level;

public class PlotHandler {

    public static void teleportPlayer(Plot plot, Player player) {
        player.sendMessage("§7>> §aTeleporting to plot §6#" + plot.getID());

        String worldName = "Plot_" + plot.getID();
        if(Bukkit.getWorld(worldName) == null) {
            BTEPlotSystem.getMultiverseCore().getMVWorldManager().loadWorld(worldName);
        }

        player.teleport(getPlotSpawnPoint(Bukkit.getWorld(worldName)));

        player.playSound(player.getLocation(), Utils.TeleportSound, 1, 1);
        player.setAllowFlight(true);
        player.setFlying(true);

        player.getInventory().setItem(8, CompanionMenu.getItem());

        sendLinkMessages(plot, player);
    }

    public static void finishPlot(Plot plot) throws Exception {
        plot.setStatus(Status.unreviewed);

        RegionContainer container = WorldGuardPlugin.inst().getRegionContainer();
        RegionManager regionManager = container.get(Bukkit.getWorld("Plot_" + plot.getID()));
        ProtectedRegion region = regionManager.getApplicableRegions(plot.getBuilder().getPlayer().getLocation()).getRegions().stream().findFirst().get();
        region.getOwners().removePlayer(plot.getBuilder().getUUID());

        String worldName = "Plot_" + plot.getID();
        if(Bukkit.getWorld(worldName) != null) {
            for(Player player : Bukkit.getWorld(worldName).getPlayers()) {
                player.teleport(Utils.getSpawnPoint());
            }
        }
    }

    public static void abandonPlot(Plot plot) throws Exception {
        plot.setStatus(Status.unclaimed);

        String worldName = "Plot_" + plot.getID();
        if(Bukkit.getWorld(worldName) != null) {
            for(Player player : Bukkit.getWorld(worldName).getPlayers()) {
                player.teleport(Utils.getSpawnPoint());
            }
        }

        plot.getBuilder().removePlot(plot.getSlot());
        plot.setBuilder(null);
        BTEPlotSystem.getMultiverseCore().getMVWorldManager().deleteWorld(worldName, true, true);
        BTEPlotSystem.getMultiverseCore().getMVWorldManager().removeWorldFromConfig(worldName);
    }

    public static void deletePlot(Plot plot) throws Exception {
        abandonPlot(plot);

        Files.deleteIfExists(Paths.get(PlotManager.getSchematicPath(),String.valueOf(plot.getCity().getID()), plot.getID() + ".schematic"));

        String query = "DELETE FROM plots WHERE idplot = '" + plot.getID() + "'";
        PreparedStatement statement = DatabaseConnection.prepareStatement(query);
        statement.execute();
    }

    public static void unloadPlot(Player player) {
        World world = player.getWorld();
        if(world.getName().startsWith("Plot_") && world.getPlayers().size() - 1 == 0) {
            try {
                Bukkit.getScheduler().scheduleSyncRepeatingTask(BTEPlotSystem.getPlugin(), () -> Bukkit.getServer().unloadWorld(world, true), 1, 20*3);
            } catch (Exception ex) {
                Bukkit.getLogger().log(Level.SEVERE, "An error occurred while unloading plot world!", ex);
            }
        }
    }

    public static Location getPlotSpawnPoint(World world) {
        return new Location(world,
                (double) (PlotManager.getPlotSize() / 2) + 0.5,
                30,
                (double) (PlotManager.getPlotSize() / 2) + 0.5,
                -90,
                90);
    }

    public static void sendLinkMessages(Plot plot, Player player){
        TextComponent[] tc = new TextComponent[3];
        tc[0] = new TextComponent();
        tc[1] = new TextComponent();
        tc[2] = new TextComponent();

        tc[0].setText("§7>> Click me to open the §aGoogle Maps §7link....");
        tc[1].setText("§7>> Click me to open the §aGoogle Earth Web §7link....");
        tc[2].setText("§7>> Click me to open the §aOpen Street Map §7link....");

        tc[0].setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, plot.getGoogleMapsLink()));
        tc[1].setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, plot.getGoogleEarthLink()));
        tc[2].setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, plot.getOSMMapsLink()));

        tc[0].setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Google Maps").create()));
        tc[1].setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Google Earth Web").create()));
        tc[2].setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Open Street Map").create()));

        player.sendMessage("§7--------------------");
        player.spigot().sendMessage(tc[0]);
        player.spigot().sendMessage(tc[1]);
        player.spigot().sendMessage(tc[2]);
        player.sendMessage("§7--------------------");
    }
}
