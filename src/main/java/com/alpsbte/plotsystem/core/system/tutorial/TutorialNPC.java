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

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

public class TutorialNPC {
    public Villager tutorialNPC;

    public TutorialNPC(Location spawnLocation) {
        tutorialNPC = createTutorialNPC(spawnLocation);
    }

    private static Villager createTutorialNPC(Location spawnLocation) {
        // Temporary NPC till the PlotSystem is updated to 1.20
        Villager v = (Villager) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.VILLAGER);
        v.setAI(false);
        v.setCustomName("§6§lBob");
        // v.setGlowing(true);
        v.setSilent(true);
        return v;

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
        npc.faceLocation(player.getLocation());*/
    }
}
