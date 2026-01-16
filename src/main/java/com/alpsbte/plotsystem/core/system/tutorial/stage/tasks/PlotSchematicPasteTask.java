package com.alpsbte.plotsystem.core.system.tutorial.stage.tasks;

import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.PlotTutorial;
import org.bukkit.entity.Player;

import java.io.IOException;

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
            try {
                tutorial.onPlotSchematicPaste(player.getUniqueId(), schematicId);
            } catch (IOException ex) {
                tutorial.onException(ex);
                return;
            }
        }
        setTaskDone();
    }
}
