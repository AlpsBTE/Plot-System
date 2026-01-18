package com.alpsbte.plotsystem.core.system.tutorial.stage.tasks;

import com.alpsbte.plotsystem.PlotSystem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DelayTask extends AbstractTask {
    private long delay;

    public DelayTask(Player player) {
        super(player);
    }

    public DelayTask(Player player, long delay) {
        this(player);
        this.delay = delay;
    }

    @Override
    public void performTask() {
        Bukkit.getScheduler().runTaskLater(PlotSystem.getPlugin(), this::setTaskDone, 20 * delay);
    }

    @Override
    public String toString() {
        return "DelayTask";
    }
}
