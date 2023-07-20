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

import com.alpsbte.plotsystem.core.holograms.TutorialHologram;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

public class HologramMessageTask extends AbstractTask {
    private final TutorialHologram hologram;
    private final Sound soundEffect;
    private final List<String> content;

    public HologramMessageTask(Player player, TutorialHologram hologram, Sound soundEffect, List<String> content) {
        super(player);
        this.soundEffect = soundEffect;
        this.hologram = hologram;
        this.content = content;
    }

    @Override
    public void performTask() {
        hologram.updateContent(content);
        if (soundEffect != null) player.playSound(player.getLocation(), soundEffect, 1f, 1f);
        setTaskDone();
    }

    @Override
    public String toString() {
        return "HologramMessageTask";
    }
}
