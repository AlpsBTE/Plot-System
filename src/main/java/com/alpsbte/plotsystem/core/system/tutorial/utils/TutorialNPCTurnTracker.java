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

import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import org.bukkit.Location;

public class TutorialNPCTurnTracker implements Runnable {
    public static int turnToPlayerDistance = FancyNpcsPlugin.get().getFancyNpcConfig().getTurnToPlayerDistance();;

    @Override
    public void run() {
        for (AbstractTutorial tutorial : AbstractTutorial.getActiveTutorials()) {
            if (tutorial.getNPC() == null) continue;
            Location playerLoc = tutorial.getPlayer().getLocation();
            Location npcLoc = tutorial.getNPC().getNpc().getData().getLocation();
            if (npcLoc == null || !npcLoc.getWorld().getName()
                    .equalsIgnoreCase(playerLoc.getWorld().getName())) continue;

            double distance = playerLoc.distance(npcLoc);
            if (Double.isNaN(distance)) continue;

            if (distance < turnToPlayerDistance) {
                Location newLoc = playerLoc.clone();
                newLoc.setDirection(newLoc.subtract(npcLoc).toVector());
                tutorial.getNPC().getNpc().lookAt(tutorial.getPlayer(), newLoc);
            }
        }
    }
}
