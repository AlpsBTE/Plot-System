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

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.tutorial.stage.AbstractStage;
import com.alpsbte.plotsystem.core.system.tutorial.stage.StageTimeline;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public abstract class AbstractTutorial implements Tutorial {
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
            onTutorialStop(player, true);
        } else {
            Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
                try {
                    // Switch to next stage
                    currentStage = getStage();

                    // Check if player has to switch world
                    if (currentStage.getInitWorldIndex() != currentWorldIndex) {
                        currentWorldIndex = currentStage.getInitWorldIndex();
                        onSwitchWorld(player, currentStage.getInitWorldIndex());
                    }

                    prepareNextStage();
                } catch (Exception ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "An error occurred while processing next stage!", ex);
                    player.sendMessage(Utils.ChatUtils.getErrorMessageFormat(LangUtil.getInstance().get(player, LangPaths.Message.Error.ERROR_OCCURRED)));
                }
            });
        }
    }

    protected void prepareNextStage() throws SQLException, IOException {
        // Ge the timeline of the current stage and increase the active stage index
        stageTimeline = currentStage.getTimeline();
        activeStageIndex++;

        // Send new stage unlocked message to player
        Bukkit.getScheduler().runTaskLater(PlotSystem.getPlugin(), () -> {
            ChatHandler.printInfo(player, ChatHandler.getStageUnlockedInfo(currentStage.getMessages().get(0), currentStage.getMessages().get(1)));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.7f);

            // Start tasks timeline
            stageTimeline.StartTimeline();
        }, 20 * 2);
    }

    @Override
    public void onTutorialStop(Player player, boolean isComplete) {
        activeTutorials.remove(this);
        if (stageTimeline != null) stageTimeline.onStopTimeLine(player);
        if (npc != null) npc.tutorialNPC.remove();

        if (isComplete) player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        Bukkit.getLogger().log(Level.INFO, "There are " + activeTutorials.size() + " active tutorials.");
        Bukkit.getLogger().log(Level.INFO, "There are " + TutorialEventListener.runningEventTasks.size() + " tutorial event tasks running.");

        // TODO: save stage to database
    }

    @Override
    public void onStageComplete(Player player) {
        if (!player.getUniqueId().toString().equals(this.player.getUniqueId().toString())) return;
        nextStage();
    }

    @Override
    public void onSwitchWorld(Player player, int tutorialWorldIndex) {
        if (!player.getUniqueId().toString().equals(this.player.getUniqueId().toString())) return;
        TutorialWorld world = worlds.get(tutorialWorldIndex);
        setTutorialWorld(Bukkit.getWorld(world.getWorldName()), world);
    }

    private void setTutorialWorld(World bukkitWorld, TutorialWorld world) {
        TutorialWorld.SpawnPoint playerSpawnPoint = world.getPlayerSpawnLocation();

        player.teleport(new Location(bukkitWorld,
                playerSpawnPoint.getX(),
                AlpsUtils.getHighestBlockYAt(bukkitWorld, (int) playerSpawnPoint.getX(), (int) playerSpawnPoint.getZ()) + 1,
                playerSpawnPoint.getZ(),
                playerSpawnPoint.getYaw(),
                playerSpawnPoint.getPitch()));

        TutorialWorld.SpawnPoint npcSpawnPoint = world.getNpcSpawnLocation();
        Location npcLocation = new Location(bukkitWorld,
                npcSpawnPoint.getX(),
                AlpsUtils.getHighestBlockYAt(bukkitWorld, (int) npcSpawnPoint.getX(), (int) npcSpawnPoint.getZ()) + 1,
                npcSpawnPoint.getZ(),
                npcSpawnPoint.getYaw(),
                npcSpawnPoint.getPitch());

        if (npc == null) {
            npc = new TutorialNPC(npcLocation);
        } else npc.tutorialNPC.teleport(npcLocation);
    }

    @Override
    public Player getPlayer() {
        return player;
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

    // TODO: MOVE SOMEWHERE ELSE
    public static class ChatHandler {
        public static void printInfo(Player player, String[] messages) {
            player.sendMessage("");
            for (String message : messages) player.sendMessage(message);
            player.sendMessage("");
        }

        public static String[] getStageUnlockedInfo(String title, String description) {
            LoreBuilder builder = new LoreBuilder()
                .addLines("§b§l" + "NEW STAGE UNLOCKED", "  §f§l◆ §6§l" + title, ""); // TODO: set player lang
            String[] descriptionLines = description.split("%newline%");
            Arrays.stream(descriptionLines).forEach(desc -> builder.addLine("    §7§l▪ §f" + desc));
            return builder.build().toArray(new String[0]);
        }
    }
}
