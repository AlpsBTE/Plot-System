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

package com.alpsbte.plotsystem.core.menus.tutorial;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.core.menus.AbstractMenu;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.Tutorial;
import com.alpsbte.plotsystem.core.system.tutorial.TutorialCategory;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.io.TutorialPaths;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.sql.SQLException;
import java.util.logging.Level;

public class TutorialStagesMenu extends AbstractMenu {
    private static final int totalStagesRows = 2;
    private static final int totalStagesSlots = totalStagesRows * (9 - 2);
    private static final int stagesPerRow = totalStagesSlots / totalStagesRows;

    private final int tutorialId;
    private final String tutorialItemName;

    private final int stagesInFirstRow;
    private final int stagesInSecondRow;
    private final int startSlotFirstRow;
    private final int startSlotSecondRow;

    private TutorialPlot plot;

    private int playerHighestStage = -1;
    private int playerCurrentStage = -1;
    private boolean isTutorialCompleted;

    public TutorialStagesMenu(Player menuPlayer, int tutorialId) {
        super(6, LangUtil.getInstance().get(menuPlayer, LangPaths.MenuTitle.TUTORIAL_STAGES), menuPlayer, false);
        this.tutorialId = tutorialId;
        int totalStages = ConfigUtil.getTutorialInstance().configs[tutorialId].getInt(TutorialPaths.TUTORIAL_STAGES);

        // Calculate the number of stages to place in the first row
        stagesInFirstRow = Math.min(stagesPerRow, totalStages);

        // Calculate the number of stages to place in the second row
        stagesInSecondRow = Math.min(stagesPerRow, totalStages - stagesInFirstRow);

        // Calculate the starting slot for the first row
        startSlotFirstRow = 9 * 2 + ((stagesPerRow - stagesInFirstRow) / 2) + 1; // 9 = slots, 2 = rows

        // Calculate the starting slot for the second row
        startSlotSecondRow = 9 * 3 + ((stagesPerRow - stagesInSecondRow) / 2) + 1;

        // Get tutorial item name
        tutorialItemName = ConfigUtil.getTutorialInstance().configs[tutorialId].getString(TutorialPaths.TUTORIAL_ITEM_NAME);

        reloadMenuAsync();
    }

    @Override
    protected void setPreviewItems() {
        // Set loading item for tutorial item
        getMenu().getSlot(4).setItem(MenuItems.loadingItem(Material.valueOf(tutorialItemName), getMenuPlayer()));

        // Place stages in the first row
        for (int i = 0; i < stagesInFirstRow; i++) {
            getMenu().getSlot(startSlotFirstRow + i).setItem(MenuItems.loadingItem(Material.STAINED_GLASS_PANE, (byte) 7, getMenuPlayer()));
        }

        // Place stages in the second row
        for (int i = 0; i < stagesInSecondRow; i++) {
            getMenu().getSlot(startSlotSecondRow + i).setItem(MenuItems.loadingItem(Material.STAINED_GLASS_PANE, (byte) 7, getMenuPlayer()));
        }

        // Set back item
        getMenu().getSlot(49).setItem(MenuItems.backMenuItem(getMenuPlayer()));

        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {
        try {
            plot = TutorialPlot.getPlot(getMenuPlayer().getUniqueId().toString(), tutorialId);
            if (plot != null) {
                playerHighestStage = plot.getStage();
                isTutorialCompleted = plot.getStatus() == Status.completed;
            }
            Tutorial tutorial = AbstractTutorial.getActiveTutorial(getMenuPlayer().getUniqueId());
            if (tutorial != null) playerCurrentStage = tutorial.getCurrentStage();
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.INFO, "A SQL error occurred!", ex);
        }

        // Place stages in the first row
        for (int i = 0; i < stagesInFirstRow; i++) {
            getMenu().getSlot(startSlotFirstRow + i).setItem(getStageItem(tutorialId, i));
        }

        // Place stages in the second row
        for (int i = 0; i < stagesInSecondRow; i++) {
            getMenu().getSlot(startSlotSecondRow + i).setItem(getStageItem(tutorialId, stagesInFirstRow + i));
        }

        // Set click event for back item
        getMenu().getSlot(49).setClickHandler((clickPlayer, clickInformation) -> new TutorialsMenu(clickPlayer));
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click events for stages in the first row
        for (int i = 0; i < stagesInFirstRow; i++) {
            setStageClickEvent(startSlotFirstRow + i, i);
        }

        // Set click events for stages in the second row
        for (int i = 0; i < stagesInSecondRow; i++) {
            setStageClickEvent(startSlotSecondRow + i, stagesInFirstRow + i);
        }
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("111101111")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("111101111")
                .build();
    }

