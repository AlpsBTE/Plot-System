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

package com.alpsbte.plotsystem.core.system.tutorial.tasks;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TeleportTask extends AbstractTask {
    private int tutorialWorldIndex;
    private Location location = null;

    public TeleportTask(Player player, int tutorialWorldIndex) {
        super(player);
        this.tutorialWorldIndex = tutorialWorldIndex;
    }

    public TeleportTask(Player player, Location location) {
        super(player);
        this.location = location;
    }

    @Override
    public void performTask() {
        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
            if (location == null) {
                AbstractTutorial.activeTutorials.forEach(t -> t.onSwitchWorld(player, tutorialWorldIndex));
            } else {
                player.teleport(location);
            }
            setTaskDone();
        });
    }
}
