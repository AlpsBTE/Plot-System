/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.message;

import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorialHologram;
import com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialUtils;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.AbstractTask;
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
