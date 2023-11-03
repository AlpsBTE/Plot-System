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

package com.alpsbte.plotsystem.core.menus;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.alpslib.utils.heads.CustomHead;
import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import li.cinnazeyy.langlibs.core.file.LanguageFile;
import li.cinnazeyy.langlibs.core.language.LangLibAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import java.util.Objects;
import java.util.logging.Level;

public class SelectLanguageMenu extends AbstractMenu {

    public SelectLanguageMenu(Player player) {
        super(3, LangUtil.getInstance().get(player, LangPaths.MenuTitle.SELECT_LANGUAGE), player);
    }

    @Override
    protected void setPreviewItems() { super.setPreviewItems(); }

    @Override
    protected void setMenuItemsAsync() {
        // Set language items
        for (int i = 1; i <= LangUtil.getInstance().languageFiles.length; i++) {
            LanguageFile langFile = LangUtil.getInstance().languageFiles[i - 1];
            getMenu().getSlot(i + ((i >= 8) ? 2 : 0)).setItem(
                    new ItemBuilder(AlpsUtils.getItemHead(new CustomHead(langFile.getLanguage().HeadId)))
                            .setName("§6§l" + langFile.getLanguage().Name + "(" + langFile.getLanguage().Region + ")")
                            .build());
        }

        // Set back item
        getMenu().getSlot(22).setItem(MenuItems.backMenuItem(getMenuPlayer()));

        // Set auto-detect language item
        getMenu().getSlot(25).setItem(
                new ItemBuilder(new ItemStack(Material.SLIME_BALL))
                        .setName("§a§l" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.AUTO_DETECT_LANGUAGE))
                        .setLore(new LoreBuilder()
                                .addLines(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.AUTO_DETECT_LANGUAGE),
                                        "",
                                        "§6" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Note.Action.CLICK_TO_ENABLE))
                                .build())
                        .build());
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click event for language items
        for (int i = 1; i <= LangUtil.getInstance().languageFiles.length; i++) {
            LanguageFile langFile = LangUtil.getInstance().languageFiles[i - 1];
            getMenu().getSlot(i + ((i >= 8) ? 2 : 0)).setClickHandler((clickPlayer, clickInformation) -> {
                try {
                    getMenuPlayer().closeInventory();
                    LangLibAPI.setPlayerLang(Objects.requireNonNull(getMenuPlayer().getPlayer()),langFile.getLanguage().toString());
                    Utils.updatePlayerInventorySlots(clickPlayer);

                    getMenuPlayer().playSound(getMenuPlayer().getLocation(), Utils.SoundUtils.DONE_SOUND, 1f, 1f);
                    getMenuPlayer().sendMessage(Utils.ChatUtils.getInfoMessageFormat(
                            LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Info.CHANGED_LANGUAGE,
                                    langFile.getLanguage().Name)));
                } catch (Exception ex) {
                    Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
                    getMenuPlayer().playSound(getMenuPlayer().getLocation(), Utils.SoundUtils.ERROR_SOUND, 1f, 1f);
                }
            });
        }

        // Set click event for back item
        getMenu().getSlot(22).setClickHandler((clickPlayer, clickInformation) -> new SettingsMenu(clickPlayer));

        // Set click event for auto-detect language item
        getMenu().getSlot(25).setClickHandler((clickPlayer, clickInformation) -> {
            try {
                LangLibAPI.setPlayerLang(Objects.requireNonNull(getMenuPlayer().getPlayer()),getMenuPlayer().getLocale());
                reloadMenuAsync();
                getMenuPlayer().playSound(getMenuPlayer().getLocation(), Utils.SoundUtils.DONE_SOUND, 1f, 1f);
                getMenuPlayer().sendMessage(Utils.ChatUtils.getInfoMessageFormat(
                        LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Message.Info.CHANGED_LANGUAGE,
                                LangUtil.getInstance().getLanguageFileByPlayer(getMenuPlayer()).getLanguage().toString())));
            } catch (Exception ex) {
                Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
                getMenuPlayer().playSound(getMenuPlayer().getLocation(), Utils.SoundUtils.ERROR_SOUND, 1f, 1f);
            }
        });
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1).setName(" ").build())
                .pattern("000000000")
                .pattern("000000000")
                .pattern("111101111")
                .build();
    }
}
