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

import com.alpsbte.alpslib.hologram.DecentHologramDisplay;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.tutorial.stage.AbstractStage;
import com.alpsbte.plotsystem.core.system.tutorial.stage.StageTimeline;
import com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialNPC;
import com.alpsbte.plotsystem.core.system.tutorial.stage.TutorialWorld;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.text;

/**
 * Abstract class for all tutorials. Inherit this class to create a new tutorial.
 * There is no way to save the progress of a tutorial. This needs to be implemented manually.
 *
 * @see AbstractTutorial#saveTutorial(int)
 * @see AbstractTutorial#onTutorialComplete(UUID)
 */
public abstract class AbstractTutorial implements Tutorial {

    /**
     * This action method is used to set the stage preparation as done when switching to the next stage.
     *
     * @see AbstractTutorial#prepareStage(PrepareStageAction)
     */
    @FunctionalInterface
    protected interface PrepareStageAction {
        void setDone();
    }

    /**
     * A list of all registered tutorials.
     *
     * @see AbstractTutorial#registerTutorials(List)
     */
    private static final List<Class<? extends AbstractTutorial>> tutorials = new ArrayList<>();

    /**
     * A list of all active tutorials.
     *
     * @see AbstractTutorial#getActiveTutorial(UUID)
     */
    private static final List<AbstractTutorial> activeTutorials = new ArrayList<>();

    /**
     * A list of the last interaction of a player with a NPC or the chat.
     */
    private static final Map<UUID, Long> playerInteractionHistory = new HashMap<>();
    private static final long PLAYER_INTERACTION_COOLDOWN = 1000; // The cooldown for player interactions in milliseconds


    protected final TutorialDataModel tutorialDataModel;
    private final int tutorialId;
    private final UUID playerUUID;
    private final Player player;


    protected AbstractStage currentStage;
    protected List<Class<? extends AbstractStage>> stages;
    private int currentStageIndex;
    private List<TutorialWorld> worlds;
    protected int currentWorldIndex = -1;


    private StageTimeline stageTimeline;
    private TutorialNPC npc;

    /**
     * Creates a new instance of the tutorial current stage.
     *
     * @return current stage
     * @throws NoSuchMethodException     if the constructor of the current stage could not be found
     * @throws InvocationTargetException if the constructor of the current stage could not be invoked
     * @throws InstantiationException    if the current stage could not be instantiated
     * @throws IllegalAccessException    if the current stage could not be accessed
     */
    protected abstract AbstractStage getStage() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException;

    /**
     * Initializes all worlds that can be used in the tutorial.
     *
     * @return list of all worlds
     */
    protected abstract List<TutorialWorld> initWorlds();

    /**
     * Initializes all stages of the tutorial as classes.
     *
     * @return list of all stages
     */
    protected abstract List<Class<? extends AbstractStage>> initStages();

    /**
     * Initializes the individual npc for the tutorial player.
     *
     * @return tutorial npc
     */
    protected abstract TutorialNPC initNpc();

    /**
     * This method is called before the next stage timeline is started.
     * Use this method to do some preparations for the next stage.
     *
     * @param action action to set the stage preparation as done
     */
    protected abstract void prepareStage(PrepareStageAction action);

    protected AbstractTutorial(Player player, TutorialDataModel tutorialDataModel, int tutorialId, int stageId) {
        this.tutorialId = tutorialId;
        this.playerUUID = player.getUniqueId();
        this.player = player;
        this.tutorialDataModel = tutorialDataModel;

        if (stageId < 0) stageId = 0;
        currentStageIndex = stageId - 1;
    }

    /**
     * This method needs to be called before the tutorial can be started.
     */
    protected void initTutorial() {
        activeTutorials.add(this);
        worlds = initWorlds();
        stages = initStages();
        npc = initNpc();
        if (worlds == null || stages == null || npc == null) onException(new InstantiationException("Tutorial could not be initialised."));
    }

    @Override
    public int getId() {
        return tutorialId;
    }

    @Override
    public UUID getPlayerUUID() {
        return playerUUID;
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
    public List<AbstractTutorialHologram> getActiveHolograms() {
        return DecentHologramDisplay.activeDisplays.stream()
                .filter(AbstractTutorialHologram.class::isInstance)
                .filter(holo -> holo.isVisible(playerUUID))
                .map(h -> (AbstractTutorialHologram) h)
                .collect(Collectors.toList());
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
        if (stageTimeline != null) stageTimeline.onStopTimeLine();
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
            onTutorialStop(player.getUniqueId());
            onTutorialComplete(player.getUniqueId());
        } else {
            try {
                // Switch to the next stage
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
                onException(ex);
            }
        }
    }

