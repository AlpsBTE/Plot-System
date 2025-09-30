package com.alpsbte.plotsystem.core.menus;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.BaseItems;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class PlotTypeMenu extends AbstractMenu {
    private Builder builder;

    public PlotTypeMenu(Player player) {
        super(3, LangUtil.getInstance().get(player, LangPaths.MenuTitle.SELECT_PLOT_TYPE), player);
    }

    @Override
    protected void setPreviewItems() {
        super.setPreviewItems();

        builder = Builder.byUUID(getMenuPlayer().getUniqueId());
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set plot type items
        getMenu().getSlot(11).setItem(
                new ItemBuilder(BaseItems.PLOT_FOCUS_MODE.getItem())
                        .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.SELECT_FOCUS_MODE), GOLD, BOLD))
                        .setLore(new LoreBuilder()
                                .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.SELECT_FOCUS_MODE), true)
                                .build())
                        .setEnchanted(builder.getPlotType().getId() == PlotType.FOCUS_MODE.getId())
                        .build());

        getMenu().getSlot(13).setItem(
                new ItemBuilder(Material.DARK_OAK_SAPLING, 1)
                        .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.SELECT_INSPIRATION_MODE), GOLD, BOLD))
                        .setLore(new LoreBuilder()
                                .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.SELECT_INSPIRATION_MODE), true)
                                .build())
                        .setEnchanted(builder.getPlotType().getId() == PlotType.LOCAL_INSPIRATION_MODE.getId())
                        .build());

        boolean inspirationModeDisabled = PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.DISABLE_CITY_INSPIRATION_MODE); // TODO remove or enhance as soon CIM is working again
        if (!inspirationModeDisabled) {
            getMenu().getSlot(15).setItem(
                    new ItemBuilder(BaseItems.PLOT_CITY_INSPIRATION_MODE.getItem())
                            .setName(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.SELECT_CITY_INSPIRATION_MODE), GOLD, BOLD)
                                    .append(text(" [", DARK_GRAY).append(text("BETA", RED).append(text("]", DARK_GRAY))))) // temporary BETA tag
                            .setLore(new LoreBuilder()
                                    .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.SELECT_CITY_INSPIRATION_MODE), true)
                                    .build())
                            .setEnchanted(builder.getPlotType().getId() == PlotType.CITY_INSPIRATION_MODE.getId())
                            .build());
        }


        // Set selected glass pane
        int selectedPlotTypeSlot = 13;
        if (builder.getPlotType() == PlotType.FOCUS_MODE)
            selectedPlotTypeSlot = 11;
        if (builder.getPlotType() == PlotType.CITY_INSPIRATION_MODE)
            selectedPlotTypeSlot = 15;
        getMenu().getSlot(selectedPlotTypeSlot - 9).setItem(new ItemBuilder(Material.LIME_STAINED_GLASS_PANE, 1).setName(empty()).build());


        // Set back item
        getMenu().getSlot(22).setItem(MenuItems.backMenuItem(getMenuPlayer()));
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click event for plot type items
        getMenu().getSlot(11).setClickHandler(((clickPlayer, clickInformation) -> {
            boolean successful = builder.setPlotType(PlotType.FOCUS_MODE);
            if (!successful) return;
            getMenuPlayer().playSound(getMenuPlayer().getLocation(), Utils.SoundUtils.DONE_SOUND, 1f, 1f);
            reloadMenuAsync();
        }));

        getMenu().getSlot(13).setClickHandler(((clickPlayer, clickInformation) -> {
            boolean successful = builder.setPlotType(PlotType.LOCAL_INSPIRATION_MODE);
            if (!successful) return;
            getMenuPlayer().playSound(getMenuPlayer().getLocation(), Utils.SoundUtils.DONE_SOUND, 1f, 1f);
            reloadMenuAsync();
        }));

        boolean inspirationModeDisabled = PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.DISABLE_CITY_INSPIRATION_MODE); // TODO improve or remove when CIM ist working again
        if (!inspirationModeDisabled) {
            getMenu().getSlot(15).setClickHandler(((clickPlayer, clickInformation) -> {
                boolean successful = builder.setPlotType(PlotType.CITY_INSPIRATION_MODE);
                if (!successful) return;
                getMenuPlayer().playSound(getMenuPlayer().getLocation(), Utils.SoundUtils.DONE_SOUND, 1f, 1f);
                reloadMenuAsync();
            }));
        }

        // Set click event for back item
        getMenu().getSlot(22).setClickHandler((clickPlayer, clickInformation) -> new SettingsMenu(clickPlayer));
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1).setName(empty()).build())
                .pattern(Utils.FULL_MASK)
                .pattern(Utils.EMPTY_MASK)
                .pattern("111101111")
                .build();
    }
}
