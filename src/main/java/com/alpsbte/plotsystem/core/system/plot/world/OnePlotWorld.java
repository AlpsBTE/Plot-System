/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.alpsbte.plotsystem.core.system.plot.world;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import com.alpsbte.plotsystem.core.system.plot.generator.loader.AbstractPlotLoader;
import com.alpsbte.plotsystem.core.system.plot.generator.loader.DefaultPlotLoader;
import com.alpsbte.plotsystem.core.system.plot.generator.loader.TutorialPlotLoader;
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
    private final AbstractPlot plot;

    public OnePlotWorld(@NotNull AbstractPlot plot) {
        super((plot instanceof TutorialPlot ? "T-" : "P-") + plot.getID(), plot);
        this.plot = plot;
        this.plotOwner = plot.getPlotOwner();
    }

    @Override
    public <T extends AbstractPlotLoader> boolean generateWorld(@NotNull Class<T> generator) {
        if (isWorldGenerated()) return false;

        if (generator.isAssignableFrom(DefaultPlotLoader.class)) {
            new DefaultPlotLoader(plot, plotOwner);
        } else if (generator.isAssignableFrom(TutorialPlotLoader.class)) {
            new TutorialPlotLoader(plot, plotOwner);
        } else return false;
        return true;
    }

    @Override
    public boolean loadWorld() {
        if (plot == null || isWorldGenerated()) return super.loadWorld();

        // Generate plot if it doesn't exist
        if (plot.getPlotType() == PlotType.TUTORIAL || ((Plot) plot).getCompletedSchematic() == null)
            generateWorld(TutorialPlotLoader.class);

        new DefaultPlotLoader(plot, plotOwner, plot.getPlotType(), this);

        if (!isWorldGenerated() || !isWorldLoaded()) {
            PlotSystem.getPlugin().getComponentLogger().warn(text("Could not regenerate world " + getWorldName() + " for plot " + plot.getID() + "!"));
            return false;
        }
        return true;
    }

    @Override
    public boolean unloadWorld(boolean movePlayers) {
        boolean isTutorialPlot;
        isTutorialPlot = plot.getPlotType() == PlotType.TUTORIAL;

        if (isTutorialPlot) return deleteWorld();
        else return super.unloadWorld(movePlayers);
    }

    @Override
    public boolean teleportPlayer(@NotNull Player player) {
        if (!super.teleportPlayer(player)) return false;

        player.playSound(player.getLocation(), Utils.SoundUtils.TELEPORT_SOUND, 1, 1);
        player.setAllowFlight(true);
        player.setFlying(true);

        if (plot == null) return true;
        if (plot.getPlotType() != PlotType.TUTORIAL) {
            player.sendMessage(Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(player, LangPaths.Message.Info.TELEPORTING_PLOT, String.valueOf(plot.getID()))));
            PlotUtils.ChatFormatting.sendLinkMessages(plot, player);
        }
        Utils.updatePlayerInventorySlots(player);

        if (!plot.getPlotOwner().getUUID().equals(player.getUniqueId())) return true;
        plot.setLastActivity(false);

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

    @Override
    public boolean onAbandon() {
        if (!isWorldGenerated()) return super.onAbandon();
        if (isWorldLoaded()) {
            for (Player player : getBukkitWorld().getPlayers()) player.teleport(Utils.getSpawnLocation());
        }
        if (!deleteWorld()) {
            PlotSystem.getPlugin().getComponentLogger().warn(text("Could not delete plot world " + getWorldName() + "!"));
            return false;
        }
        return super.onAbandon();
    }
}
