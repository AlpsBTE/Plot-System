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

// import de.oliver.fancynpcs.FancyNpcs;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcData;
import de.oliver.fancynpcs.api.utils.SkinFetcher;
import de.oliver.fancynpcs.api.utils.SkinFetcher.SkinData;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Objects;

/**
 * FancyNpcs Wrapper class to manage NPC visibility and name-tag holograms.
 * Using SkinFetcher to get minecraft skin from signature or player UUID.
 *
 * @see SkinFetcher
 */
public abstract class AbstractNpc {
    /**
     * All active NPC constructed by the abstract class.
     */
    public static final List<AbstractNpc> activeNPCs = new ArrayList<>();
    private static final String EMPTY_TAG = "<empty>";
    private static final String IDENTIFIER_TAG = "alpslib_";

    /**
     * Get the NPC name.
     *
     * @param playerUUID Focused player.
     * @return The NPC name to be overridden.
     */
    public abstract String getDisplayName(UUID playerUUID);

    /**
     * Get the NPC Name suffix message to be added.
     *
     * @param playerUUID Focused player.
     * @return The NPC name suffix to be overridden.
     */
    public abstract String getActionTitle(UUID playerUUID);

    private final String id;
    private final SkinData skin;
    private Npc npc;
    private NpcHologram hologram;

    /**
     * Create a new NPC by custom skin texture and signature.
     *
     * @param id            Identifier for this NPC to prevent duplicate naming.
     * @param skinTexture   Minecraft skin texture value.
     * @param skinSignature Minecraft skin texture signature.
     * @see <a href="https://mineskin.org/">mineskin.org</a>
     * Minecraft skin texture and signature can be generated at Mineskin.
     */
    public AbstractNpc(String id, String skinTexture, String skinSignature) {
        this.id = id;
        this.skin = new SkinData(IDENTIFIER_TAG + id, skinTexture, skinSignature);
    }

    /**
     * Create NPC Data on a world with location, the npc will not be visible to any player yet.
     *
     * @param spawnPos     The NPC spawn location.
     * @param saveToFile   Whether the NPC Data will be persistent.
     * @param turnToPlayer Will the NPC always facing the player.
     */
    public void create(Location spawnPos, boolean saveToFile, boolean turnToPlayer) {
        if (npc != null) delete();

        NpcData npcData = new NpcData(id, UUID.randomUUID(), spawnPos);
        npc = FancyNpcsPlugin.get().getNpcAdapter().apply(npcData);
        npc.getData().setSkin(skin);
        npc.getData().setDisplayName(EMPTY_TAG);
        npc.getData().setTurnToPlayer(turnToPlayer);
        npc.setSaveToFile(saveToFile);
        // TODO: uncomment this after we have our npc visibility package
        // npc.getData().setOnlyVisibleTo(true);
        npc.create();
        FancyNpcsPlugin.get().getNpcManager().registerNpc(npc);
        hologram = new NpcHologram(IDENTIFIER_TAG + id, spawnPos, this);
        activeNPCs.add(this);
    }

    /**
     * Spawn the NPC on a location, this will only be visible to a focused player.
     *
     * @param player Focused player.
     * @see Npc#spawn(Player) FancyNpcs spawn method.
     */
    public void show(Player player) {
        if (npc == null) return;
        Bukkit.getScheduler().runTaskAsynchronously(FancyNpcsPlugin.get().getPlugin(), () -> {
            // TODO: uncomment this after we have our npc visibility package
            //npc.getData().showToPlayer(player.getUniqueId());
            npc.spawn(player);
            if (hologram != null && player.getWorld().getName().equals(Objects.requireNonNull(hologram.getLocation().getWorld()).getName()))
                hologram.create(player);
        });
    }

    /**
     * Spawn the NPC on a location, with any player be able to see.
     *
     * @see Npc#spawnForAll() FancyNpcs::Npc#spawnForAll()
     */
    public void showForAll() {
        if (npc == null) return;
        // TODO: uncomment this after we have our npc visibility package
        //npc.getData().setOnlyVisibleTo(false);
        npc.spawnForAll();
        if (hologram != null) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (player.getWorld().getName().equals(Objects.requireNonNull(hologram.getLocation().getWorld()).getName()))
                    Bukkit.getScheduler().runTask(FancyNpcsPlugin.get().getPlugin(), () -> hologram.create(player));
            });
        }
    }

    /**
     * Hide NPC from the focused player.
     *
     * @param player Focused player.
     */
    public void hide(Player player) {
        if (npc == null || !npc.getIsVisibleForPlayer().containsKey(player.getUniqueId())) return;
        hologram.remove(player.getUniqueId());
        // TODO: uncomment this after we have our npc visibility package
        // npc.getData().hideFromPlayer(player.getUniqueId());
        npc.remove(player);
    }

    /**
     * Update NPC name-tag hologram and NPC glow.
     *
     * @param playerUUID The focused player.
     * @param isVisible  Is the name-tag visible.
     * @param enableGlow Set npc to glow
     * @see NpcData#setGlowing(boolean)
     */
    public void setActionTitleVisibility(UUID playerUUID, boolean isVisible, boolean enableGlow) {
        if (hologram != null && hologram.getHologram(playerUUID) != null && !hologram.getHologram(playerUUID).isDisabled())
            hologram.setActionTitleVisibility(playerUUID, isVisible);
        if (npc != null) {
            if (npc.getData().isGlowing() != enableGlow) {
                npc.getData().setGlowing(enableGlow);
                npc.updateForAll();
            }
        }
    }

    /**
     * Delete this NPC.
     */
    public void delete() {
        if (npc != null) npc.removeForAll();
        FancyNpcsPlugin.get().getNpcManager().removeNpc(npc);
        if (hologram != null) hologram.delete();
        activeNPCs.remove(this);
    }

    /**
     * Get the identifier used to construct this NPC.
     *
     * @return Identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Get this NPC skin texture value.
     *
     * @return Minecraft Skin Value
     */
    public String getSkinTexture() {
        return skin.value();
    }

    /**
     * Get this NPC skin signature value.
     *
     * @return Minecraft Skin Signature
     */
    public String getSkinSignature() {
        return skin.signature();
    }

    /**
     * Get the FancyNpcs class created by this wrapper.
     *
     * @return npc
     */
    public Npc getNpc() {
        return npc;
    }

    /**
     * Get the NPC name-tag hologram.
     *
     * @return NpcHologram
     */
    public NpcHologram getHologram() {
        return hologram;
    }
}