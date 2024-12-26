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

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcData;
import de.oliver.fancynpcs.api.utils.SkinFetcher;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TutorialNPC {
    public static final List<TutorialNPC> activeTutorialNPCs = new ArrayList<>();
    private static final String EMPTY_TAG = "<empty>";

    private final String id;
    private final String displayName;
    private final String interactionPrompt;
    private final SkinFetcher.SkinData skin;

    private Npc npc;
    private TutorialNPCHologram hologram;

    /**
     * Constructs a new {@link TutorialNPC} with the specified ID, display name, skin texture, and skin signature.
     *
     * @param npcId The unique identifier for the NPC.
     * @param npcDisplayName The display name of the NPC as a {@link String}.
     * @param npcSknTexture The skin texture of the NPC, represented as a {@code String}.
     * @param npcSkinSignature The signature associated with the NPC skin texture, represented as a {@code String}.
     */
    public TutorialNPC(String npcId, String npcDisplayName, String npcInteractionPrompt, String npcSknTexture, String npcSkinSignature) {
        this.id = npcId;
        this.displayName = npcDisplayName;
        this.interactionPrompt = npcInteractionPrompt;
        this.skin = new SkinFetcher.SkinData(npcId, npcSknTexture, npcSkinSignature);
    }

    /**
     * Creates a new NPC at the specified spawn position.
     *
     * @param spawnPos The location where the NPC should be spawned.
     */
    public void create(Location spawnPos) {
        if (npc != null) delete();

        NpcData npcData = new NpcData(id, UUID.randomUUID(), spawnPos);
        npc = FancyNpcsPlugin.get().getNpcAdapter().apply(npcData);
        npc.getData().setSkin(skin);
        npc.getData().setDisplayName(EMPTY_TAG);
        npc.getData().setTurnToPlayer(true);
        npc.setSaveToFile(false);
        npc.create();
        hologram = new TutorialNPCHologram(id, spawnPos, this);
        activeTutorialNPCs.add(this);
    }

    /**
     * Spawns the NPC and its associated hologram for the specified player.
     *
     * @param player The player for whom the NPC and hologram should be spawned.
     */
    public void spawn(Player player) {
        if (npc == null) return;
        Bukkit.getScheduler().runTaskAsynchronously(FancyNpcsPlugin.get().getPlugin(), () -> {
            npc.spawn(player);
            if (hologram != null && player.getWorld().getName().equals(
                    Objects.requireNonNull(hologram.getLocation().getWorld()).getName()))
                hologram.create(player);
        });
    }

    /**
     * Moves the NPC to a new location and updates its hologram.
     *
     * @param player The tutorial player.
     * @param newLoc The new {@link Location} to move the NPC to.
     */
    public void move(Player player, Location newLoc) {
        if (npc == null) return;
        npc.getData().setLocation(newLoc);
        if (hologram != null) hologram.delete();
        hologram = new TutorialNPCHologram(id, newLoc, this);
        spawn(player);
    }

    /**
     * Deletes the NPC and its associated hologram from the game.
     */
    public void delete() {
        if (npc != null) npc.removeForAll();
        if (hologram != null) hologram.delete();
        activeTutorialNPCs.remove(this);
    }

    /**
     * Sets the visibility of the interaction prompt (e.g., "Right Click") for the specified player
     * and controls the glowing state of the associated NPC.
     *
     * @param playerUUID The UUID of the player for whom the interaction prompt visibility is being set.
     * @param isVisible  {@code true} to show the interaction prompt, or {@code false} to hide it.
     * @param enableGlow {@code true} to enable glowing for the NPC, or {@code false} to disable it.
     */
    public void setInteractionPromptVisibility(UUID playerUUID, boolean isVisible, boolean enableGlow) {
        if (hologram != null && hologram.getHologram(playerUUID) != null && !hologram.getHologram(playerUUID).isDisabled())
            hologram.setInteractionPromptVisibility(playerUUID, isVisible);
        if (npc != null && npc.getData().isGlowing() != enableGlow) {
                npc.getData().setGlowing(enableGlow);
                npc.updateForAll();
        }
    }

    public Npc getNpc() {
        return npc;
    }

    public TutorialNPCHologram getHologram() {
        return hologram;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getInteractionPrompt() {
        return interactionPrompt;
    }

    public SkinFetcher.SkinData getSkin() {
        return skin;
    }
}
