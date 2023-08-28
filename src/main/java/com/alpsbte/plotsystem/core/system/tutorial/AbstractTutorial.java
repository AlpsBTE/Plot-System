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
import org.bukkit.*;
import org.bukkit.entity.Player;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Abstract class for all tutorials. Inherit this class to create a new tutorial.
 * There is no way to save the progress of a tutorial. This needs to be implemented manually at the moment.
 * @see AbstractTutorial#saveStage()
 * @see AbstractTutorial#onTutorialComplete(UUID)
 */
public abstract class AbstractTutorial implements Tutorial {

    /**
     * This action method is used to set the stage preparation as done when switching to the next stage.
     * @see AbstractTutorial#prepareStage(PrepareStageAction)
     */
    @FunctionalInterface
    protected interface PrepareStageAction {
        void setDone();
    }

    /**
     * A list of all registered tutorials.
     * @see AbstractTutorial#setTutorials(List)
     */
    private static List<Class<? extends AbstractTutorial>> tutorials;

    /**
     * A list of all active tutorials.
     * @see AbstractTutorial#getActiveTutorial(UUID)
     */
    private static final List<Tutorial> activeTutorials = new ArrayList<>();


    private final Player player;
    private final int tutorialId;


    protected AbstractStage currentStage;
    private List<Class<? extends AbstractStage>> stages;
    private int currentStageIndex;
    private List<TutorialWorld> worlds;
    protected int currentWorldIndex = -1;


    private StageTimeline stageTimeline;
    private TutorialNPC npc;

    /**
     * Creates a new instance of the tutorial current stage.
     * @return current stage
     * @throws NoSuchMethodException if the constructor of the current stage could not be found
     * @throws InvocationTargetException if the constructor of the current stage could not be invoked
     * @throws InstantiationException if the current stage could not be instantiated
     * @throws IllegalAccessException if the current stage could not be accessed
     */
    protected abstract AbstractStage getStage() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException;

    /**
     * Sets all worlds which can be used in the tutorial.
     * @return list of all worlds
     */
    protected abstract List<TutorialWorld> setWorlds();

    /**
     * Sets all stages of the tutorial as classes.
     * @return list of all stages
     */
    protected abstract List<Class<? extends AbstractStage>> setStages();

    /**
     * This method is called before the next stage timeline is started.
     * Use this method to do some preparations for the next stage.
     * @param action action to set the stage preparation as done
     */
    protected abstract void prepareStage(PrepareStageAction action);

    protected AbstractTutorial(Player player, int tutorialId, int stageId) {
        this.player = player;
        this.tutorialId = tutorialId;

        if (stageId < 0) stageId = 0;
        currentStageIndex = stageId - 1;
    }

    /**
     * This method needs to be called before the tutorial can be started.
     */
    protected void initTutorial() {
        activeTutorials.add(this);
        worlds = setWorlds();
        stages = setStages();
    }

