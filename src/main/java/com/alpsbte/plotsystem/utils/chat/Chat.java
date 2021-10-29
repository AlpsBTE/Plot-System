package com.alpsbte.plotsystem.utils.chat;

import org.bukkit.entity.Player;

public abstract class Chat {
    private final Player player;
    public boolean isPlayerInteractionEnabled = true;

    public Chat(Player player) {
        this.player = player;
    }

    public Chat(Player player, boolean playerInteractionEnabled) {
        this.player = player;
        isPlayerInteractionEnabled = playerInteractionEnabled;
    }

    protected abstract void onResponse(String response);

    public Player getPlayer() {
        return player;
    }

    public boolean isPlayerInteractionEnabled() {
        return isPlayerInteractionEnabled;
    }
}
