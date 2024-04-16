package com.alpsbte.plotsystem.core.holograms;

import com.alpsbte.plotsystem.core.holograms.connector.DecentHologramDisplay;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import eu.decentsoftware.holograms.api.utils.entity.HologramEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;

import java.util.logging.Level;

public class MessagesManager extends HologramManager {
    public static void init() {
        activeDisplays.add(new WelcomeMessage());
    }
}
