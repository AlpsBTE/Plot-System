package github.BTEPlotSystem.core.menus;

import github.BTEPlotSystem.core.SpecialBlocks;
import github.BTEPlotSystem.utils.ItemBuilder;
import github.BTEPlotSystem.utils.LoreBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import org.ipvp.canvas.slot.Slot;
import org.ipvp.canvas.type.ChestMenu;

public class SpecialBlocksMenu extends SpecialBlocks {
    public Menu getUI() {
        Menu specialBlocksMenu = ChestMenu.builder(3).title("Special Blocks").redraw(true).build();

        // Set bottom glass border
        Mask mask = BinaryMask.builder(specialBlocksMenu)
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte)7).setName(" ").build())
                .pattern("000000000") // First row
                .pattern("000000000") // Second row
                .pattern("111101111").build(); // Third row
        mask.apply(specialBlocksMenu);

        // Set close button item
        specialBlocksMenu.getSlot(22).setItem(new ItemBuilder(Material.BARRIER, 1)
                .setName("§6§lCLOSE")
                .build());

        specialBlocksMenu.getSlot(22).setClickHandler((player, clickInformation) -> {
            player.closeInventory();
        });

        // Set special blocks items
        for(int i = 0; i <= 14; i++) {
            Slot slot = specialBlocksMenu.getSlot(i);

            switch (i) {
                // First Row
                // Seamless Sandstone
                case 0:
                    slot.setItem(SeamlessSandstone);

                    slot.setClickHandler((player, clickInformation) -> {
                        giveItemToPlayer(player, SeamlessSandstone);
                    });
                    break;
                // Seamless Stone
                case 1:
                    slot.setItem(SeamlessStone);

                    slot.setClickHandler((player, clickInformation) -> {
                        giveItemToPlayer(player, SeamlessStone);
                    });
                    break;
                // Red Mushroom
                case 2:
                    slot.setItem(RedMushroom);

                    slot.setClickHandler((player, clickInformation) -> {
                        giveItemToPlayer(player, RedMushroom);
                    });
                    break;
                // Mushroom Stem
                case 3:
                    slot.setItem(MushroomStem);

                    slot.setClickHandler((player, clickInformation) -> {
                        giveItemToPlayer(player, MushroomStem);
                    });
                    break;
                // Brown Mushroom
                case 4:
                    slot.setItem(BrownMushroom);

                    slot.setClickHandler((player, clickInformation) -> {
                        giveItemToPlayer(player, BrownMushroom);
                    });
                    break;
                // Light Brown Mushroom
                case 5:
                    slot.setItem(LightBrownMushroom);

                    slot.setClickHandler((player, clickInformation) -> {
                        giveItemToPlayer(player, LightBrownMushroom);
                    });
                    break;
                // Barrier
                case 6:
                    slot.setItem(Barrier);

                    slot.setClickHandler((player, clickInformation) -> {
                        giveItemToPlayer(player, Barrier);
                    });
                    break;

                // Second Row
                // Bark Oak Log
                case 9:
                    slot.setItem(BarkOakLog);

                    slot.setClickHandler((player, clickInformation) -> {
                        giveItemToPlayer(player, BarkOakLog);
                    });
                    break;
                // Bark Spruce Log
                case 10:
                    slot.setItem(BarkSpruceLog);

                    slot.setClickHandler((player, clickInformation) -> {
                        giveItemToPlayer(player, BarkSpruceLog);
                    });
                    break;
                // Bark Birch Log
                case 11:
                    slot.setItem(BarkBirchLog);

                    slot.setClickHandler((player, clickInformation) -> {
                        giveItemToPlayer(player, BarkBirchLog);
                    });
                    break;
                // Bark Jungle Log
                case 12:
                    slot.setItem(BarkJungleLog);

                    slot.setClickHandler((player, clickInformation) -> {
                        giveItemToPlayer(player, BarkJungleLog);
                    });
                    break;
                // Bark Acacia Log
                case 13:
                    slot.setItem(BarkAcaciaLog);

                    slot.setClickHandler((player, clickInformation) -> {
                        giveItemToPlayer(player, BarkAcaciaLog);
                    });
                    break;
                // Bark Dark Oak Log
                case 14:
                    slot.setItem(BarkDarkOakLog);

                    slot.setClickHandler((player, clickInformation) -> {
                        giveItemToPlayer(player, BarkDarkOakLog);
                    });
                    break;
            }
        }

        return specialBlocksMenu;
    }

    private void giveItemToPlayer(Player player, ItemStack item) {
        if(!player.getInventory().contains(item)) {
            player.getInventory().addItem(item);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 5.0f, 1.0f);
        }
    }

    public static ItemStack getItem() {
        return new ItemBuilder(Material.GOLD_BLOCK ,1)
                .setName("§b§lSPECIAL BLOCKS")
                .setLore(new LoreBuilder().description("Open the special blocks menu to get a variety of inaccessible blocks.").build())
                .build();
    }
}
