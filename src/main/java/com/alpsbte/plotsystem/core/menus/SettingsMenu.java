package com.alpsbte.plotsystem.core.menus;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.BaseItems;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import org.bukkit.entity.Player;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.util.function.Consumer;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class SettingsMenu extends AbstractMenu {
    private Consumer<Player> onBack = player -> player.performCommand("companion");

    public SettingsMenu(Player player) {
        super(3, LangUtil.getInstance().get(player, LangPaths.MenuTitle.SETTINGS), player);
    }

    public SettingsMenu(Player player, Consumer<Player> onBack) {
        this(player);
        this.onBack = onBack;
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set language item
        getMenu().getSlot(11).setItem(
                new ItemBuilder(BaseItems.LANGUAGE_ITEM.getItem())
                        .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.SELECT_LANGUAGE), GOLD, BOLD))
                        .setLore(new LoreBuilder()
                                .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.SELECT_LANGUAGE))
                                .build())
                        .build());

        // Set Plot type item
        getMenu().getSlot(15).setItem(
                new ItemBuilder(BaseItems.PLOT_TYPE.getItem())
                        .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.SELECT_PLOT_TYPE), GOLD, BOLD))
                        .setLore(new LoreBuilder()
                                .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.SELECT_PLOT_TYPE))
                                .build())
                        .build());

        // Set back item
        getMenu().getSlot(22).setItem(MenuItems.backMenuItem(getMenuPlayer()));
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click event for language item
        getMenu().getSlot(11).setClickHandler(((clickPlayer, clickInformation) -> clickPlayer.performCommand("language")));

        // Set click event for plot type item
        getMenu().getSlot(15).setClickHandler(((clickPlayer, clickInformation) -> new PlotTypeMenu(clickPlayer)));

        // Set click event for back item
        getMenu().getSlot(22).setClickHandler((clickPlayer, clickInformation) -> onBack.accept(clickPlayer));
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(Utils.DEFAULT_ITEM)
                .pattern(Utils.FULL_MASK)
                .pattern(Utils.EMPTY_MASK)
                .pattern("111101111")
                .build();
    }
}
