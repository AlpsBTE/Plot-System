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

package com.alpsbte.plotsystem.core.system.tutorial.stage;

import com.alpsbte.alpslib.hologram.HolographicDisplay;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import me.filoghost.holographicdisplays.api.Position;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.md_5.bungee.api.ChatColor.BOLD;
import static net.md_5.bungee.api.ChatColor.GOLD;

public class TutorialNPC {
    private Villager npcVillager;
    private final NPCHologram npcHologram;

    public TutorialNPC(String npcId, String npcName, String holoFooter) {
        npcHologram = new NPCHologram(npcId, npcName, holoFooter);
    }

    public void teleport(Location loc) {
        if (npcVillager != null && npcVillager.getLocation().getWorld().getName().equals(loc.getWorld().getName())) {
            npcVillager.teleport(loc);
        } else {
            remove();
            npcVillager = (Villager) loc.getWorld().spawnEntity(loc, EntityType.VILLAGER);
            npcVillager.setAI(false);
            npcVillager.setSilent(true);
            npcVillager.setCustomNameVisible(false);

            AbstractTutorial.getTutorialNPCs().add(npcVillager.getUniqueId());
        }

        npcHologram.create(Position.of(loc));

         /*
            // Create NPC
            NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "§6§lTutorial");

            // Set NPC Skin
            String texture = PlotSystem.getPlugin().getConfig().getString(ConfigPaths.TUTORIAL_NPC_TEXTURE);
            String signature = PlotSystem.getPlugin().getConfig().getString(ConfigPaths.TUTORIAL_NPC_SIGNATURE);
            npc.getOrAddTrait(SkinTrait.class).setSkinPersistent("TutorialNPCSkin", signature, texture);

            // Set NPC Location
            npc.spawn(location);

            // Set NPC to look at player
            npc.faceLocation(player.getLocation());
        */
    }

    public void remove() {
        if (npcVillager != null) {
            AbstractTutorial.getTutorialNPCs().remove(npcVillager.getUniqueId());
            npcVillager.remove();
        }
        npcHologram.remove();
    }

    public void setHologramFooterVisibility(boolean isVisible, boolean enableGlow) {
        if (npcHologram.getHologram() != null && !npcHologram.getHologram().isDeleted()) npcHologram.setFooterVisibility(isVisible);
        if (npcVillager != null) npcVillager.setGlowing(enableGlow);
    }

    public Villager getVillager() {
        return npcVillager;
    }

    public NPCHologram getNpcHologram() {
        return npcHologram;
    }



    public static class NPCHologram extends HolographicDisplay {
        private static final double NPC_HOLOGRAM_Y = 2.4;
        private static final double NPC_HOLOGRAM_Y_WITH_FOOTER = 2.7;

        private final String npcName;
        private final String holoFooter;
        private boolean isFooterVisible = false;

        private Position position;

        public NPCHologram(@NotNull String id, String npcName, String holoFooter) {
            super(id);
            this.npcName = npcName;
            this.holoFooter = holoFooter;
        }

        @Override
        public void create(Position position) {
            this.position = position;
            super.create(position.add(0, NPC_HOLOGRAM_Y, 0));
        }

        @Override
        public ItemStack getItem() {
            return null;
        }

        @Override
        public String getTitle() {
            return GOLD + BOLD.toString() + npcName;
        }

        @Override
        public List<DataLine<?>> getHeader() {
            return Collections.singletonList(new TextLine(this.getTitle()));
        }

        @Override
        public List<DataLine<?>> getContent() {
            return new ArrayList<>();
        }

        @Override
        public List<DataLine<?>> getFooter() {
            return isFooterVisible ? Collections.singletonList(new TextLine(holoFooter)) : new ArrayList<>();
        }

        public void setFooterVisibility(boolean isVisible) {
            isFooterVisible = isVisible;
            getHologram().setPosition(position.add(0, isFooterVisible ? NPC_HOLOGRAM_Y_WITH_FOOTER : NPC_HOLOGRAM_Y, 0));
            reload();
        }

        public boolean isFooterVisible() {
            return isFooterVisible;
        }
    }
}
