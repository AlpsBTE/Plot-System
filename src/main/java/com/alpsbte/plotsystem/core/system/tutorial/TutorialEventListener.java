/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.core.system.tutorial;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TutorialEventListener implements Listener {

    @EventHandler
    public void onPlayerBlockPlaceEvent(BlockPlaceEvent event) {
        AbstractTutorial.activeTutorials.forEach(t -> {
            if (!event.getPlayer().getUniqueId().equals(t.builder.getUUID())) return;
            t.activeStage.onPlayerBlockPlaceEvent(event);
        });
    }

    @EventHandler
    public void onPlayerBlockBreakEvent(BlockBreakEvent event) {
        AbstractTutorial.activeTutorials.forEach(t -> {
            if (!event.getPlayer().getUniqueId().equals(t.builder.getUUID())) return;
            t.activeStage.onPlayerBlockBreakEvent(event);
        });
    }

    @EventHandler
    public void onPlayerCommandInputEvent(PlayerCommandPreprocessEvent event) {
        AbstractTutorial.activeTutorials.forEach(t -> {
            if (!event.getPlayer().getUniqueId().equals(t.builder.getUUID())) return;
            t.activeStage.onPlayerCommandInputEvent(event.getMessage().replace("/", "").replace("//", ""));
        });
    }

    @EventHandler
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
        AbstractTutorial.activeTutorials.forEach(t -> {
            if (!event.getPlayer().getUniqueId().equals(t.builder.getUUID())) return;
            t.activeStage.onPlayerTeleportEvent(event.getTo());
        });
    }
}
