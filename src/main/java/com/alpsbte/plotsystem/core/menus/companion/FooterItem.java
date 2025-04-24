package com.alpsbte.plotsystem.core.menus.companion;

import org.bukkit.inventory.ItemStack;

public class FooterItem {
    public final ItemStack item;
    public final org.ipvp.canvas.slot.Slot.ClickHandler clickHandler;

    FooterItem(ItemStack item, org.ipvp.canvas.slot.Slot.ClickHandler clickHandler) {
        this.item = item;
        this.clickHandler = clickHandler;
    }

    FooterItem(ItemStack item) {
        this.item = item;
        this.clickHandler = null;
    }
}
