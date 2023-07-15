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
import com.alpsbte.plotsystem.core.holograms.TutorialHologram;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class HologramMessageTask extends AbstractTask {
    private BukkitTask waitTask = null;
    private final Sound soundEffect;
    private final TutorialHologram hologram;
    private final long interval;

    public HologramMessageTask(Player player, TutorialHologram hologram, Sound soundEffect, long interval) {
        super(player);
        this.soundEffect = soundEffect;
        this.hologram = hologram;
        this.interval = interval;
    }

    @Override
    public void performTask() {
        hologram.updateInterval(interval);
        hologram.nextPage();
        if (soundEffect != null) player.playSound(player.getLocation(), soundEffect, 1f, 1f);

        waitTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (hologram.changePageTask.isCancelled()) {
                    setTaskDone();
                    waitTask.cancel();
                }
            }
        }.runTaskTimer(PlotSystem.getPlugin(), 0, 1);
    }

    @Override
    public String toString() {
        return "HologramMessageTask";
    }
}
