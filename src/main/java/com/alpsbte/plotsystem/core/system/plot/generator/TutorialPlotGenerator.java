package com.alpsbte.plotsystem.core.system.plot.generator;

import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.chat.PlotTutorialChat;
import org.jetbrains.annotations.NotNull;

public class TutorialPlotGenerator extends AbstractPlotGenerator {

    private final PlotTutorialChat plotTutorialChat;

    public TutorialPlotGenerator(@NotNull Plot plot, @NotNull Builder builder) {
        super(plot, builder);
        plotTutorialChat = new PlotTutorialChat(builder.getPlayer());

        // TODO: Implement tutorial plot generator
    }

    @Override
    protected boolean init() {
        return true;
    }
}
