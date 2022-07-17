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
        if (PlotSystem.DependencyManager.isHolographicDisplaysEnabled()) {
            for (HolographicDisplay hologram : holograms) {
                if (PlotSystem.getPlugin().getConfigManager().getConfig().getBoolean(hologram.getDefaultPath() + ConfigPaths.HOLOGRAMS_ENABLED)) {
                    hologram.show();
                } else {
                    hologram.hide();
                }
            }
        }
    }

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
