package com.alpsbte.plotsystem.utils.items;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum BaseItems {
    COMPANION_ITEM("companion-item"),

    LEADERBOARD_PLOT("leaderboard-plot"),
    LEADERBOARD_SCORE("leaderboard-score"),

    PLOT_UNFINISHED("plot-unfinished"),
    PLOT_UNREVIEWED("plot-unreviewed"),
    PLOT_COMPLETED("plot-completed"),

    MENU_ERROR("menu-error"),
    MENU_CLOSE("menu-close"),

    REVIEW_ITEM("review-item"),
    REVIEW_ACCURACY("review-accuracy"),
    REVIEW_BLOCK_PALETTE("review-block-palette"),
    REVIEW_DETAILING("review-detailing"),
    REVIEW_TECHNIQUE("review-technique"),
    REVIEW_POINT_ZERO("review-point-zero"),
    REVIEW_POINT_ONE("review-point-one"),
    REVIEW_POINT_TWO("review-point-two"),
    REVIEW_POINT_THREE("review-point-three"),
    REVIEW_POINT_FOUR("review-point-four"),
    REVIEW_POINT_FIVE("review-point-five"),
    REVIEW_SUBMIT("review-submit"),
    REVIEW_CANCEL("review-cancel"),
    REVIEW_INFO_PLOT("review-info-plot"),

    SETTINGS_ITEM("settings-item");

    final ItemStack itemStack;

    BaseItems(String configPath) {
        String materialString = ConfigUtil.getInstance().configs[2].getString(configPath + ".material");
        Material material = Material.getMaterial(materialString == null ? "" : materialString, false);
        material = material == null ? Material.BARRIER : material;

        itemStack = new ItemBuilder(material).setItemModel(ConfigUtil.getInstance().configs[2].get(configPath + ".modelId")).build();
    }

    public ItemStack getItem() {
        return itemStack;
    }
}
