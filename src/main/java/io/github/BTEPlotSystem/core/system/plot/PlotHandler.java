/*
 * The MIT License (MIT)
 *
 *  Copyright © 2021, Alps BTE <bte.atchli@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package github.BTEPlotSystem.core.system.plot;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.core.menus.CompanionMenu;
import github.BTEPlotSystem.core.menus.ReviewMenu;
import github.BTEPlotSystem.utils.Utils;
import github.BTEPlotSystem.utils.enums.Status;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.multiverse.io.FileUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class PlotHandler {

    public static void teleportPlayer(Plot plot, Player player) throws SQLException {
        player.sendMessage(Utils.getInfoMessageFormat("Teleporting to plot §6#" + plot.getID()));

        loadPlot(plot);
        player.teleport(getPlotSpawnPoint(plot));

        player.playSound(player.getLocation(), Utils.TeleportSound, 1, 1);
        player.setAllowFlight(true);
        player.setFlying(true);

        player.getInventory().setItem(8, CompanionMenu.getMenuItem());

        if(player.hasPermission("alpsbte.review")) {
            player.getInventory().setItem(7, ReviewMenu.getMenuItem());
        }

        sendLinkMessages(plot, player);

        if(plot.getBuilder().getUUID().equals(player.getUniqueId())) {
            plot.setLastActivity(false);
        }
    }

    public static void submitPlot(Plot plot) throws Exception {
        plot.setStatus(Status.unreviewed);

        if(plot.getPlotWorld() != null) {
            for(Player player : plot.getPlotWorld().getPlayers()) {
                player.teleport(Utils.getSpawnPoint());
            }
        }

        plot.removeBuilderPerms(plot.getBuilder().getUUID()).save();
    }

    public static void undoSubmit(Plot plot) throws SQLException {
        plot.setStatus(Status.unfinished);

        plot.addBuilderPerms(plot.getBuilder().getUUID()).save();
    }

    public static void abandonPlot(Plot plot) throws Exception {
        if(plot.isReviewed()) {
            try (Connection con = DatabaseConnection.getConnection()) {
                PreparedStatement ps = con.prepareStatement("DELETE FROM reviews WHERE id_review = ?");
                ps.setInt(1, plot.getReview().getReviewID());
                ps.executeUpdate();

                ps = con.prepareStatement("UPDATE plots SET idreview = DEFAULT(idreview) WHERE idplot = ?");
                ps.setInt(1, plot.getID());
                ps.executeUpdate();
            }
        }

        loadPlot(plot); // Load Plot to be listed by Multiverse
        for(Player player : plot.getPlotWorld().getPlayers()) {
            player.teleport(Utils.getSpawnPoint());
        }

        plot.getBuilder().removePlot(plot.getSlot());
        plot.setBuilder(null);
        plot.setLastActivity(true);
        plot.setScore(-1);
        plot.setStatus(Status.unclaimed);

        BTEPlotSystem.getMultiverseCore().getMVWorldManager().deleteWorld(plot.getWorldName(), true, true);
        BTEPlotSystem.getMultiverseCore().saveWorldConfig();

        FileUtils.deleteDirectory(new File(PlotManager.getWorldGuardConfigPath(plot.getWorldName())));
        FileUtils.deleteDirectory(new File(PlotManager.getMultiverseInventoriesConfigPath(plot.getWorldName())));
    }

    public static void deletePlot(Plot plot) throws Exception {
        abandonPlot(plot);

        Files.deleteIfExists(Paths.get(PlotManager.getDefaultSchematicPath(),String.valueOf(plot.getCity().getID()), plot.getID() + ".schematic"));

        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM plots WHERE idplot = ?");
            ps.setInt(1, plot.getID());
            ps.execute();
        }
    }

    public static void loadPlot(Plot plot) {
        if(plot.getPlotWorld() == null) {
            BTEPlotSystem.getMultiverseCore().getMVWorldManager().loadWorld(plot.getWorldName());
        }
    }

    public static void unloadPlot(Plot plot) {
        if(plot.getPlotWorld() != null && plot.getPlotWorld().getPlayers().isEmpty()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(BTEPlotSystem.getPlugin(), () ->
                    BTEPlotSystem.getMultiverseCore().getMVWorldManager().unloadWorld(plot.getWorldName(), true), 20*3);
        }
    }

    public static Location getPlotSpawnPoint(Plot plot) {
        return new Location(plot.getPlotWorld(),
                (double) (PlotManager.getPlotSize(plot) / 2) + 0.5,
                30, // TODO: Fit Y value to schematic height to prevent collision
                (double) (PlotManager.getPlotSize(plot) / 2) + 0.5,
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

        try {
            tc[0].setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, plot.getGoogleMapsLink()));
            tc[1].setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, plot.getGoogleEarthLink()));
            tc[2].setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, plot.getOSMMapsLink()));
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQl error occurred!", ex);
        }

        tc[0].setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Google Maps").create()));
        tc[1].setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Google Earth Web").create()));
        tc[2].setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Open Street Map").create()));

        player.sendMessage("§7--------------------");
        player.spigot().sendMessage(tc[0]);
        player.spigot().sendMessage(tc[1]);
        player.spigot().sendMessage(tc[2]);
        player.sendMessage("§7--------------------");
    }

    public static void sendFeedbackMessage(List<Plot> plots, Player player) throws SQLException {
        player.sendMessage("§7--------------------");
        for(Plot plot : plots) {
            player.sendMessage("§aYour plot with the ID §6#" + plot.getID() + " §ahas been reviewed!");
            TextComponent tc = new TextComponent();
            tc.setText("§6Click Here §ato check your feedback.");
            tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/feedback " + plot.getID()));
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Feedback").create()));
            player.spigot().sendMessage(tc);

            if(plots.size() != plots.indexOf(plot) + 1) {
                player.sendMessage("");
            }

            plot.getReview().setFeedbackSent(true);
        }
        player.sendMessage("§7--------------------");
        player.playSound(player.getLocation(), Utils.FinishPlotSound, 1, 1);
    }

    public static void sendUnfinishedPlotReminderMessage(List<Plot> plots, Player player) {
        player.sendMessage("§aYou still have §6" + plots.size() + " §aunfinished plot" + (plots.size() <= 1 ? "!" : "s!"));
        TextComponent tc = new TextComponent();
        tc.setText("§6Click Here §ato open your plots menu.");
        tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/plots"));
        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Show my plots").create()));
        player.spigot().sendMessage(tc);
    }

    public static void sendUnreviewedPlotsReminderMessage(List<Plot> plots, Player player) {
        if(plots.size() <= 1) {
            player.sendMessage("§aThere is §6" + plots.size() + " §aunreviewed plot!");
        } else {
            player.sendMessage("§aThere are §6" + plots.size() + " §aunreviewed plots!");
        }

        TextComponent tc = new TextComponent();
        tc.setText("§6Click Here §ato open the review menu.");
        tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/review"));
        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Show unreviewed plots").create()));
        player.spigot().sendMessage(tc);
    }
}
