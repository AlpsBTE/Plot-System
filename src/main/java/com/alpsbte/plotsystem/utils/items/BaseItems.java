package com.alpsbte.plotsystem.utils.items;

import com.alpsbte.alpslib.utils.item.ItemUtils;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public enum BaseItems {
    COMPANION_ITEM("companion-item"),

    LEADERBOARD_SCORE("leaderboard-score"),

    PLOT_UNFINISHED("plot-unfinished"),
    PLOT_UNREVIEWED("plot-unreviewed"),
    PLOT_COMPLETED("plot-completed"),
    PLOT_TYPE("plot-type"),
    PLOT_FOCUS_MODE("plot-focus-mode"),
    PLOT_CITY_INSPIRATION_MODE("plot-city-inspiration-mode"),
    PLOT_SLOT_EMPTY("plot-slot-empty"),
    PLOT_SLOT_FILLED("plot-slot-filled"),
    PLOT_SUBMIT("plot-submit"),
    PLOT_UNDO_SUBMIT("plot-undo-submit"),
    PLOT_ABANDON("plot-abandon"),
    PLOT_TELEPORT("plot-teleport"),

    MENU_ERROR("menu-error"),
    MENU_CLOSE("menu-close"),
    MENU_BACK("menu-back"),
    MENU_NEXT("menu-next"),
    MENU_ADD("menu-add"),
    MENU_REMOVE("menu-remove"),

    REVIEW_ITEM("review-item"),
    REVIEW_ACCURACY("review-accuracy"),
    REVIEW_BLOCK_PALETTE("review-block-palette"),
    REVIEW_POINT_ZERO("review-point-zero"),
    REVIEW_POINT_ONE("review-point-one"),
    REVIEW_POINT_TWO("review-point-two"),
    REVIEW_POINT_THREE("review-point-three"),
    REVIEW_POINT_FOUR("review-point-four"),
    REVIEW_POINT_FIVE("review-point-five"),
    REVIEW_SUBMIT("review-submit"),
    REVIEW_INFO("review-info"),
    REVIEW_INFO_PLOT("review-info-plot"),
    REVIEW_TOGGLE_OPTIONAL("review-toggle-optional"),
    REVIEW_TOGGLE_REQUIRED("review-toggle-required"),
    REVIEW_TOGGLE_CHECKED("review-toggle-checked"),
    REVIEW_FEEDBACK("review-feedback"),
    REVIEW_SCORE("review-score"),

    BUILDER_UTILITIES("builder-utilities"),
    FILTER_ITEM("filter-item"),
    SETTINGS_ITEM("settings-item"),
    RANDOM_PLOT_ITEM("random-plot-item"),

    DIFFICULTY_AUTOMATIC("difficulty-automatic"),
    DIFFICULTY_EASY("difficulty-easy"),
    DIFFICULTY_MEDIUM("difficulty-medium"),
    DIFFICULTY_HARD("difficulty-hard"),

    TUTORIAL_ITEM("tutorial-item"),

    LANGUAGE_ITEM("language-item");

    final ItemStack itemStack;

    BaseItems(String configPath) {
        String materialString = ConfigUtil.getInstance().configs[2].getString(configPath + ".material");
        materialString = materialString == null ? Material.BARRIER.name() : materialString;
        Object customModelData = ConfigUtil.getInstance().configs[2].get(configPath + ".modelId");
        itemStack = ItemUtils.getConfiguredItem(materialString, customModelData);

        itemStack.getItemMeta().setAttributeModifiers(null);
        itemStack.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
    }

    public @NotNull ItemStack getItem() {
        return itemStack.clone();
    }
}
