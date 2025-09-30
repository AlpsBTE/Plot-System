package com.alpsbte.plotsystem.core.system.tutorial.stage;

import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import org.bukkit.entity.Player;

public abstract class AbstractPlotStage extends AbstractStage {
    private final TutorialPlot plot;
    private final int initSchematicId;

    protected AbstractPlotStage(Player player, int initWorldIndex, TutorialPlot plot, int initSchematicId) {
        super(player, plot.getID(), initWorldIndex);
        this.plot = plot;
        this.initSchematicId = initSchematicId;
    }

    public TutorialPlot getPlot() {
        return plot;
    }

    public int getInitSchematicId() {
        return initSchematicId;
    }
}
