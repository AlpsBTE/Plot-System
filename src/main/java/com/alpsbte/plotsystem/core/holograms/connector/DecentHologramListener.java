package com.alpsbte.plotsystem.core.holograms.connector;

import eu.decentsoftware.holograms.event.HologramClickEvent;
import org.bukkit.Bukkit;
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
            if (display.getLocation() == null) return;
            if (display.getHologram(event.getPlayer().getUniqueId()).equals(event.getHologram()))
                if(display.getClickListender() != null)
                    display.getClickListender().onClick(event);
        }

        Bukkit.getLogger().info("Hologram clicked on an entity: "
                + event.getEntityId()
                + " with hologram lines: "
                + event.getHologram().getPage(event.getPlayer()).getLines());
    }
}
