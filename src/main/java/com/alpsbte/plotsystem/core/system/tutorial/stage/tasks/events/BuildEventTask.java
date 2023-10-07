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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashMap;

public class BuildEventTask extends AbstractTask implements EventTask {
    private final TaskAction<BlockVector3> onPlacedBlockAction;
    private final HashMap<BlockVector3, Material> blocksToBuild;
    public BuildEventTask(Player player, String assignmentMessage, HashMap<BlockVector3, Material> blocksToBuild, TaskAction<BlockVector3> onPlacedBlockAction) {
        super(player, assignmentMessage, blocksToBuild.size());
        this.blocksToBuild = blocksToBuild;
        this.onPlacedBlockAction = onPlacedBlockAction;
    }

    @Override
    public void performTask() {
        TutorialEventListener.runningEventTasks.put(player.getUniqueId().toString(), this);
    }

    // TODO: Untested (wait for 1.20)
    @Override
    public void performEvent(Event event) {
        if (event instanceof BlockPlaceEvent) {
            BlockPlaceEvent buildEvent = (BlockPlaceEvent) event;
            if (!buildEvent.canBuild()) return;
            Block placedBlock = buildEvent.getBlockPlaced();
            BlockVector3 placedBlockVector = BlockVector3.at(placedBlock.getX(), placedBlock.getY(), placedBlock.getZ());

            for (BlockVector3 blockVector : blocksToBuild.keySet()) {
                if (blockVector.equals(placedBlockVector)) {
                    if (placedBlock.getType().equals(blocksToBuild.get(blockVector))) {
                        removeBlockToBuild(blockVector);
                        return;
                    }
                }
            }

            buildEvent.setCancelled(true);
        }
    }

    private void removeBlockToBuild(BlockVector3 blockVector) {
        blocksToBuild.remove(blockVector);

        updateAssignments();
        onPlacedBlockAction.performAction(blockVector);
        if (blocksToBuild.isEmpty()) setTaskDone();
    }
}
