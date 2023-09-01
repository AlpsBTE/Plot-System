/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
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

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.Server;
import com.alpsbte.plotsystem.core.system.plot.generator.AbstractPlotGenerator;
import com.alpsbte.plotsystem.core.system.plot.world.CityPlotWorld;
import com.alpsbte.plotsystem.core.system.plot.world.OnePlotWorld;
import com.alpsbte.plotsystem.utils.ShortLink;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.FTPManager;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;

public class PlotHandler {

    public static void submitPlot(Plot plot) throws SQLException {
        plot.setStatus(Status.unreviewed);

        if(plot.getWorld().isWorldLoaded()) {
            for(Player player : plot.getWorld() instanceof OnePlotWorld ? plot.getWorld().getBukkitWorld().getPlayers() : ((CityPlotWorld) plot.getWorld()).getPlayersOnPlot()) {
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
        if (plot.getPlotMembers().size() != 0) {
            for (Builder builder : plot.getPlotMembers()) {
                plot.getPermissions().addBuilderPerms(builder.getUUID());
            }
        }
    }

    public static boolean abandonPlot(Plot plot) {
        try {
            if (plot.getWorld() instanceof OnePlotWorld) {
                if (plot.getWorld().isWorldGenerated()) {
                    if (plot.getWorld().isWorldLoaded()) {
                        for(Player player : plot.getWorld().getBukkitWorld().getPlayers()) {
                            player.teleport(Utils.getSpawnLocation());
                        }
                    }
                    if (!plot.getWorld().deleteWorld()) Bukkit.getLogger().log(Level.WARNING, "Could not delete plot world " + plot.getWorld().getWorldName() + "!");
                }
            } else {
                RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();

                if (plot.getWorld().loadWorld()) {
                    CityPlotWorld world = plot.getWorld();
                    List<Player> playersToTeleport = new ArrayList<>(world.getPlayersOnPlot());

                    RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(world.getBukkitWorld()));
                    if (regionManager != null) {
                        for (Builder builder : plot.getPlotMembers()) {
                            plot.removePlotMember(builder);
                        }

                        if (regionManager.hasRegion(world.getRegionName())) regionManager.removeRegion(world.getRegionName());
                        if (regionManager.hasRegion(world.getRegionName() + "-1")) regionManager.removeRegion(world.getRegionName() + "-1");

                        AbstractPlotGenerator.pasteSchematic(null, plot.getOutlinesSchematic(), world, true);
                    } else Bukkit.getLogger().log(Level.WARNING, "Region Manager is null!");

                    playersToTeleport.forEach(p -> p.teleport(Utils.getSpawnLocation()));
                    if (plot.getWorld().isWorldLoaded()) plot.getWorld().unloadWorld(false);
                }
            }
        } catch (SQLException | IOException | WorldEditException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to abandon plot with the ID " + plot.getID() + "!", ex);
            return false;
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

                    if (plot.getPlotOwner() != null) {
                        PlotManager.clearCache(plot.getPlotOwner().getUUID());
                        plot.getPlotOwner().removePlot(plot.getSlot());
                    }
                    plot.setPlotOwner(null);
                    plot.setLastActivity(true);
                    plot.setTotalScore(-1);
                    plot.setStatus(Status.unclaimed);
                    plot.setPlotType(PlotType.LOCAL_INSPIRATION_MODE);
                } catch (SQLException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                    throw new CompletionException(ex);
                }
            }).join();
        } catch (CompletionException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to abandon plot with the ID " + plot.getID() + "!", ex);
            return false;
        }
        return true;
    }

    public static boolean deletePlot(Plot plot) throws SQLException {
        if (abandonPlot(plot)) {
            try {
                CompletableFuture.runAsync(() -> {
                    try {
                        Server plotServer = plot.getCity().getCountry().getServer();

                        Files.deleteIfExists(Paths.get(PlotManager.getDefaultSchematicPath(), String.valueOf(plotServer.getID()), "finishedSchematics", String.valueOf(plot.getCity().getID()), plot.getID() + ".schematic"));
                        Files.deleteIfExists(Paths.get(PlotManager.getDefaultSchematicPath(), String.valueOf(plotServer.getID()), String.valueOf(plot.getCity().getID()), plot.getID() + ".schematic"));
                        Files.deleteIfExists(Paths.get(PlotManager.getDefaultSchematicPath(), String.valueOf(plotServer.getID()), String.valueOf(plot.getCity().getID()), plot.getID() + "-env.schematic"));


                        Files.deleteIfExists(Paths.get(PlotManager.getDefaultSchematicPath(), String.valueOf(plotServer.getID()), "finishedSchematics", String.valueOf(plot.getCity().getID()), plot.getID() + ".schem"));
                        Files.deleteIfExists(Paths.get(PlotManager.getDefaultSchematicPath(), String.valueOf(plotServer.getID()), String.valueOf(plot.getCity().getID()), plot.getID() + ".schem"));
                        Files.deleteIfExists(Paths.get(PlotManager.getDefaultSchematicPath(), String.valueOf(plotServer.getID()), String.valueOf(plot.getCity().getID()), plot.getID() + "-env.schem"));

                        if (plotServer.getFTPConfiguration() != null) {
                            FTPManager.deleteSchematic(FTPManager.getFTPUrl(plotServer, plot.getCity().getID()), plot.getID() + ".schematic");
                            FTPManager.deleteSchematic(FTPManager.getFTPUrl(plotServer, plot.getCity().getID()).replaceFirst("finishedSchematics/", ""), plot.getID() + ".schematic");
                            FTPManager.deleteSchematic(FTPManager.getFTPUrl(plotServer, plot.getCity().getID()).replaceFirst("finishedSchematics/", ""), plot.getID() + "-env.schematic");

                            FTPManager.deleteSchematic(FTPManager.getFTPUrl(plotServer, plot.getCity().getID()), plot.getID() + ".schem");
                            FTPManager.deleteSchematic(FTPManager.getFTPUrl(plotServer, plot.getCity().getID()).replaceFirst("finishedSchematics/", ""), plot.getID() + ".schem");
                            FTPManager.deleteSchematic(FTPManager.getFTPUrl(plotServer, plot.getCity().getID()).replaceFirst("finishedSchematics/", ""), plot.getID() + "-env.schem");
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
        Bukkit.getScheduler().runTaskAsynchronously(PlotSystem.getPlugin(), () -> {
            TextComponent[] tc = new TextComponent[3];
            tc[0] = new TextComponent();
            tc[1] = new TextComponent();
            tc[2] = new TextComponent();

            try {
                if(PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.SHORTLINK_ENABLE)) {
                    tc[0].setText("§7§l> " + LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_OPEN_LINK_WITH_SHORTLINK, "Google Maps", ShortLink.generateShortLink(
                            plot.getGoogleMapsLink(),
                            PlotSystem.getPlugin().getConfig().getString(ConfigPaths.SHORTLINK_APIKEY),
                            PlotSystem.getPlugin().getConfig().getString(ConfigPaths.SHORTLINK_HOST))));

                    tc[1].setText("§7§l> " + LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_OPEN_LINK_WITH_SHORTLINK, "Google Earth Web", ShortLink.generateShortLink(
                            plot.getGoogleEarthLink(),
                            PlotSystem.getPlugin().getConfig().getString(ConfigPaths.SHORTLINK_APIKEY),
                            PlotSystem.getPlugin().getConfig().getString(ConfigPaths.SHORTLINK_HOST))));

                    tc[2].setText("§7§l> " + LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_OPEN_LINK_WITH_SHORTLINK, "Open Street Map", ShortLink.generateShortLink(
                            plot.getOSMMapsLink(),
                            PlotSystem.getPlugin().getConfig().getString(ConfigPaths.SHORTLINK_APIKEY),
                            PlotSystem.getPlugin().getConfig().getString(ConfigPaths.SHORTLINK_HOST))));
                } else {
                    tc[0].setText("§7§l> " + LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_OPEN_LINK, "Google Maps"));
                    tc[1].setText("§7§l> " + LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_OPEN_LINK, "Google Earth Web"));
                    tc[2].setText("§7§l> " + LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_OPEN_LINK, "Open Street Map"));
                }

                tc[0].setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, plot.getGoogleMapsLink()));
                tc[1].setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, plot.getGoogleEarthLink()));
                tc[2].setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, plot.getOSMMapsLink()));
            } catch (IOException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "An error occurred while creating shortlink!", ex);
            }

            tc[0].setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Google Maps").create()));
            tc[1].setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Google Earth Web").create()));
            tc[2].setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Open Street Map").create()));

            // Temporary fix for bedrock players
            String coords = null;
            try {
                String[] coordsSplit = plot.getGeoCoordinates().split(",");
                double lat = Double.parseDouble(coordsSplit[0]);
                double lon = Double.parseDouble(coordsSplit[1]);
                DecimalFormat df = new DecimalFormat("##.#####");
                df.setRoundingMode(RoundingMode.FLOOR);
                coords = "§a" + df.format(lat) + "§7, §a" + df.format(lon);
            } catch (IOException ex) {
                Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            }

            player.sendMessage("§8--------------------------");
            if (coords != null) player.sendMessage("§7Coords: " + coords);
            player.spigot().sendMessage(tc[0]);
            player.spigot().sendMessage(tc[1]);
            player.spigot().sendMessage(tc[2]);
            player.sendMessage("§8--------------------------");

            PlotHandler.sendGroupTipMessage(plot, player);
        });
    }

    public static void sendGroupTipMessage(Plot plot, Player player) {
        try {
            if (plot.getPlotMembers().size() == 0) {
                TextComponent tc = new TextComponent();
                tc.setText("§7§l> " + LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_PLAY_WITH_FRIENDS));
                tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/plot members " + plot.getID()));
                tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(LangUtil.getInstance().get(player, LangPaths.Plot.MEMBERS)).create()));

                player.spigot().sendMessage(tc);
                player.sendMessage("§8--------------------------");
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public static void sendFeedbackMessage(List<Plot> plots, Player player) throws SQLException {
        player.sendMessage("§8--------------------------");
        for(Plot plot : plots) {
            player.sendMessage("§7§l> " + LangUtil.getInstance().get(player, LangPaths.Message.Info.REVIEWED_PLOT, String.valueOf(plot.getID())));
            TextComponent tc = new TextComponent();
            tc.setText(LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_SHOW_FEEDBACK));
            tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/plot feedback " + plot.getID()));
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
                    LangUtil.getInstance().get(player, LangPaths.Plot.PLOT_NAME) + " " + LangUtil.getInstance().get(player, LangPaths.Review.FEEDBACK)).create()));
            player.spigot().sendMessage(tc);

            if(plots.size() != plots.indexOf(plot) + 1) {
                player.sendMessage("");
            }
        }
        player.sendMessage("§8--------------------------");
        player.playSound(player.getLocation(), Utils.SoundUtils.FINISH_PLOT_SOUND, 1, 1);
    }

    public static void sendUnfinishedPlotReminderMessage(List<Plot> plots, Player player) {
        player.sendMessage("§7§l> " + LangUtil.getInstance().get(player, plots.size() <= 1 ? LangPaths.Message.Info.UNFINISHED_PLOT : LangPaths.Message.Info.UNFINISHED_PLOTS, String.valueOf(plots.size())));
        TextComponent tc = new TextComponent();
        tc.setText(LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_SHOW_PLOTS));
        tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/plots"));
        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(LangUtil.getInstance().get(player, LangPaths.MenuTitle.SHOW_PLOTS)).create()));
        player.spigot().sendMessage(tc);
    }

    public static void sendUnreviewedPlotsReminderMessage(List<Plot> plots, Player player) {
        player.sendMessage("§7§l> " + LangUtil.getInstance().get(player, plots.size() <= 1 ?
                LangPaths.Message.Info.UNREVIEWED_PLOT :
                LangPaths.Message.Info.UNREVIEWED_PLOTS, String.valueOf(plots.size())));

        TextComponent tc = new TextComponent();
        tc.setText(LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_SHOW_OPEN_REVIEWS));
        tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/review"));
        tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(LangUtil.getInstance().get(player, LangPaths.MenuTitle.SHOW_PLOTS)).create()));
        player.spigot().sendMessage(tc);
    }
}
