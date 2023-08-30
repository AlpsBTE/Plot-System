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
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.TutorialCategory;
import com.alpsbte.plotsystem.utils.Utils;
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
    private String beginnerTutorialItemName;
    // private boolean isBeginnerTutorialCompleted = false;

    public TutorialsMenu(Player menuPlayer) {
        super(6, LangUtil.getInstance().get(menuPlayer, LangPaths.MenuTitle.TUTORIALS), menuPlayer);
    }

    @Override
    protected void setPreviewItems() {
        // Load player head
        ItemStack playerHead = AlpsUtils.getPlayerHead(getMenuPlayer().getUniqueId());

        // Set player stats item
        getMenu().getSlot(4)
                .setItem(new ItemBuilder(playerHead)
                        .setName("§6§l" + getMenuPlayer().getName())
                        .build());

        // Set loading item for beginner tutorial
        beginnerTutorialItemName = ConfigUtil.getTutorialInstance().getBeginnerTutorial().getString(TutorialPaths.TUTORIAL_ITEM_NAME);
        getMenu().getSlot(22).setItem(MenuItems.loadingItem(Material.valueOf(beginnerTutorialItemName), getMenuPlayer()));

        // Set advanced tutorial items
        getMenu().getSlot(28).setItem(getAdvancedTutorialItem(getMenuPlayer()));
        getMenu().getSlot(29).setItem(getAdvancedTutorialItem(getMenuPlayer()));
        getMenu().getSlot(33).setItem(getAdvancedTutorialItem(getMenuPlayer()));
        getMenu().getSlot(34).setItem(getAdvancedTutorialItem(getMenuPlayer()));

        // Set back item
        getMenu().getSlot(49).setItem(MenuItems.backMenuItem(getMenuPlayer()));

        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set tutorial items
        try {
            plots = TutorialPlot.getPlots(getMenuPlayer().getUniqueId());
            // TutorialPlot beginnerTutorial = getPlotById(TutorialCategory.BEGINNER.getId());
            // isBeginnerTutorialCompleted = beginnerTutorial != null && beginnerTutorial.getStatus() == Status.completed;

            // Set beginner tutorial item
            getMenu().getSlot(22).setItem(getTutorialItem(TutorialCategory.BEGINNER.getId(), beginnerTutorialItemName,
                    LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.TUTORIAL_BEGINNER),
                    LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.TUTORIAL_BEGINNER))
            );
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click event for beginner tutorial item
        getMenu().getSlot(22).setClickHandler((clickPlayer, clickInfo) ->
                setTutorialClickEvent(TutorialCategory.BEGINNER.getId(), clickInfo.getClickType()));

        // Set click events for advanced tutorial items
        getMenu().getSlot(28).setClickHandler((clickPlayer, clickInfo) -> setTutorialClickEvent(-1, clickInfo.getClickType()));
        getMenu().getSlot(29).setClickHandler((clickPlayer, clickInfo) -> setTutorialClickEvent(-1, clickInfo.getClickType()));
        getMenu().getSlot(33).setClickHandler((clickPlayer, clickInfo) -> setTutorialClickEvent(-1, clickInfo.getClickType()));
        getMenu().getSlot(34).setClickHandler((clickPlayer, clickInfo) -> setTutorialClickEvent(-1, clickInfo.getClickType()));

        // Set click event for back item
        getMenu().getSlot(49).setClickHandler((clickPlayer, clickInfo) -> clickPlayer.performCommand("companion"));
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

    private TutorialPlot getPlotById(int tutorialId) {
        try {
            for (TutorialPlot plot : plots) {
                if (plot.getTutorialID() == tutorialId) {
                    return plot;
                }
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
        return null;
    }

    /**
     * Sets the click event for a tutorial item and loads the tutorial stage
     * @param tutorialId The tutorial id
     * @param clickType The click type (left or right)
     */
    private void setTutorialClickEvent(int tutorialId, ClickType clickType) {
        getMenuPlayer().playSound(getMenuPlayer().getLocation(), Sound.ENTITY_ITEMFRAME_ADD_ITEM, 0.8f, 0.8f);

        if (tutorialId >= 0 && tutorialId < TutorialCategory.values().length) {
            if (clickType == ClickType.LEFT) {
                TutorialPlot plot = getPlotById(tutorialId);
                try {
                    if (plot == null || !plot.isCompleted()) {
                        getMenuPlayer().closeInventory();
                        if (!AbstractTutorial.loadTutorial(getMenuPlayer(), tutorialId)) {
                            if (AbstractTutorial.getActiveTutorial(getMenuPlayer().getUniqueId()) != null) {
                                getMenuPlayer().sendMessage(Utils.ChatUtils.getErrorMessageFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Error.TUTORIAL_ALREADY_RUNNING)));
                            } else throw new Exception("Failed to load tutorial");
                        }
                    }
                } catch (Exception ex) {
                    getMenuPlayer().closeInventory();
                    Bukkit.getLogger().log(Level.SEVERE, "An error occurred while handling menu click event", ex);
                    getMenuPlayer().sendMessage(Utils.ChatUtils.getErrorMessageFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Error.ERROR_OCCURRED)));
                    getMenuPlayer().playSound(getMenuPlayer().getLocation(), Utils.SoundUtils.ERROR_SOUND, 1, 1);
                }
            } else if (clickType == ClickType.RIGHT) {
                new TutorialStagesMenu(getMenuPlayer(), tutorialId);
            }
        }
    }

    private ItemStack getTutorialItem(int tutorialId, String itemName, String title, String desc) throws SQLException {
        return (tutorialId != TutorialCategory.BEGINNER.getId() /*&& !isBeginnerTutorialCompleted*/) ? getAdvancedTutorialItem(getMenuPlayer()) :
                constructTutorialItem(getMenuPlayer(), getPlotById(tutorialId), new ItemStack(Material.getMaterial(itemName)), title, desc);
    }

    private static ItemStack constructTutorialItem(Player player, TutorialPlot plot, ItemStack itemStack, String title, String desc) throws SQLException {
        // Create tutorial item lore
        LoreBuilder loreBuilder = new LoreBuilder().addLines("§7" + desc, StringUtils.EMPTY);
        if (plot == null || !plot.isCompleted()) {
            loreBuilder.addLine(LangUtil.getInstance().get(player, LangPaths.Note.Action.LEFT_CLICK) + " §8» " +
                    "§e" + LangUtil.getInstance().get(player, LangPaths.Note.Action.START));
        }
        loreBuilder.addLine(LangUtil.getInstance().get(player, LangPaths.Note.Action.RIGHT_CLICK) + " §8» " +
                "§e" + LangUtil.getInstance().get(player, LangPaths.Note.Action.TUTORIAL_SHOW_STAGES));

        // Create tutorial item
        return new ItemBuilder(itemStack)
                .setName("§b§l" + title)
                .setLore(loreBuilder.build())
                .build();
    }

    private static ItemStack getAdvancedTutorialItem(Player player) {
        return new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 15)
                .setName("§c§l" + LangUtil.getInstance().get(player, LangPaths.Note.UNDER_CONSTRUCTION))
                .build();
    }

    public static ItemStack getTutorialItem(Player player) {
        return new ItemBuilder(AlpsUtils.getItemHead(Utils.HeadUtils.TUTORIAL_HEAD))
                .setName("§b§l" + LangUtil.getInstance().get(player, LangPaths.MenuTitle.TUTORIALS))
                .setLore(new LoreBuilder().addLine(LangUtil.getInstance().get(player, LangPaths.MenuDescription.TUTORIALS)).build())
                .build();
    }
}
