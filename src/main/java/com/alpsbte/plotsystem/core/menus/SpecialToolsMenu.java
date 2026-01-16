package com.alpsbte.plotsystem.core.menus;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class SpecialToolsMenu extends AbstractMenu {
    public SpecialToolsMenu(Player player) {
        super(3, LangUtil.getInstance().get(player, LangPaths.MenuTitle.SPECIAL_TOOLS), player);
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set special items
        getMenu().getSlot(10).setItem(BARRIER);
        getMenu().getSlot(12).setItem(STRUCTURE_VOID);
        getMenu().getSlot(14).setItem(LIGHT_BLOCK);
        getMenu().getSlot(16).setItem(DEBUG_STICK);

        // Set back item
        getMenu().getSlot(22).setItem(MenuItems.backMenuItem(getMenuPlayer()));
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click event for special items
        getMenu().getSlot(10).setClickHandler((clickPlayer, clickInformation) -> setClickEvent(BARRIER));
        getMenu().getSlot(12).setClickHandler((clickPlayer, clickInformation) -> setClickEvent(STRUCTURE_VOID));
        getMenu().getSlot(14).setClickHandler((clickPlayer, clickInformation) -> setClickEvent(LIGHT_BLOCK));
        getMenu().getSlot(16).setClickHandler((clickPlayer, clickInformation) -> setClickEvent(DEBUG_STICK));

        // Set click event for back item
        getMenu().getSlot(22).setClickHandler((clickPlayer, clickInformation) -> new BuilderUtilitiesMenu(clickPlayer));
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

    private void setClickEvent(ItemStack item) {
        if (getMenuPlayer().getInventory().contains(item)) return;
        getMenuPlayer().getInventory().addItem(item);
        getMenuPlayer().playSound(getMenuPlayer().getLocation(), Sound.ENTITY_ITEM_PICKUP, 5.0f, 1.0f);
    }

    /**
     * @return Menu item
     */
    public static ItemStack getMenuItem(Player player) {
        return new ItemBuilder(Material.BRUSH, 1)
                .setName(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.SPECIAL_TOOLS), GOLD, BOLD))
                .setLore(new LoreBuilder()
                        .addLine(LangUtil.getInstance().get(player, LangPaths.MenuDescription.SPECIAL_TOOLS), true)
                        .build())
                .build();
    }

    private static final ItemStack BARRIER = new ItemBuilder(Material.BARRIER, 1)
            .setName(text("Barrier", GOLD, BOLD))
            .setEnchanted(true)
            .build();

    private static final ItemStack STRUCTURE_VOID = new ItemBuilder(Material.STRUCTURE_VOID, 1)
            .setName(text("Structure Void", GOLD, BOLD))
            .setEnchanted(true)
            .build();

    private static final ItemStack LIGHT_BLOCK = new ItemBuilder(Material.LIGHT, 1)
            .setName(text("Light Block", GOLD, BOLD))
            .setEnchanted(true)
            .build();

    private static final ItemStack DEBUG_STICK = new ItemBuilder(Material.DEBUG_STICK, 1)
            .setName(text("Debug Stick", GOLD, BOLD))
            .setEnchanted(true)
            .build();
}
