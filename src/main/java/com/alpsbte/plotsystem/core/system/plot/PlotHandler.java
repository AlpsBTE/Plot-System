/*
 *  The MIT License (MIT)
 *
 *  Copyright © 2021-2025, Alps BTE <bte.atchli@gmail.com>
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
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.plot.generator.loader.AbstractPlotLoader;
import com.alpsbte.plotsystem.core.system.plot.generator.loader.DefaultPlotLoader;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.core.system.plot.world.CityPlotWorld;
import com.alpsbte.plotsystem.core.system.plot.world.OnePlotWorld;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Slot;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static net.kyori.adventure.text.Component.text;

public class PlotHandler {
    private PlotHandler() {}

    private static final Map<UUID, LocalDateTime> playerPlotGenerationHistory = new HashMap<>();

    public static boolean assignPlot(Builder builder, Plot plot) {
        Player player = builder.getPlayer();

        // Score Requirement met?
        if (PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.ENABLE_SCORE_REQUIREMENT) && !DataProvider.DIFFICULTY.builderMeetsRequirements(builder, plot.getDifficulty())) {
            player.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(player, LangPaths.Message.Error.PLAYER_NEEDS_HIGHER_SCORE)));
            player.playSound(player.getLocation(), Utils.SoundUtils.ERROR_SOUND, SoundCategory.MASTER, 1, 1, 0);
            return false;
        }

        // Slot available?
        Slot freeSlot = builder.getFreeSlot();
        if (freeSlot == null) {
            player.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(player, LangPaths.Message.Error.ALL_SLOTS_OCCUPIED)));
            player.playSound(player.getLocation(), Utils.SoundUtils.ERROR_SOUND, SoundCategory.MASTER, 1, 1, 0);
            return false;
        }

        // Assign
        if (!builder.setSlot(builder.getFreeSlot(), plot.getId())) return false;
        if (!plot.setStatus(Status.unfinished)) return false;
        return plot.setPlotOwner(builder);
    }

    public static boolean assignAndGeneratePlot(Builder builder, Plot plot) {
        PlotType type = builder.getPlotType();
        if (type.equals(PlotType.CITY_INSPIRATION_MODE) && ConfigUtil.getInstance().configs[0].getBoolean(ConfigPaths.DISABLE_CITY_INSPIRATION_MODE))
            type = PlotType.LOCAL_INSPIRATION_MODE;

        boolean successful = assignPlot(builder, plot);
        if (successful) generatePlot(builder, plot, type);

        return successful;
    }

    public static boolean assignAndGenerateRandomPlot(Builder builder, CityProject city, PlotDifficulty difficulty) {
        Plot randomPlot = DataProvider.PLOT.getPlots(city, difficulty, Status.unclaimed)
                .get(Utils.getRandom().nextInt(DataProvider.PLOT.getPlots(city, difficulty, Status.unclaimed).size()));
        return assignAndGeneratePlot(builder, randomPlot);
    }

    public static void generatePlot(Builder builder, Plot plot, PlotType type) {
        Player player = builder.getPlayer();

        // Cooldown
        if (playerPlotGenerationHistory.containsKey(builder.getUUID())) {
            if (!playerPlotGenerationHistory.get(builder.getUUID()).isBefore(LocalDateTime.now().minusSeconds(10))) {
                player.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(player, LangPaths.Message.Error.PLEASE_WAIT)));
                player.playSound(player.getLocation(), Utils.SoundUtils.ERROR_SOUND, SoundCategory.MASTER, 1, 1, 0);
                return;
            }
            playerPlotGenerationHistory.remove(builder.getUUID());
        }
        playerPlotGenerationHistory.put(builder.getUUID(), LocalDateTime.now());

        player.sendMessage(Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(player, LangPaths.Message.Info.CREATING_PLOT)));
        player.playSound(player.getLocation(), Utils.SoundUtils.CREATE_PLOT_SOUND, SoundCategory.MASTER, 1, 1, 0);

        new DefaultPlotLoader(plot, builder, type, PlotWorld.getByType(type, plot));
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean abandonPlot(AbstractPlot plot) {
        boolean successfullyAbandoned = plot.getWorld().onAbandon();
        if (!successfullyAbandoned) {
            PlotSystem.getPlugin().getComponentLogger().error(text("Failed to abandon plot with the ID " + plot.getId() + "!"));
            return false;
        }

        CompletableFuture.runAsync(() -> {
            if (plot.getPlotType() == PlotType.TUTORIAL) return;
            Plot dPlot = (Plot) plot;
            boolean successful;
            successful = DataProvider.REVIEW.removeAllReviewsOfPlot(dPlot.getId());

            for (Builder builder : dPlot.getPlotMembers()) {
                if (!successful) break;
                successful = dPlot.removePlotMember(builder);
            }

            if (successful && plot.getPlotOwner() != null) {
                PlotUtils.Cache.clearCache(plot.getPlotOwner().getUUID());
                successful = plot.getPlotOwner().setSlot(plot.getPlotOwner().getSlot(dPlot), -1);
            }

            if (successful) {
                successful = dPlot.setPlotOwner(null)
                        && dPlot.setLastActivity(true)
                        && dPlot.setStatus(Status.unclaimed)
                        && dPlot.setPlotType(PlotType.LOCAL_INSPIRATION_MODE);
            }

            successful = successful && DataProvider.PLOT.setCompletedSchematic(plot.getId(), null);
            if (!successful) PlotSystem.getPlugin().getComponentLogger().error(text("Failed to abandon plot with the ID " + plot.getId() + "!"));
        });
        return true;
    }

    public static boolean deletePlot(Plot plot) {
        if (!abandonPlot(plot)) {
            PlotSystem.getPlugin().getComponentLogger().warn(text("Failed to delete plot with the ID " + plot.getId() + "!"));
            return false;
        }
        CompletableFuture.runAsync(() -> {
            if (DataProvider.PLOT.deletePlot(plot.getId())) return;
            PlotSystem.getPlugin().getComponentLogger().warn(text("Failed to delete plot with the ID " + plot.getId() + " from the database!"));
        });
        return true;
    }

    public static void abandonInactivePlots() {
        FileConfiguration config = PlotSystem.getPlugin().getConfig();
        long inactivityIntervalDays = config.getLong(ConfigPaths.INACTIVITY_INTERVAL);
        long rejectedInactivityIntervalDays = (config.getLong(ConfigPaths.REJECTED_INACTIVITY_INTERVAL) != -1) ? config.getLong(ConfigPaths.REJECTED_INACTIVITY_INTERVAL) : inactivityIntervalDays;
        if (inactivityIntervalDays == -2 && rejectedInactivityIntervalDays == -2) return;

        for (Plot plot : DataProvider.PLOT.getPlots(Status.unfinished)) {
            LocalDate lastActivity = plot.getLastActivity();
            long interval = plot.isRejected() ? rejectedInactivityIntervalDays : inactivityIntervalDays;
            if (interval == -2 || lastActivity == null || lastActivity.plusDays(interval).isAfter(LocalDate.now())) continue;

            Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
                if (!abandonPlot(plot)) {
                    PlotSystem.getPlugin().getComponentLogger().warn(text("An error occurred while abandoning plot #" + plot.getId() + " due to inactivity!"));
                    return;
                }
                PlotSystem.getPlugin().getComponentLogger().info(text("Abandoned plot #" + plot.getId() + " due to inactivity!"));
            });
        }
    }

    public static void submitPlot(@NotNull Plot plot) {
        plot.setStatus(Status.unreviewed);

        if (plot.getWorld().isWorldLoaded()) {
            for (Player player : plot.getWorld() instanceof OnePlotWorld ? plot.getWorld().getBukkitWorld().getPlayers() : ((CityPlotWorld) plot.getWorld()).getPlayersOnPlot(plot)) {
                player.teleport(Utils.getSpawnLocation());
            }
        }

        plot.getPermissions().removeBuilderPerms(plot.getPlotOwner().getUUID()).save();
        if (!plot.getPlotMembers().isEmpty()) {
            for (Builder builder : plot.getPlotMembers()) {
                plot.getPermissions().removeBuilderPerms(builder.getUUID());
            }
        }
    }

    public static void undoSubmit(@NotNull Plot plot) {
        plot.setStatus(Status.unfinished);

        plot.getPermissions().addBuilderPerms(plot.getPlotOwner().getUUID()).save();
        if (!plot.getPlotMembers().isEmpty()) {
            for (Builder builder : plot.getPlotMembers()) {
                plot.getPermissions().addBuilderPerms(builder.getUUID());
            }
        }
    }

    public static void removePlayerFromGenerationHistory(UUID playerUuid) {
        playerPlotGenerationHistory.remove(playerUuid);
    }

    public static boolean savePlotAsSchematic(@NotNull Plot plot) throws IOException, WorldEditException, ExecutionException, InterruptedException {
        if (plot.getVersion() < 4) {
            PlotSystem.getPlugin().getComponentLogger().error(text("Saving schematics of legacy plots is no longer allowed!"));
            return false;
        }

        Clipboard clipboard;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(plot.getInitialSchematicBytes());
        try (ClipboardReader reader = AbstractPlot.CLIPBOARD_FORMAT.getReader(inputStream)) {
            clipboard = reader.read();
        }
        if (clipboard == null) return false;

        CuboidRegion cuboidRegion = PlotUtils.getPlotAsRegion(plot);
        if (cuboidRegion == null) return false;

        BlockVector3 plotCenter = plot.getCenter();

        // Get plot outline
        List<BlockVector2> plotOutlines = plot.getOutline();

        // Load finished plot region as cuboid region
        if (!plot.getWorld().loadWorld()) return false;
        com.sk89q.worldedit.world.World world = new BukkitWorld(plot.getWorld().getBukkitWorld());
        Polygonal2DRegion region = new Polygonal2DRegion(world, plotOutlines, cuboidRegion.getMinimumPoint().y(), cuboidRegion.getMaximumPoint().y());

        // Copy and write finished plot clipboard to schematic
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (Clipboard cb = new BlockArrayClipboard(region)) {
            cb.setOrigin(BlockVector3.at(plotCenter.x(), cuboidRegion.getMinimumY(), (double) plotCenter.z()));

            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(Objects.requireNonNull(region.getWorld()), region, cb, region.getMinimumPoint());
            Operations.complete(forwardExtentCopy);

            try (ClipboardWriter writer = AbstractPlot.CLIPBOARD_FORMAT.getWriter(outputStream)) {
                double initialY = clipboard.getRegion().getMinimumY();
                double offset = initialY - cuboidRegion.getMinimumY();
                writer.write(cb.transform(new AffineTransform().translate(Vector3.at(0, offset, 0))));
            }
        }

        // Set Completed Schematic
        boolean successful = DataProvider.PLOT.setCompletedSchematic(plot.getId(), outputStream.toByteArray());
        if (!successful) return false;

        // If plot was created in a void world, copy the result to the city world
        if (plot.getPlotType() != PlotType.CITY_INSPIRATION_MODE) {
            Utils.runSync(() -> {
                AbstractPlotLoader.pasteSchematic(null, outputStream.toByteArray(), new CityPlotWorld(plot), false);
                return null;
            }).get();
        }
        return true;
    }
}
