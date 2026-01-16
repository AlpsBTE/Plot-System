package com.alpsbte.plotsystem.core.system.tutorial;

import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.events.EventTask;
import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

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