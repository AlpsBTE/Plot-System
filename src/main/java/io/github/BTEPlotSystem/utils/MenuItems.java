package github.BTEPlotSystem.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MenuItems {

    public static ItemStack closeMenuItem() {
        return new ItemBuilder(Material.BARRIER)
                .setName("§c§lClose")
                .setLore(new LoreBuilder()
                    .addLine("Close the menu").build())
                .build();
    }

    public static ItemStack backMenuItem() {
        return new ItemBuilder(Utils.getItemHead("9226"))
                .setName("§6§lBack")
                .setLore(new LoreBuilder()
                    .addLine("Go back to the last menu").build())
                .build();
    }

    public static ItemStack nextPageItem() {
        return new ItemBuilder(Utils.getItemHead("9223"))
                .setName("§6§lNext Page")
                .setLore(new LoreBuilder()
                    .addLine("Show the next page").build())
                .build();
    }

    public static ItemStack previousPageItem() {
        return new ItemBuilder(Utils.getItemHead("9226"))
                .setName("§6§lPrevious Page")
                .setLore(new LoreBuilder()
                        .addLine("Show the previous page").build())
                .build();
    }

    public static ItemStack errorItem() {
        return new ItemBuilder(Material.BARRIER)
                .setName("§c§lError")
                .setLore(new LoreBuilder()
                    .addLine("An internal error occurred! Please contact a staff member!").build())
                .build();
    }
}
