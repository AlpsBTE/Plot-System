package com.alpsbte.plotsystem.core.system.plot.generator;

import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotHandler;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Status;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.Random;

import static com.alpsbte.plotsystem.core.system.plot.PlotManager.getPlots;

public class DefaultPlotGenerator extends AbstractPlotGenerator {

    public DefaultPlotGenerator(int cityID, PlotDifficulty plotDifficulty, Builder builder) throws SQLException {
        this(getPlots(cityID, plotDifficulty, Status.unclaimed).get(new Random().nextInt(getPlots(cityID, plotDifficulty, Status.unclaimed).size())), builder);
    }

    public DefaultPlotGenerator(Plot plot, Builder builder) {
        super(plot, builder);
    }

    @Override
    protected void onComplete(boolean failed) throws SQLException {
        super.onComplete(failed);

        if (!failed) {
            PlotHandler.teleportPlayer(getPlot(), getBuilder().getPlayer());
            Bukkit.broadcastMessage(Utils.getInfoMessageFormat("Created new plot§a for §6" + getPlot().getPlotOwner().getName() + "§a!"));
        }
    }
}
