package com.alpsbte.plotsystem.core.menus;

import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum MenuBackground {
    NONE(null),
    COMPANION_CONTINENTS("companion-continents"),
    COMPANION_COUNTRIES("companion-countries"),
    COMPANION_CITY_PROJECTS("companion-city-projects"),
    PLAYER_PLOTS("player-plots"),
    PLOT_ACTIONS("plot-actions"),
    PLOT_MEMBERS("plot-members"),
    ABANDON_CONFIRM("abandon-confirm"),
    FEEDBACK("feedback"),
    PLOT_TYPE("plot-type"),
    SETTINGS("settings"),
    BUILDER_UTILITIES("builder-utilities"),
    SPECIAL_TOOLS("special-tools"),
    REVIEW("review"),
    REVIEW_PLOT("review-plot"),
    REVIEW_PLOT_TOGGLES("review-plot-toggles"),
    TUTORIALS("tutorials"),
    TUTORIAL_STAGES("tutorial-stages");

    private static final String PREFIX_PATH = "background-prefix";
    private static final String SUFFIX_PATH = "background-suffix";

    private final String configPath;

    MenuBackground(String configPath) {
        this.configPath = configPath;
    }

    public @Nullable String background() {
        if (configPath == null) return null;
        String value = getString(configPath);
        return value.isEmpty() ? null : value;
    }

    public boolean isActive() {
        return background() != null;
    }

    public @NotNull Component apply(@NotNull Component title) {
        String background = background();
        if (background == null) return title;
        return Component.text()
                .append(Component.text(getString(PREFIX_PATH) + background + getString(SUFFIX_PATH), NamedTextColor.WHITE))
                .append(title)
                .build();
    }

    private static @NotNull String getString(@NotNull String path) {
        String value = ConfigUtil.getInstance().configs[3].getString(path);
        return value == null ? "" : value;
    }
}
