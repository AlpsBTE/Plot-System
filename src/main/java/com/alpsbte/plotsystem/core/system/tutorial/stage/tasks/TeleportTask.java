package com.alpsbte.plotsystem.core.system.tutorial.stage.tasks;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TeleportTask extends AbstractTask {
    private int tutorialWorldIndex;
    private Location location = null;

    public TeleportTask(Player player, int tutorialWorldIndex) {
        super(player);
        this.tutorialWorldIndex = tutorialWorldIndex;
    }

    public TeleportTask(Player player, Location location) {
        super(player);
        this.location = location;
    }

    @Override
    public void performTask() {
        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
            if (location == null) {
                AbstractTutorial tutorial = AbstractTutorial.getActiveTutorial(player.getUniqueId());
                if (tutorial == null) {
                    setTaskDone();
                    return;
                }

                tutorial.switchWorldAsync(player.getUniqueId(), tutorialWorldIndex).whenComplete((ignored, throwable) ->
                        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
                            if (throwable != null) {
                                tutorial.onException(throwable instanceof Exception exception ? exception : new Exception(throwable));
                                return;
                            }
                            setTaskDone();
                        }));
                return;
            } else {
                player.teleport(location);
            }
            setTaskDone();
        });
    }
}
