package com.alpsbte.plotsystem.core.system.tutorial;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public abstract class AbstractStage {
    abstract void performStage();
    public void onPlayerBlockPlaceEvent(BlockPlaceEvent event) {}
    public void onPlayerBlockBreakEvent(BlockBreakEvent event) {}
    public void onPlayerCommandInputEvent(Player player, String command) {}
    public void onPlayerTeleportEvent(Player player, Location location) {}
}
