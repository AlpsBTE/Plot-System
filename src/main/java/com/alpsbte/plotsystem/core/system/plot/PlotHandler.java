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

package com.alpsbte.plotsystem.core.system.plot;

import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.system.Server;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.ftp.FTPManager;
import net.md_5.bungee.api.chat.*;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;

public class PlotHandler {

    public static void submitPlot(Plot plot) throws SQLException {
        plot.setStatus(Status.unreviewed);

        if(plot.getWorld().isWorldLoaded()) {
            for(Player player : plot.getWorld().getBukkitWorld().getPlayers()) {
                player.teleport(Utils.getSpawnLocation());
            }
        }

        plot.getPermissions().removeBuilderPerms(plot.getPlotOwner().getUUID()).save();
        if (plot.getPlotMembers().size() != 0) {
            for (Builder builder : plot.getPlotMembers()) {
                plot.getPermissions().removeBuilderPerms(builder.getUUID());
            }
        }
    }

    public static void undoSubmit(Plot plot) throws SQLException {
        plot.setStatus(Status.unfinished);

        plot.getPermissions().addBuilderPerms(plot.getPlotOwner().getUUID()).save();
    }

    public static boolean abandonPlot(Plot plot) {
        try {
            if (plot.getWorld().isWorldGenerated() && plot.getWorld().loadWorld()) {
                for (Player player : plot.getWorld().getBukkitWorld().getPlayers()) {
                    player.teleport(Utils.getSpawnLocation());
                }

                if (plot.getWorld().deleteWorld()) {
                    for (Builder builder : plot.getPlotMembers()) {
                        plot.removePlotMember(builder);
                    }

                    try {
                        CompletableFuture.runAsync(() -> {
                            try {
                                if (plot.isReviewed()) {
                                    DatabaseConnection.createStatement("UPDATE plotsystem_plots SET review_id = DEFAULT(review_id) WHERE id = ?")
                                            .setValue(plot.getID()).executeUpdate();

                                    DatabaseConnection.createStatement("DELETE FROM plotsystem_reviews WHERE id = ?")
                                            .setValue(plot.getReview().getReviewID()).executeUpdate();
                                }

                                plot.getPlotOwner().removePlot(plot.getSlot());
                                plot.setPlotOwner(null);
                                plot.setLastActivity(true);
                                plot.setTotalScore(-1);
                                plot.setStatus(Status.unclaimed);
                            } catch (SQLException ex) {
                                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                                throw new CompletionException(ex);
                            }
                        }).join();
                    } catch (CompletionException ex) {
                        return false;
                    }
                    return true;
                }
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
        Bukkit.getLogger().log(Level.WARNING, "Failed to abandon plot with the ID " + plot.getID() + "!");
        return false;
    }

    public static boolean deletePlot(Plot plot) throws SQLException {
        if (plot.getWorld().isWorldGenerated() && abandonPlot(plot)) {
            try {
                CompletableFuture.runAsync(() -> {
                    try {
                        Server plotServer = plot.getCity().getCountry().getServer();

                        Files.deleteIfExists(Paths.get(PlotManager.getDefaultSchematicPath(), String.valueOf(plotServer.getID()), "finishedSchematics", String.valueOf(plot.getCity().getID()), plot.getID() + ".schematic"));
                        Files.deleteIfExists(Paths.get(PlotManager.getDefaultSchematicPath(), String.valueOf(plotServer.getID()), String.valueOf(plot.getCity().getID()), plot.getID() + ".schematic"));

                        if (plotServer.getFTPConfiguration() != null) {
                            FTPManager.deleteSchematics(FTPManager.getFTPUrl(plotServer, plot.getCity().getID()), plot.getID() + ".schematic", false);
                        }

                        DatabaseConnection.createStatement("DELETE FROM plotsystem_plots WHERE id = ?")
                                .setValue(plot.getID()).executeUpdate();
                    } catch (IOException | SQLException | URISyntaxException ex) {
                        Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
                        throw new CompletionException(ex);
                    }
                });
            } catch (CompletionException ex) {
                return false;
            }
            return true;
        }
        Bukkit.getLogger().log(Level.WARNING, "Failed to delete plot with the ID " + plot.getID() + "!");
        return false;
    }

    public static void sendLinkMessages(Plot plot, Player player){
        TextComponent[] tc = new TextComponent[3];
        tc[0] = new TextComponent();
        tc[1] = new TextComponent();
        tc[2] = new TextComponent();

        tc[0].setText("§7§l> §7Click me to open the §aGoogle Maps §7link....");
        tc[1].setText("§7§l> §7Click me to open the §aGoogle Earth Web §7link....");
        tc[2].setText("§7§l> §7Click me to open the §aOpen Street Map §7link....");

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

        // Temporary fix for bedrock players
        String coords = null;
        try {
            String[] coordsSplit = plot.getGeoCoordinates().split(",");
            double lat = Double.parseDouble(coordsSplit[0]);
            double lon = Double.parseDouble(coordsSplit[1]);
            DecimalFormat df = new DecimalFormat("##.#####");
            df.setRoundingMode(RoundingMode.FLOOR);
            coords = "§a" + df.format(lat) + "§7, §a" + df.format(lon);
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
        }

        player.sendMessage("§8--------------------------");
        if (coords != null) player.sendMessage("§7Coords: " + coords);
        player.spigot().sendMessage(tc[0]);
        player.spigot().sendMessage(tc[1]);
        player.spigot().sendMessage(tc[2]);
        player.sendMessage("§8--------------------------");
    }

    public static void sendGroupTipMessage(Plot plot, Player player) {
        try {
            if (plot.getPlotMembers().size() == 0) {
                TextComponent tc = new TextComponent();
                tc.setText("§7§l> §7Want to play with your friends? Click §aHere....");
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/plot members " + plot.getID()));
                tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Manage Plot Members...").create()));

                player.spigot().sendMessage(tc);
                player.sendMessage("§8--------------------------");
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQl error occurred!", e);
        }
    }

    public static void sendFeedbackMessage(List<Plot> plots, Player player) throws SQLException {
        player.sendMessage("§8--------------------------");
        for(Plot plot : plots) {
            player.sendMessage("§7§l> §aYour plot with the ID §6#" + plot.getID() + " §ahas been reviewed!");
            TextComponent tc = new TextComponent();
            tc.setText("§6Click Here §ato check your feedback.");
            tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/plot feedback " + plot.getID()));
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Plot Feedback").create()));
            player.spigot().sendMessage(tc);

            if(plots.size() != plots.indexOf(plot) + 1) {
                player.sendMessage("");
            }
        }
        player.sendMessage("§8--------------------------");
        player.playSound(player.getLocation(), Utils.FinishPlotSound, 1, 1);
    }

    public static void sendUnfinishedPlotReminderMessage(List<Plot> plots, Player player) {
        player.sendMessage("§7§l> §aYou have §6" + plots.size() + " §aunfinished plot" + (plots.size() <= 1 ? "!" : "s!"));
        TextComponent tc = new TextComponent();
        tc.setText("§6Click Here §ato open your plots menu.");
        tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/plots"));
        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Show Plots").create()));
        player.spigot().sendMessage(tc);
    }

    public static void sendUnreviewedPlotsReminderMessage(List<Plot> plots, Player player) {
        if(plots.size() <= 1) {
            player.sendMessage("§7§l> §aThere is §6" + plots.size() + " §aunreviewed plot!");
        } else {
            player.sendMessage("§7§l> §aThere are §6" + plots.size() + " §aunreviewed plots!");
        }

        TextComponent tc = new TextComponent();
        tc.setText("§6Click Here §ato open the review menu.");
        tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/review"));
        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Show unreviewed plots").create()));
        player.spigot().sendMessage(tc);
    }
}