    @Override
    public void onStageComplete(UUID playerUUID) {
        if (!player.getUniqueId().toString().equals(playerUUID.toString())) return;
        saveTutorial(getCurrentStage() + 1);
        nextStage();
    }

    @Override
    public void onSwitchWorld(UUID playerUUID, int tutorialWorldIndex) {
        if (!player.getUniqueId().toString().equals(playerUUID.toString())) return;
        if (currentWorldIndex == tutorialWorldIndex) return;
        currentWorldIndex = tutorialWorldIndex;

        TutorialWorld world = worlds.get(tutorialWorldIndex);
        player.teleport(world.getPlayerSpawnLocation());
        if (npc.getNpc() != null) {
            npc.move(player, world.getNpcSpawnLocation());
        } else {
            npc.create(world.getNpcSpawnLocation());
            npc.spawn(player);
        }
    }

    @Override
    public void onTutorialStop(UUID playerUUID) {
        if (!player.getUniqueId().toString().equals(playerUUID.toString())) return;
        if (stageTimeline != null) stageTimeline.onStopTimeLine();
        npc.delete();
        playerInteractionHistory.remove(playerUUID);
        activeTutorials.remove(this);
    }

    @Override
    public void onException(Exception ex) {
        PlotSystem.getPlugin().getComponentLogger().error(text("An error occurred while processing tutorial!"), ex);

        // Send player back to hub after 3 seconds if an error occurred
        Bukkit.getScheduler().runTaskLater(PlotSystem.getPlugin(), () -> onTutorialStop(player.getUniqueId()), 20 * 3);
    }

    /**
     * Gets all registered stages of the tutorial.
     *
     * @return list of all stages
     */
    public List<Class<? extends AbstractStage>> getStages() {
        return stages;
    }

    /**
     * Gets all registered worlds of the tutorial.
     *
     * @return list of all worlds
     */
    public List<TutorialWorld> getWorlds() {
        return worlds;
    }


    /**
     * Gets all active tutorials currently being run by a player.
     *
     * @return list of all active tutorials
     */
    public static List<AbstractTutorial> getActiveTutorials() {
        return activeTutorials;
    }

    /**
     * Gets the active tutorial of a player.
     *
     * @param playerUUID uuid of the player
     * @return tutorial of the player, can be null
     */
    public static AbstractTutorial getActiveTutorial(UUID playerUUID) {
        return AbstractTutorial.activeTutorials.stream().filter(tutorial ->
                tutorial.getPlayerUUID().toString().equals(playerUUID.toString())).findAny().orElse(null);
    }

    /**
     * Checks if a player can interact with the tutorial npc or chat input.
     *
     * @param playerUUID uuid of the player
     * @return true if the player can interact, otherwise false
     */
    public static boolean isPlayerIsOnInteractCoolDown(UUID playerUUID) {
        if (playerInteractionHistory.containsKey(playerUUID)) {
            long lastInteraction = playerInteractionHistory.get(playerUUID);
            if (System.currentTimeMillis() - lastInteraction <= PLAYER_INTERACTION_COOLDOWN) return true;
        }
        playerInteractionHistory.put(playerUUID, System.currentTimeMillis());
        return false;
    }

    /**
     * This method loads a tutorial for a player. It will start with the first stage.
     *
     * @param player     player to load the tutorial for
     * @param tutorialId id of the tutorial to load
     * @return true if the tutorial was loaded successfully, otherwise false
     */
    public static boolean loadTutorial(Player player, int tutorialId) {
        return loadTutorialByStage(player, tutorialId, -1);
    }

    /**
     * This method loads a tutorial for a player. It will start with the specified stage.
     *
     * @param player     player to load the tutorial for
     * @param tutorialId id of the tutorial to load
     * @param stageId    id of the stage to start with
     * @return true if the tutorial was loaded successfully, otherwise false
     */
    public static boolean loadTutorialByStage(Player player, int tutorialId, int stageId) {
        if (tutorialId >= tutorials.size()) return false;
        if (getActiveTutorial(player.getUniqueId()) != null) return false;

        try {
            tutorials.get(tutorialId).getDeclaredConstructor(Player.class, int.class).newInstance(player, stageId);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("An error occurred while loading tutorial!"), ex);

            return false;
        }
        return true;
    }

    /**
     * This method needs to be called before any tutorial can be loaded.
     *
     * @param tutorials list of all tutorials to register
     */
    public static void registerTutorials(List<Class<? extends AbstractTutorial>> tutorials) {
        AbstractTutorial.tutorials.addAll(tutorials);
    }
}