package github.BTEPlotSystem.core.menus;

import github.BTEPlotSystem.core.plots.PlotManager;
import github.BTEPlotSystem.utils.ItemBuilder;
import github.BTEPlotSystem.utils.LoreBuilder;
import github.BTEPlotSystem.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import org.ipvp.canvas.type.ChestMenu;

public class BuilderUtilitiesMenu {

    private final Menu menu;

    public BuilderUtilitiesMenu(Player player) {
        this.menu = ChestMenu.builder(3).title("Builder Utilities").build();
        if(PlotManager.isPlotWorld(player.getWorld())) {
            Mask mask = BinaryMask.builder(menu)
                    .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                    .pattern("111111111")
                    .pattern("000000000")
                    .pattern("111111111")
                    .build();
            mask.apply(menu);

            setMenuItems();

            menu.open(player);
        } else {
            player.sendMessage(Utils.getErrorMessageFormat("You need to be on a plot in order to use this!"));
        }
    }

    private void setMenuItems() {
        // Set Custom Heads Item
        menu.getSlot(10)
                .setItem(new ItemBuilder(Material.SKULL_ITEM, 1, (byte) 3)
                        .setName("§b§lCUSTOM HEADS")
                        .setLore(new LoreBuilder().description("§7", "Open the head menu to get a variety of custom heads.").build())
                        .build());
        menu.getSlot(10).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.performCommand("hdb");
        });

        // Set Banner Maker Item
        menu.getSlot(13)
                .setItem(new ItemBuilder(Material.BANNER, 1, (byte) 14)
                        .setName("§b§lBANNER MAKER")
                        .setLore(new LoreBuilder().description("§7", "Open the banner maker menu to create your own custom banners.").build())
                        .build());
        menu.getSlot(13).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.performCommand("bm");
        });

        // Set Custom Blocks Item
        menu.getSlot(16)
                .setItem(new ItemBuilder(Material.GOLD_BLOCK ,1)
                        .setName("§b§lSPECIAL BLOCKS")
                        .setLore(new LoreBuilder().description("§7", "Open the special blocks menu to get a variety of inaccessible blocks.").build())
                        .build());
        menu.getSlot(16).setClickHandler((clickPlayer, clickInformation) -> {
            new SpecialBlocksMenu().getUI().open(clickPlayer);
        });
    }
}
