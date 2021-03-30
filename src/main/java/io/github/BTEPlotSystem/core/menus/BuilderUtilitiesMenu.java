package github.BTEPlotSystem.core.menus;

import github.BTEPlotSystem.core.system.plot.PlotManager;
import github.BTEPlotSystem.utils.ItemBuilder;
import github.BTEPlotSystem.utils.LoreBuilder;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

public class BuilderUtilitiesMenu extends AbstractMenu {

    public BuilderUtilitiesMenu(Player player) {
        super(3, "Builder Utilities", player);

        if(PlotManager.isPlotWorld(player.getWorld())) {
            Mask mask = BinaryMask.builder(getMenu())
                    .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                    .pattern("111111111")
                    .pattern("000000000")
                    .pattern("111111111")
                    .build();
            mask.apply(getMenu());

            addMenuItems();
            setItemClickEvents();

            getMenu().open(getMenuPlayer());
        } else {
            player.sendMessage(Utils.getErrorMessageFormat("You need to be on a plot in order to use this!"));
        }
    }

    @Override
    protected void addMenuItems() {
        // Add custom heads item
        getMenu().getSlot(10)
                .setItem(new ItemBuilder(Material.SKULL_ITEM, 1, (byte) 3)
                        .setName("§b§lCUSTOM HEADS")
                        .setLore(new LoreBuilder()
                                .addLine("Open the head menu to get a variety of custom heads.").build())
                        .build());

        // Add banner maker item
        getMenu().getSlot(13)
                .setItem(new ItemBuilder(Material.BANNER, 1, (byte) 14)
                        .setName("§b§lBANNER MAKER")
                        .setLore(new LoreBuilder()
                                .addLine("Open the banner maker menu to create your own custom banners.").build())
                        .build());

        // Add special blocks menu item
        getMenu().getSlot(16)
                .setItem(new ItemBuilder(Material.GOLD_BLOCK ,1)
                        .setName("§b§lSPECIAL BLOCKS")
                        .setLore(new LoreBuilder()
                                .addLine("Open the special blocks menu to get a variety of inaccessible blocks.").build())
                        .build());

        // Add back button item
        getMenu().getSlot(22).setItem(backMenuItem());
    }

    @Override
    protected void setItemClickEvents() {
        // Set click event for custom heads
        getMenu().getSlot(10).setClickHandler((clickPlayer, clickInformation) -> clickPlayer.performCommand("hdb"));

        // Set click event for banner maker
        getMenu().getSlot(13).setClickHandler((clickPlayer, clickInformation) -> clickPlayer.performCommand("bm"));

        // Set click event for special blocks
        getMenu().getSlot(16).setClickHandler((clickPlayer, clickInformation) -> new SpecialBlocksMenu(clickPlayer));

        // Set click event for back button
        getMenu().getSlot(22).setClickHandler((clickPlayer, clickInformation) -> clickPlayer.performCommand("companion"));
    }

    public static ItemStack getMenuItem() {
        return new ItemBuilder(Material.GOLD_AXE)
                .setName("§b§lBuilder Utilities")
                .setLore(new LoreBuilder()
                        .addLine("Get access to custom heads, banners and special blocks.").build())
                .build();
    }
}
