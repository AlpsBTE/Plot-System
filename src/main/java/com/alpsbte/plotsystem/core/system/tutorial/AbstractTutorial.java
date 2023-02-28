package com.alpsbte.plotsystem.core.system.tutorial;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public abstract class AbstractTutorial implements Listener {
    protected List<AbstractStage> stages;
    protected Builder builder;
    protected AbstractStage activeStage;

    protected AbstractTutorial(Builder builder) {
        this.builder = builder;
        PlotSystem.getPlugin().getServer().getPluginManager().registerEvents(this, PlotSystem.getPlugin());
    }

    @EventHandler
    public void onPlayerBlockPlaceEvent(BlockPlaceEvent event) {
        if (!event.getPlayer().getUniqueId().equals(builder.getUUID())) return;
        activeStage.onPlayerBlockPlaceEvent(event);
    }

    @EventHandler
    public void onPlayerBlockBreakEvent(BlockBreakEvent event) {
        if (!event.getPlayer().getUniqueId().equals(builder.getUUID())) return;
        activeStage.onPlayerBlockBreakEvent(event);
    }

    @EventHandler
    public void onPlayerCommandInputEvent(PlayerCommandPreprocessEvent event) {
        if (!event.getPlayer().getUniqueId().equals(builder.getUUID())) return;
        activeStage.onPlayerCommandInputEvent(event.getPlayer(), event.getMessage().replace("/", "").replace("//", ""));
    }

    @EventHandler
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
        if (!event.getPlayer().getUniqueId().equals(builder.getUUID())) return;
        activeStage.onPlayerTeleportEvent(event.getPlayer(), event.getTo());
    }
}