    /**
     * Sets the click event for a stage item and loads the tutorial stage
     * @param slot The slot of the stage item
     * @param stageId The id of the stage
     */
    private void setStageClickEvent(int slot, int stageId) {
        getMenu().getSlot(slot).setClickHandler(((player, clickInformation) -> {
            if (playerHighestStage >= stageId && clickInformation.getClickType().isLeftClick()) {
                // Load the tutorial stage by id
                if (!AbstractTutorial.loadTutorial(getMenuPlayer(), tutorialId, stageId)) {
                    Tutorial tutorial = AbstractTutorial.getActiveTutorial(player.getUniqueId());
                    if (tutorial != null) {
                        if (tutorial.getId() == tutorialId && playerCurrentStage != stageId) {
                            getMenuPlayer().closeInventory();
                            tutorial.setStage(stageId);
                        }
                    } else {
                        getMenuPlayer().closeInventory();
                        getMenuPlayer().sendMessage(Utils.ChatUtils.getErrorMessageFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Error.ERROR_OCCURRED)));
                        getMenuPlayer().playSound(getMenuPlayer().getLocation(), Utils.SoundUtils.ERROR_SOUND, 1, 1);
                    }
                } else getMenuPlayer().closeInventory();
            }

            getMenuPlayer().playSound(getMenuPlayer().getLocation(), Sound.ENTITY_ITEMFRAME_ADD_ITEM, 0.8f, 0.8f);
        }));
    }

    /**
     * Gets the menu stage item by the given tutorial id and stage id
     * @param tutorialId The tutorial id
     * @param stageId The stage id
     * @return The menu stage item
     */
    private ItemStack getStageItem(int tutorialId, int stageId) {
        LoreBuilder lore = new LoreBuilder().addLine(getStageTitle(getMenuPlayer(), tutorialId, stageId + 1));
        boolean isInProgress = playerHighestStage == stageId && plot != null && !isTutorialCompleted;

        ChatColor titleColor = isInProgress ? ChatColor.YELLOW : (stageId < playerHighestStage || isTutorialCompleted ? ChatColor.GREEN : ChatColor.RED);
        ItemStack stageItem = new ItemStack(Material.STAINED_GLASS_PANE, stageId + 1, (byte) (isInProgress ? 4 : (stageId < playerHighestStage || isTutorialCompleted ? 5 : 14)));
        boolean isClickable = stageId != playerCurrentStage && stageId <= playerHighestStage;

        if (isClickable) lore.addLines(StringUtils.EMPTY, LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Note.Action.LEFT_CLICK) + " §8» " +
                "§e" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Note.Action.START));

        return new ItemBuilder(stageItem)
                .setName(titleColor + ChatColor.BOLD.toString() + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Tutorials.TUTORIALS_STAGE) + " " + (stageId + 1))
                .setLore(lore.build())
                .build();
    }

    /**
     * Gets the stage title by reading the language file manually.
     * @param player The player
     * @param tutorialId The tutorial id
     * @param stageId The stage id
     * @return The stage title
     */
    private static String getStageTitle(Player player, int tutorialId, int stageId) {
        String stageString = "stage-" + stageId;
        return LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS +
                TutorialCategory.values()[tutorialId].name().toLowerCase() + "." +
                stageString + "." + stageString + "-title");
    }
}
