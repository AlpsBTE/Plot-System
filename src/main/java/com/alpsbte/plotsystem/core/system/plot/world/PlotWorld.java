package com.alpsbte.plotsystem.core.system.plot.world;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.generator.AbstractPlotGenerator;
import com.alpsbte.plotsystem.core.system.plot.generator.DefaultPlotGenerator;
import com.alpsbte.plotsystem.core.system.plot.generator.RawPlotGenerator;
import com.alpsbte.plotsystem.utils.Utils;
import com.sk89q.worldguard.bukkit.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class PlotWorld implements IPlotWorld {

    private final Plot plot;

    public PlotWorld(Plot plot) {
        this.plot = plot;
    }

    @Override
    public <T extends AbstractPlotGenerator> boolean generate(Builder plotOwner, Class<T> generator) {
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
            if (movePlayers && !getBukkit().getPlayers().isEmpty()) {
                for (Player player : getBukkit().getPlayers()) {
                    player.teleport(Utils.getSpawnLocation());
                }
            } else if (!movePlayers && getBukkit().getPlayers().isEmpty()) return false;

            Bukkit.getScheduler().scheduleSyncDelayedTask(PlotSystem.getPlugin(), (() ->
                    Bukkit.unloadWorld(getBukkit(), true)), 60L);
            return !isLoaded();
        }
        return false;
    }

    @Override
    public World getBukkit() {
        return Bukkit.getWorld(getName());
    }

    @Override
    public String getName() {
        return "P-" + plot.getID();
    }

    @Override
    public RegionContainer getProtectedRegion() {
        return null;
    }

    @Override
    public boolean isLoaded() {
        return getBukkit() == null;
    }

    @Override
    public boolean isGenerated() {
        return PlotSystem.DependencyManager.getMultiverseCore().getMVWorldManager().isMVWorld(getName());
    }
}
