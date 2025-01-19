package com.alpsbte.plotsystem.core.holograms.leaderboards;

import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.LangPaths;

public enum LeaderboardTimeframe {
    DAILY(ConfigPaths.DISPLAY_OPTIONS_SHOW_DAILY),
    WEEKLY(ConfigPaths.DISPLAY_OPTIONS_SHOW_WEEKLY),
    MONTHLY(ConfigPaths.DISPLAY_OPTIONS_SHOW_MONTHLY),
    YEARLY(ConfigPaths.DISPLAY_OPTIONS_SHOW_YEARLY),
    LIFETIME(ConfigPaths.DISPLAY_OPTIONS_SHOW_LIFETIME);

    public final String configPath;
    public final String langPath;

    LeaderboardTimeframe(String configPath) {
        this.configPath = configPath;
        this.langPath = LangPaths.Leaderboards.PAGES + name();
    }
}
