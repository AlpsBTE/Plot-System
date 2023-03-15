/*
 * The MIT License (MIT)
 *
 *  Copyright © 2021-2022, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.core.holograms;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.utils.io.config.ConfigPaths;

import java.util.Arrays;
import java.util.List;

public class HologramManager {
    private static final List<HolographicDisplay> holograms = Arrays.asList(
            new ScoreLeaderboard(),
            new PlotsLeaderboard()
    );

    public static void reloadHolograms() {
        if (!PlotSystem.DependencyManager.isHolographicDisplaysEnabled()) return;
        for (HolographicDisplay hologram : holograms) {
            if (PlotSystem.getPlugin().getConfigManager().getConfig().getBoolean(hologram.getDefaultPath() + ConfigPaths.HOLOGRAMS_ENABLED)) {
                hologram.show();
            } else {
                hologram.hide();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends HolographicDisplay> T getHologram(String id) {
        return holograms.stream()
                .filter(h -> h.getHologramName().equals(id))
                // T will already be extending HolographicDisplay, so casting will work
                .map(h -> (T) h)
                .findFirst().orElse(null);
    }

    public static List<HolographicDisplay> getHolograms() {
        return holograms;
    }
}