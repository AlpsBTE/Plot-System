/*
 * The MIT License (MIT)
 *
 *  Copyright © 2025, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.utils.items;

import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public enum BaseItems {
    COMPANION_ITEM("companion-item"),

    LEADERBOARD_PLOT("leaderboard-plot"),
    LEADERBOARD_SCORE("leaderboard-score"),

    PLOT_UNFINISHED("plot-unfinished"),
    PLOT_UNREVIEWED("plot-unreviewed"),
    PLOT_COMPLETED("plot-completed"),
    PLOT_TYPE("plot-type"),
    PLOT_FOCUS_MODE("plot-focus-mode"),
    PLOT_CITY_INSPIRATION_MODE("plot-city-inspiration-mode"),

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
    REVIEW_CANCEL("review-cancel"),
    REVIEW_INFO("review-info"),
    REVIEW_INFO_PLOT("review-info-plot"),
    REVIEW_TOGGLE_DISABLED("review-toggle-disabled"),
    REVIEW_TOGGLE_CHECKED("review-toggle-checked"),

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
        itemStack = Utils.getConfiguredItem(materialString, customModelData);

        itemStack.getItemMeta().setAttributeModifiers(null);
        itemStack.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ENCHANTS);
    }

    public ItemStack getItem() {
        return itemStack;
    }
}
