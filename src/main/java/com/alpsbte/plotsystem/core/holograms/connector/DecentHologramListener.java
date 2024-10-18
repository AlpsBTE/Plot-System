/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, ASEAN Build The Earth <bteasean@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.alpsbte.plotsystem.core.holograms.connector;

import eu.decentsoftware.holograms.event.HologramClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

/**
 * Listener class for DecentHologram.<br/>
 * This class listened for:<br/>
 * [1] Create displays everytime a player joined.<br/>
 * [2] Delete displays when a player quit.<br/>
 * [3] Re-create and delete displays when a player changes world.<br/>
 * [4] HologramClickEvent callback to any registered hologram.<br/>
 * @see PlayerJoinEvent
 * @see PlayerQuitEvent
 * @see PlayerChangedWorldEvent
 * @see HologramClickEvent
 */
public class DecentHologramListener implements Listener {
    public DecentHologramListener() {}

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        for (DecentHologramDisplay display : DecentHologramDisplay.activeDisplays) {
            if (display.getLocation() == null) return;
            if (Objects.requireNonNull(display.getLocation().getWorld()).getName().equals(event.getPlayer().getWorld().getName()))
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
            if (Objects.requireNonNull(display.getLocation().getWorld()).getName().equals(event.getFrom().getName())) display.remove(event.getPlayer().getUniqueId());
            else if (display.getLocation().getWorld().getName().equals(event.getPlayer().getWorld().getName())) display.create(event.getPlayer());
        }

    }

    @EventHandler
    public void onHologramClick(HologramClickEvent event) {
        for (DecentHologramDisplay display : DecentHologramDisplay.activeDisplays) {
            if (display.getLocation() == null
                | display.getClickListener() == null
                | display.getHologram(event.getPlayer().getUniqueId()) == null) continue;
            if (display.getHologram(event.getPlayer().getUniqueId()).equals(event.getHologram()))
                display.getClickListener().onClick(event);
        }
    }
}
