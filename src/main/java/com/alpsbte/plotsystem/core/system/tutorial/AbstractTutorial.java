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

package com.alpsbte.plotsystem.core.system.tutorial;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.tutorial.stage.AbstractStage;
import com.alpsbte.plotsystem.core.system.tutorial.stage.StageTimeline;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import static net.md_5.bungee.api.ChatColor.GOLD;

public abstract class AbstractTutorial implements Tutorial {
    public static final String CHAT_HIGHLIGHT_COLOR = GOLD.toString();
    public static List<Tutorial> activeTutorials = new ArrayList<>();

    protected final Player player;
    private List<Class<? extends AbstractStage>> stages;
    private List<TutorialWorld> worlds;
    protected AbstractStage currentStage;


    private StageTimeline stageTimeline;
    protected int activeStageIndex = 0;

    protected TutorialNPC npc;
    protected int currentWorldIndex = -1;

    protected abstract AbstractStage getStage() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException;
    protected abstract List<TutorialWorld> setWorlds();
    protected abstract List<Class<? extends AbstractStage>> setStages();

    protected AbstractTutorial(Player player) {
        this.player = player;
    }

    protected void initTutorial() {
        activeTutorials.add(this);
        worlds = setWorlds();
        stages = setStages();
    }

    protected void setStage(int stageIndex) {
        activeStageIndex = stageIndex - 1;
        nextStage();
    }

    protected void nextStage() {
        if (activeStageIndex + 1 >= stages.size()) {
            onTutorialStop(player.getUniqueId());
        } else {
            Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
                try {
                    // Switch to next stage
                    currentStage = getStage();

                    // Check if player has to switch world
                    if (currentStage.getInitWorldIndex() != currentWorldIndex) {
                        currentWorldIndex = currentStage.getInitWorldIndex();
                        onSwitchWorld(player.getUniqueId(), currentStage.getInitWorldIndex());
                    }

                    prepareNextStage();
                } catch (Exception ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "An error occurred while processing next stage!", ex);
                    player.sendMessage(Utils.ChatUtils.getErrorMessageFormat(LangUtil.getInstance().get(player, LangPaths.Message.Error.ERROR_OCCURRED)));

                    // Send player back to hub after 3 seconds if an error occurred
                    Bukkit.getScheduler().runTaskLater(PlotSystem.getPlugin(), () -> {
                        onSwitchWorld(player.getUniqueId(), 0);
                        onTutorialStop(player.getUniqueId());
                    }, 20 * 3);
                }
            });
        }
    }

    protected boolean hasNextStage() {
        return activeStageIndex + 1 < stages.size();
    }

    protected void prepareNextStage() throws SQLException, IOException {
        // Ge the timeline of the current stage and increase the active stage index
        stageTimeline = currentStage.getTimeline();
        activeStageIndex++;

        // Send new stage unlocked message to player
        Bukkit.getScheduler().runTaskLater(PlotSystem.getPlugin(), () -> {
            sendStageUnlockedInfo(player, currentStage.getTitle());
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.7f);

            // Start tasks timeline
            stageTimeline.StartTimeline();
        }, 20 * 2);
    }

    @Override
    public void onTutorialStop(UUID playerUUID) {
        activeTutorials.remove(this);
        if (npc != null) npc.tutorialNPC.remove();
        if (player.isOnline() && !hasNextStage() && stageTimeline != null && stageTimeline.getCurrentTaskId() + 1 >= stageTimeline.getTasks().size())
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        if (stageTimeline != null) stageTimeline.onStopTimeLine(playerUUID);

        Bukkit.getLogger().log(Level.INFO, "There are " + activeTutorials.size() + " active tutorials.");
        Bukkit.getLogger().log(Level.INFO, "There are " + TutorialEventListener.runningEventTasks.size() + " tutorial event tasks running.");

        // TODO: save stage to database
    }

    @Override
    public void onStageComplete(UUID playerUUID) {
        if (!player.getUniqueId().toString().equals(playerUUID.toString())) return;
        nextStage();
    }

    @Override
    public void onSwitchWorld(UUID playerUUID, int tutorialWorldIndex) {
        if (!player.getUniqueId().toString().equals(playerUUID.toString())) return;
        if (npc != null && npc.tutorialNPC != null) {
            npc.tutorialNPC.remove(); // Temporary fix
            npc = null;
        }
        TutorialWorld world = worlds.get(tutorialWorldIndex);
        setTutorialWorld(world);
    }

    private void setTutorialWorld(TutorialWorld world) {
        player.teleport(world.getPlayerSpawnLocation());

        if (npc == null) {
            npc = new TutorialNPC(world.getNpcSpawnLocation());
        } else {
            npc.tutorialNPC.teleport(world.getNpcSpawnLocation());
        }
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public TutorialNPC getNPC() {
        return npc;
    }

    @Override
    public World getCurrentWorld() {
        return Bukkit.getWorld(worlds.get(currentWorldIndex).getWorldName());
    }

    public List<TutorialWorld> getWorlds() {
        return worlds;
    }

    public List<Class<? extends AbstractStage>> getStages() {
        return stages;
    }

    public static Tutorial getTutorialByPlayer(UUID playerUUID) {
        return AbstractTutorial.activeTutorials.stream().filter(tutorial ->
                tutorial.getPlayer().getUniqueId().toString().equals(playerUUID.toString())).findAny().orElse(null);
    }

    public static void sendStageUnlockedInfo(Player player, String title) {
        player.sendMessage(StringUtils.EMPTY);
        player.sendMessage("§b§l" + "NEW STAGE UNLOCKED");
        player.sendMessage("  §f§l◆ §6§l" + title);
        player.sendMessage(StringUtils.EMPTY);
    }
}
