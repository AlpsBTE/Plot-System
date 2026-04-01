package com.alpsbte.plotsystem.core.menus;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.BaseItems;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import org.bukkit.entity.Player;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class AbandonConfirmMenu extends AbstractMenu {
    private final Plot plot;

    public AbandonConfirmMenu(Player player, Plot plot) {
        super(3, "Abandon plot #" + plot.getId() + "?", player);
        this.plot = plot;
    }

    @Override
    protected void setMenuItemsAsync() {
        getMenu().getSlot(12)
                .setItem(new ItemBuilder(BaseItems.PLOT_ABANDON.getItem())
                        .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.ABANDON), RED).decoration(BOLD, true))
                        .setLore(new LoreBuilder()
                                .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.ABANDON), true)
                                .emptyLine()
                                .addLine(Utils.ItemUtils.getNoteFormat(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Note.WONT_BE_ABLE_CONTINUE_BUILDING)))
                                .build())
                        .build());

        getMenu().getSlot(14)
                .setItem(new ItemBuilder(BaseItems.MENU_BACK.getItem())
                        .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.CANCEL), RED).decoration(BOLD, true))
                        .build());
    }

    @Override
    protected void setItemClickEventsAsync() {
        getMenu().getSlot(12).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            clickPlayer.performCommand("plot abandon " + plot.getId());
        });

        getMenu().getSlot(14).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            new PlotActionsMenu(clickPlayer, plot);
        });
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(Utils.DEFAULT_ITEM)
                .pattern(Utils.FULL_MASK)
                .pattern(Utils.EMPTY_MASK)
                .pattern(Utils.FULL_MASK)
                .build();
    }
}
