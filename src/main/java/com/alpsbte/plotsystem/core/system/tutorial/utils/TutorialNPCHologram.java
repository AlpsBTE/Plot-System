/*
 * The MIT License (MIT)
 *
 *  Copyright © 2024, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.core.system.tutorial.utils;

import com.alpsbte.alpslib.hologram.DecentHologramDisplay;
import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

public class TutorialNPCHologram extends DecentHologramDisplay {
    private static final double NPC_HOLOGRAM_Y = 2.3;
    private static final double NPC_HOLOGRAM_Y_WITH_ACTION_TITLE = 2.6;

    private final TutorialNPC npc;
    private boolean isInteractionPromptVisible;

    private Location baseLocation;

    /**
     * Constructs a new {@link TutorialNPCHologram} instance at the specified location.
     * This constructor initializes the hologram with a unique identifier and positions it
     * above the specified location.
     * The hologram will be associated with the given NPC.
     *
     * @param id The unique identifier for the hologram. Must not be {@code null}.
     * @param location The initial location where the hologram will be displayed. Must not be {@code null}.
     * @param npc The {@link TutorialNPC} associated with this hologram. Must not be {@code null}.
     * @throws NullPointerException if any of the parameters are {@code null}.
     */
    public TutorialNPCHologram(@NotNull String id, Location location, TutorialNPC npc) {
        super(id, location.clone().add(0, NPC_HOLOGRAM_Y, 0), true);
        this.npc = npc;
        this.baseLocation = location;
    }

    @Override
    public void create(Player player) {
        Bukkit.getScheduler().runTask(FancyNpcsPlugin.get().getPlugin(), () -> {
            if (npc.getNpc().getIsVisibleForPlayer().containsKey(player.getUniqueId())
                    && npc.getNpc().getIsVisibleForPlayer().get(player.getUniqueId())) {
                super.create(player);
            }
        });
    }

    @Override
    public boolean hasViewPermission(UUID playerUUID) {
        return true;
    }

    @Override
    public ItemStack getItem() {
        return null;
    }

    @Override
    public String getTitle(UUID playerUUID) {
        return npc.getDisplayName();
    }

    @Override
    public List<DataLine<?>> getHeader(UUID playerUUID) {
        return Collections.singletonList(new DecentHologramDisplay.TextLine(this.getTitle(playerUUID)));
    }

    @Override
    public List<DataLine<?>> getContent(UUID playerUUID) {
        return new ArrayList<>();
    }

    @Override
    public List<DataLine<?>> getFooter(UUID playerUUID) {
        return isInteractionPromptVisible() ? Collections.singletonList(new TextLine(npc.getInteractionPrompt())) : new ArrayList<>();
    }

    @Override
    public void setLocation(Location newLocation) {
        this.baseLocation = newLocation;
        for (UUID playerUUID : getHolograms().keySet())
            getHolograms().get(playerUUID)
                    .setLocation(newLocation.add(0, isInteractionPromptVisible() ?
                            NPC_HOLOGRAM_Y_WITH_ACTION_TITLE : NPC_HOLOGRAM_Y, 0));
    }

    /**
     * Sets the visibility of the interaction prompt (e.g., "Right Click") for the specified player.
     * This method updates the visibility state of the interaction prompt for the player identified by {@code playerUUID}.
     * If {@code isVisible} is set to {@code true}, the prompt will be displayed;
     * otherwise, it will be hidden.
     * The NPC hologram location is adjusted accordingly based on the prompt's visibility state.
     *
     * @param playerUUID The UUID of the player for whom the interaction prompt visibility is being set.
     * @param isVisible {@code true} to show the interaction prompt, or {@code false} to hide it.
     * @throws NullPointerException if {@code playerUUID} is {@code null}.
     */
    public void setInteractionPromptVisibility(UUID playerUUID, boolean isVisible) {
        isInteractionPromptVisible = isVisible;
        getHologram(playerUUID).setLocation(baseLocation.clone().add(0, isInteractionPromptVisible() ?
                NPC_HOLOGRAM_Y_WITH_ACTION_TITLE : NPC_HOLOGRAM_Y, 0));
        reload(playerUUID);
    }

    /**
     * Checks if the interaction prompt (e.g., "Right Click") is currently visible.
     * This method returns the visibility state of the interaction prompt for this NPC.
     *
     * @return {@code true} if the interaction prompt is visible; {@code false} otherwise.
     */
    public boolean isInteractionPromptVisible() {
        return isInteractionPromptVisible;
    }
}
