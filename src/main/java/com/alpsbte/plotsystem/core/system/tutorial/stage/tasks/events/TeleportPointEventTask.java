package com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.events;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.tutorial.TutorialEventListener;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.AbstractTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.List;

public class TeleportPointEventTask extends AbstractTask implements EventTask {
    private final BiTaskAction<Vector, Boolean> onTeleportAction;
    private final List<Vector> teleportPoints;
    private final int offsetRange;

    private BukkitTask particleTask;

    public TeleportPointEventTask(Player player, Component assignmentMessage, List<Vector> teleportPoints, int offsetRange, BiTaskAction<Vector, Boolean> onTeleportAction) {
        super(player, assignmentMessage, teleportPoints.size());
        this.teleportPoints = teleportPoints;
        this.offsetRange = offsetRange;
        this.onTeleportAction = onTeleportAction;
    }

    @Override
    public void performTask() {
        TutorialEventListener.runningEventTasks.put(player.getUniqueId().toString(), this);

        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Vector blockVector : teleportPoints)
                    player.getWorld().spawnParticle(Particle.FLAME, blockVector.getBlockX() + 0.5, blockVector.getBlockY() + 1.5,
                            blockVector.getBlockZ() + 0.5, 1, 0, 0, 0, 0);
            }
        }.runTaskTimerAsynchronously(PlotSystem.getPlugin(), 0, 10);
    }

    @Override
    public void performEvent(Event event) {
        if (event instanceof PlayerTeleportEvent teleportEvent) {
            if (teleportPoints.isEmpty()) return;

            Location teleportLoc = teleportEvent.getTo();
            int blockX = teleportLoc.getBlockX();
            int blockZ = teleportLoc.getBlockZ();

            for (Vector teleportPoint : teleportPoints) {
                if (blockX < (teleportPoint.getBlockX() - offsetRange) || blockX > (teleportPoint.getBlockX() + offsetRange)) continue;
                if (blockZ < (teleportPoint.getBlockZ() - offsetRange) || blockZ > (teleportPoint.getBlockZ() + offsetRange)) continue;
                removePoint(teleportPoint);
                return;
            }

            onTeleportAction.performAction(new Vector(teleportLoc.getBlockX(), teleportLoc.getBlockY(), teleportLoc.getBlockZ()), false);
        }
    }

    private void removePoint(Vector teleportPoint) {
        teleportPoints.remove(teleportPoint);

        updateAssignments();
        onTeleportAction.performAction(teleportPoint, true);
        if (teleportPoints.isEmpty()) setTaskDone();
    }

    @Override
    public void setTaskDone() {
        particleTask.cancel();
        super.setTaskDone();
    }

    @Override
    public String toString() {
        return "TeleportPointEventTask";
    }
}
