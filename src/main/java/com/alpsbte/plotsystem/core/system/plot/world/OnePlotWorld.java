package com.alpsbte.plotsystem.core.system.plot.world;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import com.alpsbte.plotsystem.core.system.plot.generator.AbstractPlotGenerator;
import com.alpsbte.plotsystem.core.system.plot.generator.DefaultPlotGenerator;
import com.alpsbte.plotsystem.core.system.plot.generator.TutorialPlotGenerator;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static net.kyori.adventure.text.Component.text;

public class OnePlotWorld extends PlotWorld {
    private final Builder plotOwner;

    public OnePlotWorld(@NotNull AbstractPlot plot) {
        super((plot instanceof TutorialPlot ? "T-" : "P-") + plot.getID(), plot);
        this.plotOwner = plot.getPlotOwner();
    }

    @Override
    public <T extends AbstractPlotGenerator> boolean generateWorld(@NotNull Class<T> generator) {
        if (isWorldGenerated()) return false;

        if (generator.isAssignableFrom(DefaultPlotGenerator.class)) {
            new DefaultPlotGenerator(getPlot(), plotOwner);
        } else if (generator.isAssignableFrom(TutorialPlotGenerator.class)) {
            new TutorialPlotGenerator(getPlot(), plotOwner);
        } else return false;
        return true;
    }

    @Override
    public boolean loadWorld() {
        if (getPlot() == null || isWorldGenerated()) return super.loadWorld();

        // Generate plot if it doesn't exist
        if (getPlot().getPlotType() == PlotType.TUTORIAL || ((Plot) getPlot()).getCompletedSchematic() == null)
            generateWorld(TutorialPlotGenerator.class);

        new DefaultPlotGenerator(getPlot(), plotOwner, getPlot().getPlotType()) {
            @Override
            protected boolean init() {
                return true;
            }

            @Override
            protected void onComplete(boolean failed, boolean unloadWorld) {
                getPlot().getPermissions().clearAllPerms();
                super.onComplete(true, false);
            }
        };

        if (!isWorldGenerated() || !isWorldLoaded()) {
            PlotSystem.getPlugin().getComponentLogger().warn(text("Could not regenerate world " + getWorldName() + " for plot " + getPlot().getID() + "!"));
            return false;
        }
        return true;
    }

    @Override
    public boolean unloadWorld(boolean movePlayers) {
        boolean isTutorialPlot;
        isTutorialPlot = getPlot().getPlotType() == PlotType.TUTORIAL;

        if (getPlot() != null) {
            if (isTutorialPlot) return deleteWorld();
            else return super.unloadWorld(movePlayers);
        }
        return false;
    }

    @Override
    public boolean teleportPlayer(@NotNull Player player) {
        if (!super.teleportPlayer(player)) return false;

        player.playSound(player.getLocation(), Utils.SoundUtils.TELEPORT_SOUND, 1, 1);
        player.setAllowFlight(true);
        player.setFlying(true);

        if (getPlot() == null) return true;
        if (getPlot().getPlotType() != PlotType.TUTORIAL) {
            player.sendMessage(Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(player, LangPaths.Message.Info.TELEPORTING_PLOT, String.valueOf(getPlot().getID()))));
            PlotUtils.ChatFormatting.sendLinkMessages(getPlot(), player);
        }
        Utils.updatePlayerInventorySlots(player);

        if (!getPlot().getPlotOwner().getUUID().equals(player.getUniqueId())) return true;
        getPlot().setLastActivity(false);

        return true;
    }

    @Override
    public int getPlotHeight() {
        return MIN_WORLD_HEIGHT;
    }

    @Override
    public int getPlotHeightCentered() throws IOException {
        return MIN_WORLD_HEIGHT + super.getPlotHeightCentered();
    }
}
