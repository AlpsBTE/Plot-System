/*
 * The MIT License (MIT)
 *
 *  Copyright © 2025, Alps BTE <bte.atchli@gmail.com>
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

import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.events.EventTask;
import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;

import java.util.HashMap;
import java.util.Map;

public class TutorialEventListener implements Listener {
    public static final Map<String, EventTask> runningEventTasks = new HashMap<>();

    @EventHandler
    private void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        if (!runningEventTasks.containsKey(event.getPlayer().getUniqueId().toString())) return;
        runningEventTasks.get(event.getPlayer().getUniqueId().toString()).performEvent(event);
    }

    @EventHandler
    private void onPlayerTeleportEvent(PlayerTeleportEvent event) {
        if (!runningEventTasks.containsKey(event.getPlayer().getUniqueId().toString())) return;
        runningEventTasks.get(event.getPlayer().getUniqueId().toString()).performEvent(event);
    }

    @EventHandler
    private void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (!runningEventTasks.containsKey(event.getPlayer().getUniqueId().toString())) return;
        runningEventTasks.get(event.getPlayer().getUniqueId().toString()).performEvent(event);
    }

    @EventHandler
    private void onNpcInteractEvent(PlayerUseUnknownEntityEvent event) {
        if (!runningEventTasks.containsKey(event.getPlayer().getUniqueId().toString())) return;
        runningEventTasks.get(event.getPlayer().getUniqueId().toString()).performEvent(event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerChatEvent(AsyncChatEvent event) {
        if (!runningEventTasks.containsKey(event.getPlayer().getUniqueId().toString())) return;
        runningEventTasks.get(event.getPlayer().getUniqueId().toString()).performEvent(event);
    }

    @EventHandler
    private void onPlayerPlaceBlockEvent(BlockPlaceEvent event) {
        if (!runningEventTasks.containsKey(event.getPlayer().getUniqueId().toString())) return;
        runningEventTasks.get(event.getPlayer().getUniqueId().toString()).performEvent(event);
    }

    @EventHandler
    private void onPlayerChangedWorldEvent(PlayerChangedWorldEvent event) {
        Tutorial tutorial = AbstractTutorial.getActiveTutorial(event.getPlayer().getUniqueId());
        if (tutorial != null && (tutorial.getCurrentWorld() == null || !tutorial.getCurrentWorld().getName().equals(event.getPlayer().getWorld().getName())))
            tutorial.onTutorialStop(event.getPlayer().getUniqueId());
    }
}