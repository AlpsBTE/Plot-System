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

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.core.menus.AbstractMenu;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import com.alpsbte.plotsystem.core.system.tutorial.BeginnerTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.TutorialCategory;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.io.TutorialPaths;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class TutorialsMenu extends AbstractMenu {
    private List<TutorialPlot> plots;
    // private boolean isBeginnerTutorialCompleted = false;

    public TutorialsMenu(Player menuPlayer) {
        super(6, "Tutorials", menuPlayer);
    }

    @Override
    protected void setPreviewItems() {
        // Set loading item for player head item
        getMenu().getSlot(4).setItem(MenuItems.loadingItem(Material.SKULL_ITEM, (byte) 3, getMenuPlayer()));

        // Set back item
        getMenu().getSlot(49).setItem(MenuItems.backMenuItem(getMenuPlayer()));

        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {
        // Load player head
        ItemStack playerHead = AlpsUtils.getPlayerHead(getMenuPlayer().getUniqueId());

        // Set player stats item
        getMenu().getSlot(4)
                .setItem(new ItemBuilder(playerHead)
                        .setName("§6§l" + getMenuPlayer().getName())
                        .build());

        // Set tutorial items
        try {
            plots = TutorialPlot.getPlots(getMenuPlayer().getUniqueId());
            // TutorialPlot beginnerTutorial = getPlotById(TutorialCategory.BEGINNER.getId());
            // isBeginnerTutorialCompleted = beginnerTutorial != null && beginnerTutorial.getStatus() == Status.completed;

            // Set beginner tutorial item
            getMenu().getSlot(22).setItem(getTutorialItem(TutorialCategory.BEGINNER.getId(),
                    ConfigUtil.getTutorialInstance().getBeginnerTutorial().getString(TutorialPaths.TUTORIAL_ITEM_NAME),
                    LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.TUTORIAL_BEGINNER),
                    LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.TUTORIAL_BEGINNER))
            );

            // Set advanced tutorial items
            getMenu().getSlot(28).setItem(getAdvancedTutorial(getMenuPlayer()));
            getMenu().getSlot(29).setItem(getAdvancedTutorial(getMenuPlayer()));
            getMenu().getSlot(31).setItem(getAdvancedTutorial(getMenuPlayer()));
            getMenu().getSlot(33).setItem(getAdvancedTutorial(getMenuPlayer()));
            getMenu().getSlot(34).setItem(getAdvancedTutorial(getMenuPlayer()));
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
    }

    @Override
    protected void setItemClickEventsAsync() {
        getMenu().getSlot(22).setClickHandler((clickPlayer, clickInformation) ->
                handleClickEvent(TutorialCategory.BEGINNER.getId(), clickInformation.getClickType()));

        getMenu().getSlot(28).setClickHandler((clickPlayer, clickInformation) ->
                handleClickEvent(-1, clickInformation.getClickType()));

        getMenu().getSlot(29).setClickHandler((clickPlayer, clickInformation) ->
                handleClickEvent(-1, clickInformation.getClickType()));

        getMenu().getSlot(31).setClickHandler((clickPlayer, clickInformation) ->
                handleClickEvent(-1, clickInformation.getClickType()));

        getMenu().getSlot(33).setClickHandler((clickPlayer, clickInformation) ->
                handleClickEvent(-1, clickInformation.getClickType()));

        getMenu().getSlot(34).setClickHandler((clickPlayer, clickInformation) ->
                handleClickEvent(-1, clickInformation.getClickType()));

        // Set click event for back item
        getMenu().getSlot(49).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            clickPlayer.performCommand("companion");
        });
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("111101111")
                .pattern("111111111")
                .pattern("111101111")
                .pattern("100101001")
                .pattern("111111111")
                .pattern("111101111")
                .build();
    }

    private TutorialPlot getPlotById(int tutorialId) {
        try {
            for (TutorialPlot plot : plots) {
                if (plot.getTutorialId() == tutorialId) {
                    return plot;
                }
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
        return null;
    }

    private void handleClickEvent(int tutorialId, ClickType clickType) {
        if (tutorialId >= 0 && tutorialId < TutorialCategory.values().length) {
            if (clickType == ClickType.LEFT) {
                TutorialPlot plot = getPlotById(tutorialId);
                try {
                    if (plot == null || plot.getStatus() != Status.completed) {
                        getMenuPlayer().closeInventory();
                        new BeginnerTutorial(getMenuPlayer());
                        return;
                    }
                } catch (SQLException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                }
            } else if (clickType == ClickType.RIGHT) {
                getMenuPlayer().closeInventory();
                // TODO: Open tutorial stages menu
            }
        }

        getMenuPlayer().playSound(getMenuPlayer().getLocation(), Sound.ENTITY_ITEMFRAME_ADD_ITEM, 0.8f, 0.8f);
    }

    private ItemStack getTutorialItem(int tutorialId, String itemName, String title, String desc) throws SQLException {
        return (tutorialId != TutorialCategory.BEGINNER.getId() /*&& !isBeginnerTutorialCompleted*/) ? getAdvancedTutorial(getMenuPlayer()) :
                constructTutorialItem(getMenuPlayer(), getPlotById(tutorialId), new ItemStack(Material.getMaterial(itemName)), title, desc);
    }

    private static ItemStack constructTutorialItem(Player player, TutorialPlot plot, ItemStack itemStack, String title, String desc) throws SQLException {
        // Create tutorial item lore
        LoreBuilder loreBuilder = new LoreBuilder().addLines("§7" + desc, StringUtils.EMPTY);
        if (plot == null || plot.getStatus() != Status.completed) {
            loreBuilder.addLine(LangUtil.getInstance().get(player, LangPaths.Note.Action.LEFT_CLICK) + " §8» " +
                    "§e" + (plot == null ? LangUtil.getInstance().get(player, LangPaths.Note.Action.TUTORIAL_START) :
                    LangUtil.getInstance().get(player, LangPaths.Note.Action.TUTORIAL_CONTINUE)));
        }
        loreBuilder.addLine(LangUtil.getInstance().get(player, LangPaths.Note.Action.RIGHT_CLICK) + " §8» " +
                "§e" + LangUtil.getInstance().get(player, LangPaths.Note.Action.TUTORIAL_SHOW_STAGES));

        // Create tutorial item
        return new ItemBuilder(itemStack)
                .setName("§b§l" + title)
                .setLore(loreBuilder.build())
                .build();
    }

    private static ItemStack getAdvancedTutorial(Player player) {
        return new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 15)
                .setName("§c§l" + LangUtil.getInstance().get(player, LangPaths.Note.UNDER_CONSTRUCTION))
                .build();
    }
}
