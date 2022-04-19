package com.alpsbte.plotsystem.utils;

import com.alpsbte.plotsystem.PlotSystem;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

public class PacketListener {
    public PacketListener() {
        ProtocolManager protocolManager = PlotSystem.DependencyManager.getProtocolManager();
        if (protocolManager != null) {
            // Update inventory slots of player after changing client game settings
            protocolManager.addPacketListener(new PacketAdapter(PlotSystem.getPlugin(),
                    ListenerPriority.LOWEST, PacketType.Play.Client.SETTINGS) {
                @Override
                public void onPacketReceiving(PacketEvent event) {
                    Utils.updatePlayerInventorySlots(event.getPlayer());
                }
            });
        }
    }
}
