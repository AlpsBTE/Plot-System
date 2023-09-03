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

package com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.events;

import com.alpsbte.plotsystem.core.system.tutorial.TutorialEventListener;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.AbstractTask;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public class TeleportPointEventTask extends AbstractTask implements EventTask {
    private final TaskAction<BlockVector3> onTeleportAction;
    private final List<BlockVector3> teleportPoints;
    private final int offsetRange;

    public TeleportPointEventTask(Player player, String assignmentMessage, List<BlockVector3> teleportPoints, int offsetRange, TaskAction<BlockVector3> onTeleportAction) {
        super(player, assignmentMessage, teleportPoints.size());
        this.teleportPoints = teleportPoints;
        this.offsetRange = offsetRange;
        this.onTeleportAction = onTeleportAction;
    }

    @Override
    public void performTask() {
        TutorialEventListener.runningEventTasks.put(player.getUniqueId().toString(), this);
    }

    @Override
    public void performEvent(Event event) {
        if (event instanceof PlayerTeleportEvent) {
            PlayerTeleportEvent teleportEvent = (PlayerTeleportEvent) event;

            if (teleportPoints.isEmpty()) return;

            int blockX = teleportEvent.getTo().getBlockX();
            int blockZ = teleportEvent.getTo().getBlockZ();

            for (BlockVector3 teleportPoint : teleportPoints) {
                if (blockX < (teleportPoint.getBlockX() - offsetRange) || blockX > (teleportPoint.getBlockX() + offsetRange)) continue;
                if (blockZ < (teleportPoint.getBlockZ() - offsetRange) || blockZ > (teleportPoint.getBlockZ() + offsetRange)) continue;
                removePoint(teleportPoint);
                break;
            }
        }
    }

    private void removePoint(BlockVector3 teleportPoint) {
        teleportPoints.remove(teleportPoint);

        updateProgress();
        onTeleportAction.performAction(teleportPoint);
        if (teleportPoints.isEmpty()) setTaskDone();
    }

    @Override
    public String toString() {
        return "TeleportPointEventTask";
    }
}
