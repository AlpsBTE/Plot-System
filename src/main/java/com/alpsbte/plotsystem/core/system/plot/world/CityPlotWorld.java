package com.alpsbte.plotsystem.core.system.plot.world;

import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.fastasyncworldedit.core.extent.clipboard.CPUOptimizedClipboard;
import com.google.common.annotations.Beta;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CityPlotWorld extends PlotWorld {
    public CityPlotWorld(@NotNull Plot plot) {
        super("C-" + plot.getCityProject().getId(), plot);
    }

    @Override
    public boolean teleportPlayer(@NotNull Player player) {
        if (super.teleportPlayer(player)) {
            player.playSound(player.getLocation(), Utils.SoundUtils.TELEPORT_SOUND, 1, 1);
            player.setAllowFlight(true);
            player.setFlying(true);

            if (getPlot() != null) {
                player.sendMessage(Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(player, LangPaths.Message.Info.TELEPORTING_PLOT, String.valueOf(getPlot().getId()))));

                Utils.updatePlayerInventorySlots(player);
                PlotUtils.ChatFormatting.sendLinkMessages(getPlot(), player);

                if (getPlot().getPlotOwner().getUUID().equals(player.getUniqueId())) {
                    getPlot().setLastActivity(false);
                }
            }

            return true;
        }
        return false;
    }

    @Override
    public String getRegionName() {
        return super.getRegionName() + "-" + getPlot().getId();
    }


    @Beta
    @Override
    public int getPlotHeight() throws IOException {
        return getPlot().getVersion() >= 3 ? MIN_WORLD_HEIGHT + getWorldHeight() : getPlotHeightCentered();
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
        ByteArrayInputStream inputStream = new ByteArrayInputStream(getPlot().getInitialSchematicBytes());
        try (ClipboardReader reader = AbstractPlot.CLIPBOARD_FORMAT.getReader(inputStream)) {
            clipboard = reader.read(null, dimensions -> new CPUOptimizedClipboard(
                    new CuboidRegion(null, BlockVector3.ZERO, dimensions.subtract(BlockVector3.ONE))
            ));
        }
        if (clipboard != null) {
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
        throw new IOException("A Plot's Outline schematic fails to load, cannot get clipboard.");
    }

    /**
     * Gets all players located on the plot in the city plot world
     *
     * @return a list of players located on the plot
     */
    public List<Player> getPlayersOnPlot() {
        List<Player> players = new ArrayList<>();
        if (getPlot() != null && getPlot().getWorld().isWorldLoaded() && !getPlot().getWorld().getBukkitWorld().getPlayers().isEmpty()) {
            for (Player player : getPlot().getWorld().getBukkitWorld().getPlayers()) {
                if (PlotUtils.isPlayerOnPlot(getPlot(), player)) players.add(player);
            }
            return players;
        }
        return players;
    }
}