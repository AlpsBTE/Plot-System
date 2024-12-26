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

import com.alpsbte.alpslib.utils.head.AlpsHeadUtils;
import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.menus.AbstractMenu;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.TutorialCategory;
import com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialUtils;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.CustomHeads;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.sql.SQLException;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class TutorialsMenu extends AbstractMenu {
    private TutorialPlot plot;
    private String beginnerTutorialItemName;
    private boolean isBeginnerTutorialCompleted = false;

    public TutorialsMenu(Player menuPlayer) {
        super(6, LangUtil.getInstance().get(menuPlayer, LangPaths.MenuTitle.TUTORIALS), menuPlayer);
    }

    @Override
    protected void setPreviewItems() {
        // Load player head
        ItemStack playerHead = AlpsHeadUtils.getPlayerHead(getMenuPlayer().getUniqueId());

        // Set player stats item
        getMenu().getSlot(4)
                .setItem(new ItemBuilder(playerHead)
                        .setName(text(getMenuPlayer().getName(), GOLD, BOLD))
                        .build());

        // Set loading item for beginner tutorial
        beginnerTutorialItemName = ConfigUtil.getTutorialInstance().getBeginnerTutorial().getString(TutorialUtils.Path.TUTORIAL_ITEM_NAME);
        getMenu().getSlot(22).setItem(MenuItems.loadingItem(Material.valueOf(beginnerTutorialItemName), getMenuPlayer()));

        // Set advanced tutorial items
        getMenu().getSlot(28).setItem(getAdvancedTutorialItem(getMenuPlayer()));
        getMenu().getSlot(29).setItem(getAdvancedTutorialItem(getMenuPlayer()));
        getMenu().getSlot(33).setItem(getAdvancedTutorialItem(getMenuPlayer()));
        getMenu().getSlot(34).setItem(getAdvancedTutorialItem(getMenuPlayer()));

        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set tutorial items
        try {
            plot = TutorialPlot.getPlot(getMenuPlayer().getUniqueId().toString(), TutorialCategory.BEGINNER.getId());
            // TutorialPlot beginnerTutorial = getPlotById(TutorialCategory.BEGINNER.getId());
            if (plot != null) isBeginnerTutorialCompleted = plot.isCompleted();

            // Set beginner tutorial item
            getMenu().getSlot(22).setItem(getTutorialItem(TutorialCategory.BEGINNER.getId(), beginnerTutorialItemName,
                    LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.TUTORIAL_BEGINNER),
                    LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.TUTORIAL_BEGINNER))
            );

            // Set back item
            if (!PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.TUTORIAL_REQUIRE_BEGINNER_TUTORIAL) || isBeginnerTutorialCompleted)
                getMenu().getSlot(49).setItem(MenuItems.backMenuItem(getMenuPlayer()));
        } catch (SQLException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
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
        if (!PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.TUTORIAL_REQUIRE_BEGINNER_TUTORIAL) || isBeginnerTutorialCompleted)
            getMenu().getSlot(49).setClickHandler((clickPlayer, clickInfo) -> clickPlayer.performCommand("companion"));
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(Component.empty()).build())
                .pattern("111101111")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("111111111")
                .build();
    }

    /**
     * Sets the click event for a tutorial item and loads the tutorial stage
     *
     * @param tutorialId The tutorial id
     * @param clickType  The click type (left or right)
     */
    private void setTutorialClickEvent(int tutorialId, ClickType clickType) {
        if (tutorialId >= 0 && tutorialId < TutorialCategory.values().length) {
            if (clickType == ClickType.LEFT) {
                // TutorialPlot plot = getPlotById(tutorialId);
                try {
                    if (plot == null || !plot.isCompleted()) {
                        getMenuPlayer().closeInventory();
                        if (!AbstractTutorial.loadTutorial(getMenuPlayer(), tutorialId)) {
                            if (AbstractTutorial.getActiveTutorial(getMenuPlayer().getUniqueId()) != null) {
                                getMenuPlayer().sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Error.TUTORIAL_ALREADY_RUNNING)));
                            } else throw new Exception("Failed to load tutorial");
                        }
                        return;
                    }
                } catch (Exception ex) {
                    getMenuPlayer().closeInventory();
                    PlotSystem.getPlugin().getComponentLogger().error(text("An error occurred while handling menu click event!"), ex);
                    getMenuPlayer().sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Error.ERROR_OCCURRED)));
                    getMenuPlayer().playSound(getMenuPlayer().getLocation(), Utils.SoundUtils.ERROR_SOUND, 1, 1);
                    return;
                }
            }

            new TutorialStagesMenu(getMenuPlayer(), tutorialId);
            return;
        }

        getMenuPlayer().playSound(getMenuPlayer().getLocation(), Sound.ENTITY_ITEM_FRAME_ADD_ITEM, 0.8f, 0.8f);
    }

    private ItemStack getTutorialItem(int tutorialId, String itemName, String title, String desc) throws SQLException {
        return (tutorialId != TutorialCategory.BEGINNER.getId()) ? getAdvancedTutorialItem(getMenuPlayer()) :
                constructTutorialItem(getMenuPlayer(), tutorialId, plot, new ItemStack(Material.valueOf(itemName)), title, desc);
    }

    private static ItemStack constructTutorialItem(Player player, int tutorialId, TutorialPlot plot, ItemStack itemStack, String title, String desc) throws SQLException {
        // Create tutorial item lore
        int highestPlotStage = plot != null ? plot.getStageID() : 0;
        boolean isPlotCompleted = plot != null && plot.isCompleted();
        LoreBuilder loreBuilder = new LoreBuilder()
                .addLine(text(desc, GRAY), true)
                .emptyLine()
                .addLine(text(LangUtil.getInstance().get(player, LangPaths.Tutorials.STAGE) + ": ", GRAY)
                        .append(text((highestPlotStage + (isPlotCompleted ? 1 : 0)) + "/" +
                                ConfigUtil.getTutorialInstance().configs[tutorialId].getInt(TutorialUtils.Path.TUTORIAL_STAGES), WHITE)))
                .emptyLine();
        if (plot == null || !isPlotCompleted) {
            loreBuilder.addLine(text(LangUtil.getInstance().get(player, LangPaths.Note.Action.LEFT_CLICK), GRAY)
                    .append(text(" » ", DARK_GRAY))
                    .append(text(LangUtil.getInstance().get(player, LangPaths.Note.Action.START), YELLOW)));
        }
        loreBuilder.addLine(text(LangUtil.getInstance().get(player, LangPaths.Note.Action.RIGHT_CLICK), GRAY)
                .append(text(" » ", DARK_GRAY))
                .append(text( LangUtil.getInstance().get(player, LangPaths.Note.Action.TUTORIAL_SHOW_STAGES), YELLOW)));

        // Create tutorial item
        return new ItemBuilder(itemStack)
                .setName(text(title, AQUA, BOLD))
                .setLore(loreBuilder.build())
                .build();
    }

    private static ItemStack getAdvancedTutorialItem(Player player) {
        return new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setName(text(LangUtil.getInstance().get(player, LangPaths.Note.UNDER_CONSTRUCTION), RED, BOLD))
                .build();
    }

    public static ItemStack getTutorialItem(Player player) {
        return new ItemBuilder(AlpsHeadUtils.getCustomHead(CustomHeads.WORKBENCH.getId()))
                .setName(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.TUTORIALS), AQUA, BOLD))
                .setLore(new LoreBuilder().addLine(LangUtil.getInstance().get(player, LangPaths.MenuDescription.TUTORIALS), true).build())
                .build();
    }
}
