package com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.message;

import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorialHologram;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.AbstractTask;
import org.bukkit.entity.Player;

import java.util.List;

public class DeleteHologramTask extends AbstractTask {
    private List<AbstractTutorialHologram> hologramsToRemove;

    public DeleteHologramTask(Player player) {
        this(player, null);
    }

    public DeleteHologramTask(Player player, List<AbstractTutorialHologram> hologramsToRemove) {
        super(player);
        this.hologramsToRemove = hologramsToRemove;
    }

    @Override
    public void performTask() {
        if (hologramsToRemove == null) hologramsToRemove = AbstractTutorial.getActiveTutorial(player.getUniqueId()).getActiveHolograms();
        for (AbstractTutorialHologram hologram : hologramsToRemove) if (hologram.isVisible(player.getUniqueId())) hologram.delete();
        setTaskDone();
    }
}
