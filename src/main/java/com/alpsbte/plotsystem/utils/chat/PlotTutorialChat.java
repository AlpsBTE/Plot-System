package com.alpsbte.plotsystem.utils.chat;

import org.bukkit.entity.Player;

public class PlotTutorialChat extends Chat {

    public PlotTutorialChat(Player player) {
        super(player);
    }

    @Override
    protected void onResponse(String response) {
        if (response.trim().equalsIgnoreCase("cancel")) {
            // TODO: Cancel plot tutorial
        }
    }
}
