package com.alpsbte.plotsystem.core.holograms.connector;

import eu.decentsoftware.holograms.event.HologramClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class DecentHologramListener implements Listener {
    public DecentHologramListener() {
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        // Create player's hologram each time they join
        for (DecentHologramDisplay display : DecentHologramDisplay.activeDisplays) {
            if (display.getLocation() == null) return;
            if (display.getLocation().getWorld().getName().equals(event.getPlayer().getWorld().getName()))
                display.create(event.getPlayer());
        }

    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        for (DecentHologramDisplay display : DecentHologramDisplay.activeDisplays) {
            if (display.getHolograms().containsKey(event.getPlayer().getUniqueId())) display.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent event) {
        for (DecentHologramDisplay display : DecentHologramDisplay.activeDisplays) {
            if (display.getLocation() == null) return;
            if (display.getLocation().getWorld().getName().equals(event.getFrom().getName())) display.remove(event.getPlayer().getUniqueId());
            else if (display.getLocation().getWorld().getName().equals(event.getPlayer().getWorld().getName())) display.create(event.getPlayer());
        }

    }

    @EventHandler
    public void onHologramClick(HologramClickEvent event) {
        for (DecentHologramDisplay display : DecentHologramDisplay.activeDisplays) {
            if (display.getLocation() == null | display.getClickListener() == null) continue;
            if (display.getHologram(event.getPlayer().getUniqueId()).equals(event.getHologram()))
                    display.getClickListener().onClick(event);
        }
    }
}
