/*
 * The MIT License (MIT)
 *
 *  Copyright © 2025, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.core.system.tutorial;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.database.providers.TutorialPlotProvider;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import com.alpsbte.plotsystem.core.system.plot.generator.TutorialPlotGenerator;
import com.alpsbte.plotsystem.core.system.tutorial.stage.AbstractPlotStage;
import com.alpsbte.plotsystem.core.system.tutorial.stage.AbstractStage;
import com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialNPC;
import com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialUtils;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.sk89q.worldedit.WorldEditException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialUtils.Sound;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public abstract class AbstractPlotTutorial extends AbstractTutorial implements PlotTutorial {
    protected TutorialPlot tutorialPlot;
    private TutorialPlotGenerator plotGenerator;
    private boolean isPasteSchematic;

    protected AbstractPlotTutorial(Player player, int tutorialId, int stageId) {
        super(player, tutorialId, stageId);

        CompletableFuture.runAsync(() -> {
            String playerUUID = player.getUniqueId().toString();
            Optional<TutorialPlot> plot = DataProvider.TUTORIAL_PLOT.getByTutorialId(tutorialId, playerUUID);
            if (plot.isEmpty() && DataProvider.TUTORIAL_PLOT.add(tutorialId, playerUUID))
                tutorialPlot = DataProvider.TUTORIAL_PLOT.getByTutorialId(tutorialId, playerUUID).orElse(null);

            // Check if tutorial plot is null
            if (tutorialPlot == null) {
                PlotSystem.getPlugin().getComponentLogger().error(text("Could not load tutorial. Plot is null!"));
                return;
            }

            Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
                // Initialize tutorial worlds and stages
                initTutorial();

                // Start the tutorial
                nextStage();
            });
        }).exceptionally(ex -> {
            PlotSystem.getPlugin().getComponentLogger().error(text("Could not load tutorial."), ex);
            return null;
        });
    }

    @Override
    protected TutorialNPC initNpc() {
        return new TutorialNPC(
                "ps-tutorial-" + tutorialPlot.getID(),
                ChatColor.GOLD + ChatColor.BOLD.toString() + PlotSystem.getPlugin().getConfig().getString(ConfigPaths.TUTORIAL_NPC_NAME),
                ChatColor.GRAY + "(" + LangUtil.getInstance().get(getPlayer(), LangPaths.Note.Action.RIGHT_CLICK) + ")",
                PlotSystem.getPlugin().getConfig().getString(ConfigPaths.TUTORIAL_NPC_TEXTURE),
                PlotSystem.getPlugin().getConfig().getString(ConfigPaths.TUTORIAL_NPC_SIGNATURE));
    }

    @Override
    public void setStage(int stageId) {
        isPasteSchematic = true;
        super.setStage(stageId);
    }

    @Override
    public void onPlotSchematicPaste(UUID playerUUID, int schematicId) {
        if (!getPlayerUUID().toString().equals(playerUUID.toString())) return;
        try {
            if (plotGenerator != null && tutorialPlot.getWorld().isWorldGenerated() && tutorialPlot.getWorld().isWorldLoaded()) {
                plotGenerator.generateOutlines(schematicId);
            }
        } catch (IOException | WorldEditException ex) {
            onException(ex);
        }
    }

    @Override
    public void onPlotPermissionChange(UUID playerUUID, boolean isBuildingAllowed, boolean isWorldEditAllowed) {
        if (!getPlayerUUID().toString().equals(playerUUID.toString())) return;
        if (plotGenerator != null) {
            plotGenerator.setBuildingEnabled(isBuildingAllowed);
            plotGenerator.setWorldEditEnabled(isWorldEditAllowed);
        }
    }

    @Override
    protected AbstractStage getStage() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return getStages().get(getCurrentStage()).getDeclaredConstructor(Player.class, TutorialPlot.class).newInstance(getPlayer(), tutorialPlot);
    }

    @Override
    protected void prepareStage(PrepareStageAction action) {
        Bukkit.getScheduler().runTaskLater(PlotSystem.getPlugin(), () -> {
            // paste initial schematic outlines of stage
            if (isPasteSchematic) onPlotSchematicPaste(getPlayerUUID(), ((AbstractPlotStage) currentStage).getInitSchematicId());
            isPasteSchematic = false;

            // Send a new stage unlocked message to the player
            sendStageUnlockedMessage(getPlayer(), currentStage.getTitle());
            getPlayer().playSound(getPlayer().getLocation(), Sound.STAGE_COMPLETED, 1f, 0.7f);

            // Mark stage preparation as done
            action.setDone();
        }, 20);
    }

    @Override
    public void saveTutorial(int stageId) {
        Bukkit.getScheduler().runTaskAsynchronously(PlotSystem.getPlugin(), () -> {
            if (stageId >= stages.size()) {
                if (!tutorialPlot.isComplete()) tutorialPlot.setComplete();
            } else if (stageId > tutorialPlot.getStageID()) tutorialPlot.setStageID(stageId);
        });
    }

    @Override
    public void onSwitchWorld(UUID playerUUID, int tutorialWorldIndex) {
        if (!getPlayerUUID().toString().equals(playerUUID.toString())) return;
        if (tutorialWorldIndex == 1 && (plotGenerator == null || !plotGenerator.getPlot().getWorld().isWorldGenerated())) {
            plotGenerator = new TutorialPlotGenerator(tutorialPlot, Builder.byUUID(playerUUID));
            onPlotSchematicPaste(playerUUID, ((AbstractPlotStage) currentStage).getInitSchematicId());
        }
        super.onSwitchWorld(playerUUID, tutorialWorldIndex);
    }

    @Override
    public void onTutorialComplete(UUID playerUUID) {
        if (!getPlayerUUID().toString().equals(playerUUID.toString())) return;

        saveTutorial(getCurrentStage() + 1);
        getPlayer().playSound(getPlayer().getLocation(), Sound.TUTORIAL_COMPLETED, 1f, 1f);
        sendTutorialCompletedMessage(getPlayer(), getName());
    }

    @Override
    public void onTutorialStop(UUID playerUUID) {
        if (!getPlayerUUID().toString().equals(playerUUID.toString())) return;
        super.onTutorialStop(playerUUID);
        if (tutorialPlot != null) tutorialPlot.getWorld().deleteWorld();
        int index = TutorialPlotProvider.tutorialPlots.get(tutorialPlot);
        TutorialPlotProvider.tutorialPlots.remove(tutorialPlot);
        TutorialPlotProvider.freeTutorialPlotIds.add(index);
    }

    @Override
    public void onException(Exception ex) {
        if (getPlayer().isOnline()) getPlayer().sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(getPlayer(), LangPaths.Message.Error.ERROR_OCCURRED)));
        super.onException(ex);
    }

    /**
     * Sends a message to the player when a new stage is unlocked.
     *
     * @param player The player to send the message to.
     * @param title  The title of the stage.
     */
    protected static void sendStageUnlockedMessage(Player player, String title) {
        player.sendMessage(text());
        player.sendMessage(text(LangUtil.getInstance().get(player, LangPaths.Tutorials.NEW_STAGE_UNLOCKED)).color(AQUA).decorate(BOLD));
        player.sendMessage(text("  ◆ ", WHITE, BOLD).append(text(title).color(GOLD).decorate(BOLD)));
        player.sendMessage(text());
    }

    /**
     * Sends a message to the player when a tutorial is completed.
     *
     * @param player       The player to send the message to.
     * @param tutorialName The name of the tutorial.
     * @see TutorialCategory
     */
    protected static void sendTutorialCompletedMessage(Player player, String tutorialName) {
        player.sendMessage(text());
        player.sendMessage(text(LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIAL_COMPLETED).toUpperCase()).color(AQUA).decorate(BOLD));
        player.sendMessage(text("  ◆ ").color(WHITE).decorate(BOLD).append(text(tutorialName).color(GOLD)));
        player.sendMessage(text());
    }

    /**
     * Sends a message to the player if they have not completed a tutorial, if required.
     *
     * @param player     the player to send the message to
     * @param tutorialId the id of the tutorial
     */
    public static void sendTutorialRequiredMessage(Player player, int tutorialId) {
        Component clickComponent = text("[", DARK_GRAY, BOLD)
                .append(text(LangUtil.getInstance().get(player, LangPaths.Note.Action.CONTINUE_TUTORIAL), GREEN))
                .append(text("]", DARK_GRAY))
                .clickEvent(ClickEvent.runCommand("/tutorial " + tutorialId))
                .hoverEvent(HoverEvent.showText(text(LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_PROCEED), GRAY)));

        player.sendMessage(text());
        player.sendMessage(TutorialUtils.CHAT_TASK_PREFIX_COMPONENT
                .append(AlpsUtils.deserialize(LangUtil.getInstance().get(player, LangPaths.Message.Info.BEGINNER_TUTORIAL_REQUIRED)).color(GRAY)));
        player.sendMessage(clickComponent);
        player.sendMessage(text());
    }
}