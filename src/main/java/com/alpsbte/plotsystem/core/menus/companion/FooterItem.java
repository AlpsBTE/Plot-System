package com.alpsbte.plotsystem.core.menus.companion;

import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.slot.Slot;

public record FooterItem(ItemStack item, Slot.ClickHandler clickHandler) {

    FooterItem(ItemStack item) {
        this(item, null);
    }
}
