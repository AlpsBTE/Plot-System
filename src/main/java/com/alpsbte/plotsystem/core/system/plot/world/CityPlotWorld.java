package com.alpsbte.plotsystem.core.system.plot.world;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.generator.loader.AbstractPlotLoader;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.google.common.annotations.Beta;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static net.kyori.adventure.text.Component.text;

public class CityPlotWorld extends PlotWorld {
    /**
     * Minimum build headroom required above the schematic minimum Y after applying the city height offset.
     * Used only when initializing the shared offset for a city world.
     */
    public static final int MIN_BUILDING_HEIGHT = 128;

    private static final ConcurrentHashMap<String, Object> HEIGHT_OFFSET_LOCKS = new ConcurrentHashMap<>();

    public CityPlotWorld(@NotNull Plot plot) {
        super("C-" + plot.getCityProject().getId(), plot);
    }

    private static NamespacedKey heightOffsetKey() {
        return new NamespacedKey(PlotSystem.getPlugin(), "cim_height_offset");
    }

    @Override
    public boolean teleportPlayer(@NotNull Player player) {
        if (!super.teleportPlayer(player)) return false;

        player.playSound(player.getLocation(), Utils.SoundUtils.TELEPORT_SOUND, 1, 1);
        player.setAllowFlight(true);
        player.setFlying(true);

        if (plot == null) return true;

        player.sendMessage(Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(player, LangPaths.Message.Info.TELEPORTING_PLOT, String.valueOf(plot.getId()))));

        Utils.updatePlayerInventorySlots(player);
        PlotUtils.ChatFormatting.sendLinkMessages(plot, player);

        if (!plot.getPlotOwner().getUUID().equals(player.getUniqueId())) return true;
        plot.setLastActivity(false);

