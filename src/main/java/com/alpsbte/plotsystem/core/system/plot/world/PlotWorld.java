package com.alpsbte.plotsystem.core.system.plot.world;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.generator.AbstractPlotGenerator;
import com.alpsbte.plotsystem.core.system.plot.generator.DefaultPlotGenerator;
import com.alpsbte.plotsystem.core.system.plot.generator.RawPlotGenerator;
import com.alpsbte.plotsystem.utils.Utils;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Locale;
import java.util.logging.Level;

public class PlotWorld implements IPlotWorld {

    private final Plot plot;

    public PlotWorld(Plot plot) {
        this.plot = plot;
    }

    @Override
    public <T extends AbstractPlotGenerator> boolean generate(@NotNull Builder plotOwner, @NotNull Class<T> generator) {
        if (!isGenerated()) {
            if (generator.isInstance(DefaultPlotGenerator.class)) {
                new DefaultPlotGenerator(plot, plotOwner);
            } else if (generator.isInstance(RawPlotGenerator.class)) {
                new RawPlotGenerator(plot, plotOwner);
            } else return false;
            return true;
        }
        return false;
    }

    @Override
    public <T extends AbstractPlotGenerator> boolean regenerate(@NotNull Class<T> generator) {
        if (isGenerated() && unload(true)) {
            try {
                if (delete() && generate(plot.getPlotOwner(), generator))
                    return true;
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        }
        return false;
    }

    @Override
    public boolean delete() {
        if (isGenerated() && unload(true))
            return PlotSystem.DependencyManager.getMultiverseCore().getMVWorldManager().deleteWorld(getName(), true, true) &&
                    PlotSystem.DependencyManager.getMultiverseCore().saveWorldConfig();
        return false;
    }

    @Override
    public boolean load() {
        if (isGenerated())
            return PlotSystem.DependencyManager.getMultiverseCore().getMVWorldManager().loadWorld(getName()) || isLoaded();
        return false;
    }

    @Override
    public boolean unload(boolean movePlayers) {
        if(isLoaded()) {
            if (movePlayers && !getBukkitWorld().getPlayers().isEmpty()) {
                for (Player player : getBukkitWorld().getPlayers()) {
                    player.teleport(Utils.getSpawnLocation());
                }
            } else if (!movePlayers && !getBukkitWorld().getPlayers().isEmpty()) return false;

            Bukkit.getScheduler().scheduleSyncDelayedTask(PlotSystem.getPlugin(), (() ->
                    Bukkit.unloadWorld(getBukkitWorld(), true)), 60L);
            return !isLoaded();
        }
        return false;
    }

    @Override
    public World getBukkitWorld() {
        return Bukkit.getWorld(getName());
    }

    @Override
    public String getName() {
        return "P-" + plot.getID();
    }

    @Override
    public ProtectedRegion getProtectedRegion() {
        RegionContainer container = PlotSystem.DependencyManager.getWorldGuard().getRegionContainer();
        if (load()) {
            RegionManager regionManager = container.get(getBukkitWorld());
            if (regionManager != null) {
                return regionManager.getRegion(getName().toLowerCase(Locale.ROOT));
            } else Bukkit.getLogger().log(Level.WARNING, "Region manager is null");
        }
        return null;
    }

    @Override
    public boolean isLoaded() {
        return getBukkitWorld() != null;
    }

    @Override
    public boolean isGenerated() {
        return PlotSystem.DependencyManager.getMultiverseCore().getMVWorldManager().getMVWorld(getName()) != null || PlotSystem.DependencyManager.getMultiverseCore().getMVWorldManager().getUnloadedWorlds().contains(getName());
    }
}
