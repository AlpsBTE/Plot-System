package com.alpsbte.plotsystem.core.system.plot.world;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.generator.loader.AbstractPlotLoader;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.google.common.annotations.Beta;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.Component.text;

public class CityPlotWorld extends PlotWorld {
    public CityPlotWorld(@NotNull Plot plot) {
        super("C-" + plot.getCityProject().getId(), plot);
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
        return plot.getVersion() >= 3 ? MIN_WORLD_HEIGHT + getWorldHeight() : getPlotHeightCentered();
    }

    @Beta
    @Override
    public int getPlotHeightCentered() throws IOException {
        return Math.min(MIN_WORLD_HEIGHT + getWorldHeight() + super.getPlotHeightCentered(), PlotWorld.MAX_WORLD_HEIGHT);
    }

    /**
     * Calculate additional height for the plot
     *
     * @return The origin plot height from schematic if it is in plot world boundary, else 0
     * @throws IOException if the outline schematic fails to load
     */
    @Beta
    public int getWorldHeight() throws IOException {
        Clipboard clipboard;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(plot.getInitialSchematicBytes());
        try (ClipboardReader reader = AbstractPlot.CLIPBOARD_FORMAT.getReader(inputStream)) {
            clipboard = reader.read();
        }
        if (clipboard == null) throw new IOException("A Plot's Outline schematic fails to load, cannot get clipboard.");

        int plotHeight = clipboard.getMinimumPoint().y();

            // Minimum building height for a plot (this should be configurable depending on minecraft build limit)
            // This is in the case that a plot is created at y level 300 where the max build limit is 318,
            // so you don't want builder to only be able to build for 18 blocks
            int minBuildingHeight = 128;

            // Negative y level of the current minecraft version (1.21)
            // Additional ground layer the plot use to save as schematic need to be included for plot's y-level
            int groundLayer = 64;

        // Plots created outside of vanilla build limit or the build-able height is too small
        if (plotHeight + groundLayer < MIN_WORLD_HEIGHT + groundLayer
                || plotHeight + groundLayer + minBuildingHeight > MAX_WORLD_HEIGHT + groundLayer)
            return 0; // throw new IOException("Plot height is out of range.");
        return plotHeight;
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
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        if (!loadWorld()) {
            PlotSystem.getPlugin().getComponentLogger().warn(text("Could not load world!"));
            return false;
        }

        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(getBukkitWorld()));
        if (regionManager == null) {
            PlotSystem.getPlugin().getComponentLogger().warn(text("Region Manager is null!"));
            return false;
        }

        for (Builder builder : ((Plot) plot).getPlotMembers()) {
            ((Plot) plot).removePlotMember(builder);
        }

        if (regionManager.hasRegion(getRegionName())) regionManager.removeRegion(getRegionName());
        if (regionManager.hasRegion(getRegionName() + "-1")) regionManager.removeRegion(getRegionName() + "-1");

        // paste initial schematic to reset plot
        try {
            AbstractPlotLoader.pasteSchematic(null, PlotUtils.getOutlinesSchematicBytes(plot, plot.getInitialSchematicBytes(), getBukkitWorld()), this, true);
        } catch (IOException e) {
            PlotSystem.getPlugin().getComponentLogger().error(text("Could not paste schematic!"), e);
        }

        List<Player> playersToTeleport = new ArrayList<>(getPlayersOnPlot(plot));
        playersToTeleport.forEach(p -> p.teleport(Utils.getSpawnLocation()));

        if (isWorldLoaded()) unloadWorld(false);
        return super.onAbandon();
    }
}