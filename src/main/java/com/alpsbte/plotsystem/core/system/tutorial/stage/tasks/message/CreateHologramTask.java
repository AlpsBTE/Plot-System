package com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.message;

import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorialHologram;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.AbstractTask;
import com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialUtils;
import eu.decentsoftware.holograms.event.HologramClickEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CreateHologramTask extends AbstractTask {
    private final List<AbstractTutorialHologram> hologramsToCreate;
    private final boolean isMarkAsRead;

    public CreateHologramTask(Player player, List<AbstractTutorialHologram> hologramsToCreate) {
        super(player);
        this.hologramsToCreate = hologramsToCreate;
        this.isMarkAsRead = false;
    }

    public CreateHologramTask(Player player, Component assignmentMessage, List<AbstractTutorialHologram> hologramsToCreate, boolean isMarkAsRead) {
        super(player, assignmentMessage, hologramsToCreate.size());
        this.hologramsToCreate = hologramsToCreate;
        this.isMarkAsRead = isMarkAsRead;
    }

    @Override
    public void performTask() {
        for (AbstractTutorialHologram hologram : hologramsToCreate) {
            if (isMarkAsRead) hologram.setMarkAsReadClickAction((@NotNull HologramClickEvent clickEvent) -> onMarkAsReadClick(hologram.getId()));
            hologram.create(player);
        }
        if (!isMarkAsRead) setTaskDone();
    }

    private void onMarkAsReadClick(String holoId) {
        AbstractTutorialHologram hologram = hologramsToCreate.stream().filter(holo -> holo.getId().equals(holoId)).findFirst().orElse(null);
        if (hologram == null) return;
        player.playSound(player.getLocation(), TutorialUtils.Sound.ASSIGNMENT_COMPLETED, 1f, 1f);
        hologramsToCreate.remove(hologram);
        updateAssignments();

        if (hologramsToCreate.isEmpty()) setTaskDone();
    }
}
