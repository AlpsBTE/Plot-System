package com.alpsbte.plotsystem.utils.items;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum BaseItems {
    COMPANION_ITEM("companion-item"),
    LEADERBOARD_PLOT("leaderboard-plot"),
    LEADERBOARD_SCORE("leaderboard-score"),
    REVIEW_ITEM("review-item"),
    PLOT_UNFINISHED("plot-unfinished"),
    PLOT_UNREVIEWED("plot-unreviewed"),
    PLOT_COMPLETED("plot-completed"),
    MENU_ERROR("menu-error"),
    MENU_CLOSE("menu-close"),
    REVIEW_ACCURACY("review-accuracy"),
    REVIEW_BLOCK_PALETTE("review-block-palette"),
    REVIEW_DETAILING("review-detailing"),
    REVIEW_TECHNIQUE("review-technique");

    final ItemStack itemStack;

    BaseItems(String configPath) {
        String materialString = ConfigUtil.getInstance().configs[2].getString(configPath + ".material");
        Material material = Material.getMaterial(materialString == null ? "" : materialString, false);
        material = material == null ? Material.BARRIER : material;

        int modelId = ConfigUtil.getInstance().configs[2].getInt(configPath + ".modelId");

        itemStack = new ItemBuilder(material).setItemModel(modelId).build();
    }

    public ItemStack getItem() {
        return itemStack;
    }
}
