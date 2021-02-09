package github.BTEPlotSystem.core.plots;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class PlotHandler {

    public static void TeleportPlayer(Plot plot, Player player) {
        player.sendMessage("§7>> §aTeleporting to plot §6#" + plot.getID());

        player.teleport(new Location(Bukkit.getWorld("Plot_" + plot.getID()),
                (double) (PlotManager.getPlotSize() / 2) + 0.5,
                30,
                (double) (PlotManager.getPlotSize() / 2) + 0.5,
                -90,
                90)
        );

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 5.0f, 1.0f);
        player.setGameMode(GameMode.CREATIVE);
        player.setFlying(true);

        sendLinkMessages(plot, player);
    }

    public static void sendLinkMessages(Plot plot, Player player){
        TextComponent[] tc = new TextComponent[3];
        tc[0] = new TextComponent();
        tc[1] = new TextComponent();
        tc[2] = new TextComponent();

        tc[0].setText("§7>> Click me to open the §aGoogle Maps §7link....");
        tc[1].setText("§7>> Click me to open the §aGoogle Earth §7link....");
        tc[2].setText("§7>> Click me to open the §aOpenStreetMap §7link....");

        tc[0].setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, plot.getGoogleMapsLink()));
        tc[1].setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, plot.getGoogleEarthLink()));
        tc[2].setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, plot.getOSMMapsLink()));

        tc[0].setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Click me").create()));
        tc[1].setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Click me").create()));
        tc[2].setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Click me").create()));

        player.sendMessage("§7--------------------");
        player.spigot().sendMessage(tc[0]);
        player.spigot().sendMessage(tc[1]);
        player.spigot().sendMessage(tc[2]);
        player.sendMessage("§7--------------------");
    }
}
