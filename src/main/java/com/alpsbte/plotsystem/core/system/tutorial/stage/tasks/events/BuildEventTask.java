package com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.events;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.tutorial.TutorialEventListener;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.AbstractTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Map;

public class BuildEventTask extends AbstractTask implements EventTask {
    private final BiTaskAction<Vector, Boolean> onPlacedBlockAction;
    private final Map<Vector, BlockData> blocksToBuild;
    private BukkitTask particleTask;

    public BuildEventTask(Player player, Component assignmentMessage, Map<Vector, BlockData> blocksToBuild, BiTaskAction<Vector, Boolean> onPlacedBlockAction) {
        super(player, assignmentMessage, blocksToBuild.size());
        this.blocksToBuild = blocksToBuild;
        this.onPlacedBlockAction = onPlacedBlockAction;
    }

    @Override
    public void performTask() {
        TutorialEventListener.runningEventTasks.put(player.getUniqueId().toString(), this);

        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Vector blockVector : blocksToBuild.keySet())
                    player.getWorld().spawnParticle(Particle.FLAME, blockVector.getBlockX() + 0.5, blockVector.getBlockY() + 0.5,
                            blockVector.getBlockZ() + 0.5, 1, 0, 0, 0, 0);
            }
        }.runTaskTimerAsynchronously(PlotSystem.getPlugin(), 0, 10);
    }

    @Override
    public void performEvent(Event event) {
        if (event instanceof BlockPlaceEvent buildEvent) {
            if (!buildEvent.canBuild()) return;
            Block placedBlock = buildEvent.getBlockPlaced();
            Vector placedBlockVector = new Vector(placedBlock.getX(), placedBlock.getY(), placedBlock.getZ());

            for (Map.Entry<Vector, BlockData> vectorBlock : blocksToBuild.entrySet()) {
                if (vectorBlock.getKey().equals(placedBlockVector)) {
                    if (placedBlock.getBlockData().matches(vectorBlock.getValue())) {
                        removeBlockToBuild(vectorBlock.getKey());
                        return;
                    }
                    onPlacedBlockAction.performAction(placedBlockVector, false);
                }
            }

            buildEvent.setCancelled(true);
        }
    }

    private void removeBlockToBuild(Vector blockVector) {
        blocksToBuild.remove(blockVector);

        updateAssignments();
        onPlacedBlockAction.performAction(blockVector, true);
        if (blocksToBuild.isEmpty()) setTaskDone();
    }

    @Override
    public void setTaskDone() {
        particleTask.cancel();
        super.setTaskDone();
    }
}