    @Override
    public int getId() {
        return tutorialId;
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

    @Override
    public int getCurrentStage() {
        return currentStageIndex;
    }

    @Override
    public void setStage(int stageId) {
        if (stageId < 0 || stageId > stages.size()) return;
        if (stageTimeline != null) stageTimeline.onStopTimeLine(player.getUniqueId());
        currentStageIndex = stageId - 1;
        nextStage();
    }

    /**
     * This method switches to the next stage. If no stage is left, the tutorial will be completed.
     */
    protected void nextStage() {
        // Increase stage index
        currentStageIndex++;

        if (currentStageIndex >= stages.size()) {
            onTutorialComplete(player.getUniqueId());
            onTutorialStop(player.getUniqueId());
        } else {
            Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
                try {
                    // Switch to next stage
                    currentStage = getStage();

                    // Check if player has to switch world
                    onSwitchWorld(player.getUniqueId(), currentStage.getInitWorldIndex());

                    // Ge the timeline of the current stage
                    stageTimeline = currentStage.getTimeline();

                    prepareStage(() -> {
                        // Start tasks timeline
                        stageTimeline.StartTimeline();
                    });
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

    @Override
    public void onStageComplete(UUID playerUUID) {
        if (!player.getUniqueId().toString().equals(playerUUID.toString())) return;
        saveStage();
        Bukkit.getLogger().log(Level.INFO, "Stage Complete: " + getCurrentStage());
        nextStage();
    }

    @Override
    public void onSwitchWorld(UUID playerUUID, int tutorialWorldIndex) {
        if (!player.getUniqueId().toString().equals(playerUUID.toString())) return;
        if (currentWorldIndex == tutorialWorldIndex) return;
        currentWorldIndex = tutorialWorldIndex;

        if (npc != null && npc.tutorialNPC != null) {
            npc.tutorialNPC.remove(); // Temporary fix
            npc = null;
        }
        TutorialWorld world = worlds.get(tutorialWorldIndex);

        player.teleport(world.getPlayerSpawnLocation());
        if (npc == null) npc = new TutorialNPC(world.getNpcSpawnLocation());
        else npc.tutorialNPC.teleport(world.getNpcSpawnLocation());
    }

    @Override
    public void onTutorialStop(UUID playerUUID) {
        activeTutorials.remove(this);
        if (npc != null) npc.tutorialNPC.remove();
        if (stageTimeline != null) stageTimeline.onStopTimeLine(playerUUID);

        Bukkit.getLogger().log(Level.INFO, "There are " + activeTutorials.size() + " active tutorials.");
        Bukkit.getLogger().log(Level.INFO, "There are " + TutorialEventListener.runningEventTasks.size() + " tutorial event tasks running.");
    }

    /**
     * Gets all registered stages of the tutorial.
     * @return list of all stages
     */
    public List<Class<? extends AbstractStage>> getStages() {
        return stages;
    }

    /**
     * Gets all registered worlds of the tutorial.
     * @return list of all worlds
     */
    public List<TutorialWorld> getWorlds() {
        return worlds;
    }



    /**
     * Gets all active tutorials currently being run by a player.
     * @return list of all active tutorials
     */
    public static List<Tutorial> getActiveTutorials() {
        return activeTutorials;
    }

    /**
     * Gets the active tutorial of a player.
     * @param playerUUID uuid of the player
     * @return tutorial of the player, can be null
     */
    public static Tutorial getActiveTutorial(UUID playerUUID) {
        return AbstractTutorial.activeTutorials.stream().filter(tutorial ->
                tutorial.getPlayer().getUniqueId().toString().equals(playerUUID.toString())).findAny().orElse(null);
    }

    /**
     * This method needs to be called before any tutorial can be loaded.
     * @param tutorials list of all tutorials available
     */
    public static void setTutorials(List<Class<? extends AbstractTutorial>> tutorials) {
        AbstractTutorial.tutorials = tutorials;
    }

    /**
     * This method loads a tutorial for a player. It will start with the first stage.
     * @param player player to load the tutorial for
     * @param tutorialId id of the tutorial to load
     * @return true if the tutorial was loaded successfully, otherwise false
     */
    public static boolean loadTutorial(Player player, int tutorialId) {
        return loadTutorial(player, tutorialId, -1);
    }

    /**
     * This method loads a tutorial for a player. It will start with the specified stage.
     * @param player player to load the tutorial for
     * @param tutorialId id of the tutorial to load
     * @param stageId id of the stage to start with
     * @return true if the tutorial was loaded successfully, otherwise false
     */
    public static boolean loadTutorial(Player player, int tutorialId, int stageId) {
        if (tutorials == null || tutorialId >= tutorials.size()) return false;
        if (getActiveTutorial(player.getUniqueId()) != null) return false;

        try {
            tutorials.get(tutorialId).getDeclaredConstructor(Player.class, int.class).newInstance(player, stageId);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            Bukkit.getLogger().log(Level.INFO, "An error occurred while loading tutorial!", ex);
            return false;
        }
        return true;
    }
}