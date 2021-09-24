package com.alpsbte.plotsystem.core.system.plot.generator;

import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotHandler;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Status;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class DefaultPlotGenerator extends AbstractPlotGenerator {

    public final static Map<UUID, LocalDateTime> playerPlotGenerationHistory = new HashMap<>();

    public DefaultPlotGenerator(int cityID, PlotDifficulty plotDifficulty, Builder builder) throws SQLException {
        this(PlotManager.getPlots(cityID, plotDifficulty, Status.unclaimed).get(new Random().nextInt(PlotManager.getPlots(cityID, plotDifficulty, Status.unclaimed).size())), builder);
    }

    public DefaultPlotGenerator(Plot plot, Builder builder) {
        super(plot, builder);
    }

    @Override
    protected CompletableFuture<Boolean> init() {
        try {
            if (getBuilder().getFreeSlot() != null) {
                if (DefaultPlotGenerator.playerPlotGenerationHistory.containsKey(getBuilder().getUUID())) {
                    if (DefaultPlotGenerator.playerPlotGenerationHistory.get(getBuilder().getUUID()).isBefore(LocalDateTime.now().minusSeconds(10))) {
                        DefaultPlotGenerator.playerPlotGenerationHistory.remove(getBuilder().getUUID());
                    } else {
                        getBuilder().getPlayer().sendMessage(Utils.getErrorMessageFormat("Please wait few seconds before creating a new plot!"));
                        getBuilder().getPlayer().playSound(getBuilder().getPlayer().getLocation(), Utils.ErrorSound, 1, 1);
                        return CompletableFuture.completedFuture(false);
                    }
                }

                getBuilder().getPlayer().sendMessage(Utils.getInfoMessageFormat("Creating new plot..."));
                getBuilder().getPlayer().playSound(getBuilder().getPlayer().getLocation(), Utils.CreatePlotSound, 1, 1);
                return CompletableFuture.completedFuture(true);
            } else {
                getBuilder().getPlayer().sendMessage(Utils.getErrorMessageFormat("All your slots are occupied! Please finish your current plots before creating a new one."));
                getBuilder().getPlayer().playSound(getBuilder().getPlayer().getLocation(), Utils.ErrorSound, 1, 1);
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.INFO, "A SQL error occurred!", ex);
        }
        return CompletableFuture.completedFuture(false);
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