        return true;
    }

    @Override
    public String getRegionName() {
        return super.getRegionName() + "-" + plot.getId();
    }


    @Beta
    @Override
    public int getPlotHeight() throws IOException {
        if (plot.getVersion() < 3) return getPlotHeightCentered();
        // Paste origin at schematicMinY + cityOffset so every plot in this city shares one Y frame:
        // finalY = terraY + cityOffset
        return plot.getSchematicMetadata().minimumPoint().y() + ensureHeightOffset();
    }

    @Beta
    @Override
    public int getPlotHeightCentered() throws IOException {
        int maxWorldHeight = getBukkitWorld() == null ? PlotWorld.MAX_WORLD_HEIGHT : getBukkitWorld().getMaxHeight() - 1;
        return Math.min(ensureHeightOffset() + plot.getSchematicMetadata().center().y(), maxWorldHeight);
    }

    /**
     * Returns the city-wide Y offset stored on this CIM world, initializing it on first use.
     * Defaults to the same remapping as {@link OnePlotWorld}: the first plot's schematic minimum
     * lands at {@link #MIN_WORLD_HEIGHT}. That offset is then reused for every plot in the city.
     *
     * @return additive Y offset applied to all plots in this city world
     * @throws IOException if schematic metadata cannot be read while computing the initial offset
     */
    @Beta
    public int ensureHeightOffset() throws IOException {
        World bukkitWorld = getBukkitWorld();
        AbstractPlot.SchematicMetadata metadata = plot.getSchematicMetadata();
        if (bukkitWorld == null) return computeHeightOffset(metadata, null);

        Integer existing = bukkitWorld.getPersistentDataContainer().get(heightOffsetKey(), PersistentDataType.INTEGER);
        if (existing != null) return existing;

        // Initialize on the main thread so PDC writes stay thread-safe and we never hold a lock
        // across a main-thread handoff (which could deadlock FAWE paste workers).
        try {
            return Utils.supplySync(() -> {
                World world = getBukkitWorld();
                if (world == null) return computeHeightOffset(metadata, null);

                Object lock = HEIGHT_OFFSET_LOCKS.computeIfAbsent(getWorldName(), ignored -> new Object());
                synchronized (lock) {
                    try {
                        Integer stored = world.getPersistentDataContainer().get(heightOffsetKey(), PersistentDataType.INTEGER);
                        if (stored != null) return stored;

                        int offset = computeHeightOffset(metadata, world);
                        world.getPersistentDataContainer().set(heightOffsetKey(), PersistentDataType.INTEGER, offset);
                        PlotSystem.getPlugin().getComponentLogger().info(
                                text("Initialized CIM height offset " + offset + " for world " + getWorldName())
                        );
                        return offset;
                    } finally {
                        HEIGHT_OFFSET_LOCKS.remove(getWorldName(), lock);
                    }
                }
            }).get();
        } catch (Exception exception) {
            throw new IOException("Could not initialize CIM height offset for world " + getWorldName(), exception);
        }
    }

    /**
     * Computes a shared Y offset for the city world.
     * Same baseline as one-plot worlds ({@code schematicMinY + offset = MIN_WORLD_HEIGHT}), then
     * clamps only if that would leave the schematic out of world bounds or without
     * {@link #MIN_BUILDING_HEIGHT} headroom.
     */
    @Beta
    public static int computeHeightOffset(@NotNull AbstractPlot.SchematicMetadata metadata, @Nullable World world) {
        int schematicMinY = metadata.minimumPoint().y();
        int worldMin = world != null ? world.getMinHeight() : -64;
        int worldMax = world != null ? world.getMaxHeight() - 1 : MAX_WORLD_HEIGHT + 64;

        // Match OnePlotWorld: paste the reference plot so its schematic min sits at MIN_WORLD_HEIGHT.
        int offset = MIN_WORLD_HEIGHT - schematicMinY;

        if (schematicMinY + offset < worldMin) {
            offset = worldMin - schematicMinY;
        }

        if (schematicMinY + offset + MIN_BUILDING_HEIGHT > worldMax) {
            offset = worldMax - MIN_BUILDING_HEIGHT - schematicMinY;
        }

        if (schematicMinY + offset < worldMin) {
            offset = worldMin - schematicMinY;
        }

        return offset;
    }

    /**
     * Gets all players located on the plot in the city plot world
     *
     * @return a list of players located on the plot
     */
    public List<Player> getPlayersOnPlot(AbstractPlot plot) {
        List<Player> players = new ArrayList<>();
        if (plot == null || !plot.getWorld().isWorldLoaded() || plot.getWorld().getBukkitWorld().getPlayers().isEmpty()) return players;

        for (Player player : plot.getWorld().getBukkitWorld().getPlayers()) {
            if (PlotUtils.isPlayerOnPlot(plot, player)) players.add(player);
        }
        return players;
    }

    @Override
    public boolean onAbandon() {
        if (!loadWorld()) {
            PlotSystem.getPlugin().getComponentLogger().warn(text("Could not load world!"));
            return false;
        }

        try {
            boolean regionsRemoved = Utils.supplySync(() -> {
                RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(getBukkitWorld()));
                if (regionManager == null) return false;

                if (regionManager.hasRegion(getRegionName())) regionManager.removeRegion(getRegionName());
                if (regionManager.hasRegion(getRegionName() + "-1")) regionManager.removeRegion(getRegionName() + "-1");
                regionManager.saveChanges();
                return true;
            }).get();
            if (!regionsRemoved) {
                PlotSystem.getPlugin().getComponentLogger().warn(text("Region Manager is null!"));
                return false;
            }
        } catch (Exception exception) {
            PlotSystem.getPlugin().getComponentLogger().error(text("Could not save WorldGuard regions!"), exception);
            return false;
        }

        // paste initial schematic to reset plot
        try {
            AbstractPlotLoader.runFaweBlocking(() ->
                    AbstractPlotLoader.pasteSchematic(
                            false,
                            PlotUtils.getOutlinesSchematicBytes(plot, plot.getInitialSchematicBytes()),
                            this,
                            true,
                            false
                    )
            );
        } catch (Exception e) {
            PlotSystem.getPlugin().getComponentLogger().error(text("Could not paste schematic!"), e);
            return false;
        }

        try {
            return Utils.supplySync(() -> {
                List<Player> playersToTeleport = new ArrayList<>(getPlayersOnPlot(plot));
                playersToTeleport.forEach(player -> player.teleport(Utils.getSpawnLocation()));
                return !isWorldLoaded() || unloadWorld(false);
            }).get() && super.onAbandon();
        } catch (Exception exception) {
            PlotSystem.getPlugin().getComponentLogger().error(text("Could not unload city plot world!"), exception);
            return false;
        }
    }
}
