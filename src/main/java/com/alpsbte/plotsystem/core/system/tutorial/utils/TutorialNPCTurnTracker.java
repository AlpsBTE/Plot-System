package com.alpsbte.plotsystem.core.system.tutorial.utils;

import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import org.bukkit.Location;

public class TutorialNPCTurnTracker implements Runnable {
    public static final int turnToPlayerDistance = FancyNpcsPlugin.get().getFancyNpcConfig().getTurnToPlayerDistance();

    @Override
    public void run() {
        for (AbstractTutorial tutorial : AbstractTutorial.getActiveTutorials()) {
            if (tutorial.getNPC() == null) continue;
            Location playerLoc = tutorial.getPlayer().getLocation();
            Location npcLoc = tutorial.getNPC().getNpc().getData().getLocation();
            if (npcLoc == null || !npcLoc.getWorld().getName()
                    .equalsIgnoreCase(playerLoc.getWorld().getName())) continue;

            double distance = playerLoc.distance(npcLoc);
            if (Double.isNaN(distance)) continue;

            if (distance < turnToPlayerDistance) {
                Location newLoc = playerLoc.clone();
                newLoc.setDirection(newLoc.subtract(npcLoc).toVector());
                tutorial.getNPC().getNpc().lookAt(tutorial.getPlayer(), newLoc);
            }
        }
    }
}
