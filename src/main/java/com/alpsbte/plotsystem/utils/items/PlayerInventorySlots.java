package com.alpsbte.plotsystem.utils.items;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.menus.companion.CompanionMenu;
import com.alpsbte.plotsystem.core.menus.review.ReviewMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerInventorySlots {
    private static final String PERMISSION_COMPANION = "plotsystem.companion";
    private static final String PERMISSION_REVIEW = "plotsystem.review";
    private static final int SLOT_COMPANION = 8;
    private static final int SLOT_REVIEW = 7;

    public static void update(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(PlotSystem.getPlugin(), () -> {
            // Only place hotbar shortcuts the player can actually use
            if (player.hasPermission(PERMISSION_COMPANION)) {
                player.getInventory().setItem(SLOT_COMPANION, CompanionMenu.getMenuItem(player));
            } else {
                player.getInventory().setItem(SLOT_COMPANION, ItemStack.empty());
            }

            if (player.hasPermission(PERMISSION_REVIEW)) {
                player.getInventory().setItem(SLOT_REVIEW, ReviewMenu.getMenuItem(player));
            } else {
                player.getInventory().setItem(SLOT_REVIEW, ItemStack.empty());
            }
        });
    }

    public static boolean affectsItemVisibility(String permission) {
        return permission.equals(PERMISSION_COMPANION) || permission.equals(PERMISSION_REVIEW);
    }
}
