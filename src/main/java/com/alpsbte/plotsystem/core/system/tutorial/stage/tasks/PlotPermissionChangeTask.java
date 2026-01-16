package com.alpsbte.plotsystem.core.system.tutorial.stage.tasks;

import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.PlotTutorial;
import org.bukkit.entity.Player;

public class PlotPermissionChangeTask extends AbstractTask {
    private final boolean isBuildingAllowed;
    private final boolean isWorldEditAllowed;

    public PlotPermissionChangeTask(Player player, boolean allowBuilding, boolean allowWorldEdit) {
        super(player);
        isBuildingAllowed = allowBuilding;
        isWorldEditAllowed = allowWorldEdit;
    }

    @Override
    public void performTask() {
        PlotTutorial tutorial = (PlotTutorial) AbstractTutorial.getActiveTutorial(player.getUniqueId());
        if (tutorial != null) tutorial.onPlotPermissionChange(player.getUniqueId(), isBuildingAllowed, isWorldEditAllowed);
        setTaskDone();
    }
}
