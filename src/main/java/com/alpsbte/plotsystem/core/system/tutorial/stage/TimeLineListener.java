package com.alpsbte.plotsystem.core.system.tutorial.stage;

import com.alpsbte.plotsystem.core.system.tutorial.tasks.AbstractTask;
import org.bukkit.entity.Player;

public interface TimeLineListener {
    void onTaskDone(Player player, AbstractTask task);
    void onStopTimeLine(Player player);
}
