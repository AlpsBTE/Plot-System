package com.alpsbte.plotsystem.core.system.plot.world;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.generator.loader.AbstractPlotLoader;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.google.common.annotations.Beta;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
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
    public boolean loadWorld() {
        if (!super.loadWorld()) return false;
        try {
            Utils.runSync(() -> {
                enableAutoSave();
                return null;
            }).get();
        } catch (Exception exception) {
            PlotSystem.getPlugin().getComponentLogger().warn(text("Could not enable autosave for world " + getWorldName() + "!"), exception);
        }
        return true;
    }

    /**
     * City worlds stay loaded. Unloading them while FastAsyncWorldEdit is still sending chunks
     * makes that update fail. Autosave writes the world, so this does not call a manual save.
     */
    @Override
    public boolean unloadWorld(boolean movePlayers) {
        if (!isWorldLoaded()) return true;
        try {
            Utils.runSync(() -> {
                persistLoadedWorld(movePlayers);
                return null;
            }).get();
            return true;
        } catch (Exception exception) {
            PlotSystem.getPlugin().getComponentLogger().error(text("Could not update city world " + getWorldName() + "!"), exception);
            return false;
        }
    }

    private void persistLoadedWorld(boolean movePlayers) {
        if (!isWorldLoaded()) return;
        if (movePlayers) movePlayersToSpawn();
        enableAutoSave();
        saveRegions();
    }

    private void enableAutoSave() {
        World world = getBukkitWorld();
        if (world != null) world.setAutoSave(true);
    }

    private void movePlayersToSpawn() {
        if (getBukkitWorld().getPlayers().isEmpty()) return;
        for (Player player : List.copyOf(getBukkitWorld().getPlayers())) {
            player.teleport(Utils.getSpawnLocation());
        }
    }

    private void saveRegions() {
        RegionManager regions = regionManager();
        if (regions == null) return;
        try {
            regions.saveChanges();
        } catch (StorageException exception) {
            PlotSystem.getPlugin().getComponentLogger().error(text("Could not save WorldGuard regions for world " + getWorldName() + "!"), exception);
        }
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

    /**
     * Makes sure a visitor can enter this plot in the city world.
     * City inspiration plots already have this region. Plots built in a private world are only pasted
     * into the city, so the entry region may be missing and the global region would deny entry.
     * The region is created without owners, so visitors cannot build.
     *
     * @return true when the entry region exists or was created
     */
    public boolean ensureVisitorAccess() {
        if (!isWorldLoaded() && !loadWorld()) return false;
        if (plot.getCenter() == null) {
            PlotSystem.getPlugin().getComponentLogger().warn(text("Could not read the center of plot " + plot.getId() + "!"));
            return false;
        }

        try {
            return Boolean.TRUE.equals(Utils.supplySync(this::createVisitorRegionsIfMissing).join());
        } catch (Exception exception) {
            PlotSystem.getPlugin().getComponentLogger().error(text("Could not create the visit region for plot " + plot.getId() + "!"), exception);
            return false;
        }
    }

    private @Nullable RegionManager regionManager() {
        if (getBukkitWorld() == null) return null;
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        return regionContainer.get(BukkitAdapter.adapt(getBukkitWorld()));
    }

    private boolean createVisitorRegionsIfMissing() {
        RegionManager regionManager = regionManager();
        if (regionManager == null) {
            PlotSystem.getPlugin().getComponentLogger().warn(text("Region Manager is null!"));
            return false;
        }
        if (regionManager.hasRegion(getRegionName() + "-1")) return true;

        int minWorldHeight = getBukkitWorld().getMinHeight();
        int maxWorldHeight = getBukkitWorld().getMaxHeight() - 1;
        if (!regionManager.hasRegion(getRegionName())) {
            ProtectedRegion buildRegion = new ProtectedPolygonalRegion(getRegionName(), plot.getOutline(), minWorldHeight, maxWorldHeight);
            buildRegion.setPriority(100);
            buildRegion.setFlag(Flags.BUILD, StateFlag.State.ALLOW);
            buildRegion.setFlag(Flags.BUILD.getRegionGroupFlag(), RegionGroup.OWNERS);
            regionManager.addRegion(buildRegion);
        }

        CylinderRegion cylinderRegion = new CylinderRegion(
                BukkitAdapter.adapt(getBukkitWorld()),
                plot.getCenter(),
                Vector2.at(PLOT_SIZE, PLOT_SIZE),
                minWorldHeight,
                maxWorldHeight
        );
        ProtectedRegion entryRegion = new ProtectedPolygonalRegion(getRegionName() + "-1", cylinderRegion.polygonize(-1), minWorldHeight, maxWorldHeight);
        entryRegion.setPriority(50);
        entryRegion.setFlag(Flags.ENTRY, StateFlag.State.ALLOW);
        entryRegion.setFlag(Flags.ENTRY.getRegionGroupFlag(), RegionGroup.ALL);

        FileConfiguration config = ConfigUtil.getInstance().configs[1];
        List<String> blockedCommands = new ArrayList<>(config.getStringList(ConfigPaths.BLOCKED_COMMANDS_BUILDERS));
        blockedCommands.removeIf(command -> command.equals("/cmd1"));
        entryRegion.setFlag(Flags.BLOCKED_CMDS, new HashSet<>(blockedCommands));
        entryRegion.setFlag(Flags.BLOCKED_CMDS.getRegionGroupFlag(), RegionGroup.ALL);
        regionManager.addRegion(entryRegion);

        try {
            regionManager.saveChanges();
        } catch (StorageException exception) {
            PlotSystem.getPlugin().getComponentLogger().error(text("Could not save the visit region for plot " + plot.getId() + "!"), exception);
            return false;
        }
        return true;
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

        // Reset the plot outline to its initial terrain. Clear only the schematic Y band
        // (see pasteSchematic), then paste solids with the city height offset. Pasting
        // schematic air into a shared city world carves voids into neighboring plots.
        try {
            AbstractPlotLoader.runFaweBlocking(() ->
                    AbstractPlotLoader.pasteSchematic(
                            true,
                            PlotUtils.getOutlinesSchematicBytes(plot, plot.getInitialSchematicBytes()),
                            this,
                            true,
                            true
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
