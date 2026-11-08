package com.alpsbte.plotsystem.core.menus;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.BaseItems;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import org.ipvp.canvas.slot.Slot;

import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class BuilderUtilitiesMenu extends AbstractMenu {
    private final List<UtilityButton> utilityButtons = new ArrayList<>();

    public BuilderUtilitiesMenu(Player player) {
        super(3, LangUtil.getInstance().get(player, LangPaths.MenuTitle.BUILDER_UTILITIES), player);

        if (!PlotUtils.isPlotWorld(player.getWorld())) {
            player.closeInventory();
            player.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(player, LangPaths.Message.Error.PLAYER_NEEDS_TO_BE_ON_PLOT)));
        }
    }

    @Override
    protected void setPreviewItems() {
        // Set back item
        getMenu().getSlot(22).setItem(MenuItems.backMenuItem(getMenuPlayer()));
        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {
        utilityButtons.clear();

        if (MenuAccess.canOpenCustomHeads()) {
            utilityButtons.add(new UtilityButton(
                    new ItemBuilder(Material.PLAYER_HEAD, 1)
                            .setName(Component.text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.CUSTOM_HEADS), GOLD, BOLD))
                            .setLore(new LoreBuilder()
                                    .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.CUSTOM_HEADS), true).build())
                            .build(),
                    (clickPlayer, _) -> clickPlayer.performCommand("hdb")
            ));
        }

        if (MenuAccess.canOpenBannerMaker()) {
            utilityButtons.add(new UtilityButton(
                    new ItemBuilder(Material.RED_BANNER, 1)
                            .setName(Component.text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuTitle.BANNER_MAKER), GOLD, BOLD))
                            .setLore(new LoreBuilder()
                                    .addLine(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.MenuDescription.BANNER_MAKER), true).build())
                            .build(),
                    (clickPlayer, _) -> clickPlayer.performCommand("bm")
            ));
        }

        utilityButtons.add(new UtilityButton(
                SpecialToolsMenu.getMenuItem(getMenuPlayer()),
                (clickPlayer, _) -> new SpecialToolsMenu(clickPlayer)
        ));

        int[] slots = MenuAccess.centeredActionSlots(utilityButtons.size());
        for (int i = 0; i < utilityButtons.size(); i++) {
            getMenu().getSlot(slots[i]).setItem(utilityButtons.get(i).item());
        }
    }

    @Override
    protected void setItemClickEventsAsync() {
        int[] slots = MenuAccess.centeredActionSlots(utilityButtons.size());
        for (int i = 0; i < utilityButtons.size(); i++) {
            getMenu().getSlot(slots[i]).setClickHandler(utilityButtons.get(i).clickHandler());
        }

        // Set click event for back item
        getMenu().getSlot(22).setClickHandler((clickPlayer, _) -> clickPlayer.performCommand("companion"));
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(MenuItems.borderItem())
                .pattern(Utils.FULL_MASK)
                .pattern(Utils.EMPTY_MASK)
                .pattern("111101111")
                .build();
    }

    /**
     * @return Menu item
     */
    public static ItemStack getMenuItem(Player player) {
        return new ItemBuilder(BaseItems.BUILDER_UTILITIES.getItem())
                .setName(Component.text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.BUILDER_UTILITIES), AQUA, BOLD))
                .setLore(new LoreBuilder()
                        .addLine(LangUtil.getInstance().get(player, LangPaths.MenuDescription.BUILDER_UTILITIES), true).build())
                .build();
    }

    private record UtilityButton(ItemStack item, Slot.ClickHandler clickHandler) {}
}
