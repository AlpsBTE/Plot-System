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

import com.alpsbte.plotsystem.core.system.tutorial.stage.StageTimeline;
import com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialNPC;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.AbstractTask;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public interface Tutorial {

    /**
     * Gets the ID of the Tutorial
     *
     * @return id, cannot be negative
     */
    int getId();

    /**
     * Gets the name of the Tutorial
     *
     * @return name
     */
    String getName();

    /**
     * Gets the UUID of the player who is currently doing the tutorial
     *
     * @return uuid of the player
     */
    UUID getPlayerUUID();

    /**
     * Gets the player who is currently doing the tutorial
     *
     * @return player
     */
    Player getPlayer();

    /**
     * Gets the NPC of the current tutorial world. Can be null if npc has not yet been created.
     *
     * @return NPC
     */
    TutorialNPC getNPC();

    /**
     * Gets a list of all active holograms from the current tutorial stage.
     *
     * @return list of active tutorial holograms
     */
    List<AbstractTutorialHologram> getActiveHolograms();

    /**
     * Gets the current tutorial world the player is in
     *
     * @return bukkit world
     */
    World getCurrentWorld();

    /**
     * Gets the current stage the player is in.
     * This value does not have to be the highest stage the player has completed.
     *
     * @return stage id, cannot be negative
     */
    int getCurrentStage();

    /**
     * Sets the stage of the tutorial. This method does not save the stage.
     * It switches the given stage of the tutorial the player is currently in.
     *
     * @param stageId stage id, cannot be negative
     */
    void setStage(int stageId);

    /**
     * Saves the current stage of the tutorial the completion state.
     *
     * @param stageId stage id, cannot be negative
     */
    void saveTutorial(int stageId);

    /**
     * Gets the individual tutorial config
     *
     * @return tutorial config
     */
    FileConfiguration getConfig();

    /**
     * This method is called when the player completes a stage.
     *
     * @param playerUUID uuid of the player
     * @see StageTimeline#onTaskDone(AbstractTask)
     */
    void onStageComplete(UUID playerUUID);

    /**
     * This method is called when the player switches the tutorial world.
     * This method is NOT called when the player switches out of the tutorial.
     *
     * @param playerUUID         uuid of the player
     * @param tutorialWorldIndex index of the tutorial world
     * @see AbstractTutorial#initWorlds()
     */
    void onSwitchWorld(UUID playerUUID, int tutorialWorldIndex);

    /**
     * This method is called when the player completes the tutorial.
     * This method is ONLY called when the player completes the tutorial for the first time!
     *
     * @param playerUUID uuid of the player
     * @see Tutorial#onTutorialStop(UUID)
     */
    void onTutorialComplete(UUID playerUUID);

    /**
     * The method is called when the tutorial is stopped.
     * This can happen when tutorial is completed, when the player leaves the tutorial or when an error occurs.
     * For example by switching world or leaving the server.
     *
     * @param playerUUID uuid of the player
     */
    void onTutorialStop(UUID playerUUID);

    /**
     * This method is called when an error occurs.
     *
     * @param ex exception
     */
    void onException(Exception ex);
}
