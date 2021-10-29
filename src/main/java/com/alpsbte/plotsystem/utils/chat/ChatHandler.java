package com.alpsbte.plotsystem.utils.chat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class ChatHandler implements Listener {

    public static Map<Player, Chat> playerChats = new HashMap<>();

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // TODO: Handle player chat event
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        playerChats.remove(event.getPlayer());
    }
}
