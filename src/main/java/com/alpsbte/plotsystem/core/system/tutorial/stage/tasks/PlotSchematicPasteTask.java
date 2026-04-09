package com.alpsbte.plotsystem.core.system.tutorial.stage.tasks;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.PlotTutorial;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class PlotSchematicPasteTask extends AbstractTask {
    private final int schematicId;

    public PlotSchematicPasteTask(Player player, int schematicId) {
        super(player);
        this.schematicId = schematicId;
    }

    @Override
    public void performTask() {
        PlotTutorial tutorial = (PlotTutorial) AbstractTutorial.getActiveTutorial(player.getUniqueId());
        if (tutorial != null) {
            CompletableFuture.runAsync(() -> {
                try {
                    tutorial.onPlotSchematicPaste(player.getUniqueId(), schematicId);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }).whenComplete((ignored, throwable) -> Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
                if (throwable != null) {
                    tutorial.onException(throwable instanceof Exception exception ? exception : new Exception(throwable));
                    return;
                }
                setTaskDone();
            }));
            return;
        }
        setTaskDone();
    }
}
