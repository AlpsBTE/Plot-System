/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, ASEAN Build The Earth <bteasean@gmail.com>
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

package com.alpsbte.plotsystem.core.system.tutorial.connector;

import com.alpsbte.plotsystem.core.holograms.connector.DecentHologramDisplay;
// import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;
import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

/**
 * NPC name-tag hologram to show above npc head.
 */
public class NpcHologram extends DecentHologramDisplay {
    private static final double NPC_HOLOGRAM_Y = 2.3;
    private static final double NPC_HOLOGRAM_Y_WITH_ACTION_TITLE = 2.6;

    private final AbstractNpc npc;
    private final Map<UUID, Boolean> isActionTitleVisible = new HashMap<>();

    private Location baseLocation;

    /**
     * Create a new NPC name-tag Hologram.
     * @param id Any identifier to be set as hologram name.
     * @param location The location to create this hologram.
     * @param npc AbstractNpc to be assigned to this hologram.
     */
    public NpcHologram(@NotNull String id, Location location, AbstractNpc npc) {
        super(id, location.clone().add(0, NPC_HOLOGRAM_Y, 0), true);
        this.npc = npc;
        this.baseLocation = location;
    }

    /**
     * Create NPC name-tag, with view permission set to its assigned NPC's viewing player.
     * @param player The player that will be able to view this hologram
     */
    @Override
    public void create(Player player) {
        Bukkit.getScheduler().runTask(FancyNpcsPlugin.get().getPlugin(), () -> {
            if(npc.getNpc().getIsVisibleForPlayer().containsKey(player.getUniqueId())
                    && npc.getNpc().getIsVisibleForPlayer().get(player.getUniqueId())) {
                super.create(player);
            }
        });
    }

    /**
     * This always returns true since view permission check
     * of this hologram happens before creating the hologram.
     * @param playerUUID Focused player
     * @return TRUE
     */
    @Override
    public boolean hasViewPermission(UUID playerUUID) { return true; }

    @Override
    public ItemStack getItem() {
        return null;
    }

    @Override
    public String getTitle(UUID playerUUID) {
        return npc.getDisplayName(playerUUID);
    }

    @Override
    public List<DataLine<?>> getHeader(UUID playerUUID) {
        return Collections.singletonList(new TextLine(this.getTitle(playerUUID)));
    }

    @Override
    public List<DataLine<?>> getContent(UUID playerUUID) {
        return new ArrayList<>();
    }

    @Override
    public List<DataLine<?>> getFooter(UUID playerUUID) {
        return isActionTitleVisible(playerUUID) ? Collections.singletonList(new TextLine(npc.getActionTitle(playerUUID))) : new ArrayList<>();
    }

    @Override
    public void setLocation(Location newLocation) {
        this.baseLocation = newLocation;
        for (UUID playerUUID : getHolograms().keySet())
            getHolograms().get(playerUUID)
            .setLocation(newLocation.add(0, isActionTitleVisible(playerUUID) ?
                NPC_HOLOGRAM_Y_WITH_ACTION_TITLE : NPC_HOLOGRAM_Y, 0));
    }

    /**
     * Update this name-tag hologram's location and visibility.
     * @param playerUUID Focused player.
     * @param isVisible Set the hologram visible or not.
     */
    public void setActionTitleVisibility(UUID playerUUID, boolean isVisible) {
        isActionTitleVisible.put(playerUUID, isVisible);
        getHologram(playerUUID).setLocation(baseLocation.clone().add(0, isActionTitleVisible(playerUUID) ?
            NPC_HOLOGRAM_Y_WITH_ACTION_TITLE : NPC_HOLOGRAM_Y, 0));
        reload(playerUUID);
    }

    /**
     * Get whether this hologram is visible.
     * @param playerUUID Focused player.
     * @return isActionTitleVisible
     */
    public boolean isActionTitleVisible(UUID playerUUID) {
        return isActionTitleVisible.getOrDefault(playerUUID, false);
    }
}
