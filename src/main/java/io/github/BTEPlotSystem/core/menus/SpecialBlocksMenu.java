package github.BTEPlotSystem.core.menus;

import github.BTEPlotSystem.utils.SpecialBlocks;
import github.BTEPlotSystem.utils.ItemBuilder;
import github.BTEPlotSystem.utils.LoreBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

public class SpecialBlocksMenu extends AbstractMenu {
    public SpecialBlocksMenu(Player player) {
        super(3, "Special Blocks", player);

        Mask mask = BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("000000000")
                .pattern("000000000")
                .pattern("111101111")
                .build();
        mask.apply(getMenu());

        addMenuItems();
        setItemClickEvents();

        getMenu().open(getMenuPlayer());
    }

    @Override
    protected void addMenuItems() {
        // Add Special Blocks
        for(int i = 0; i < 13; i++) {
            getMenu().getSlot(i).setItem(getSpecialBlock(i));
        }

        // Add Back Button Item
        getMenu().getSlot(22).setItem(backMenuItem());
    }

    @Override
    protected void setItemClickEvents() {
        // Add Click Event For Special Blocks
        for(int i = 0; i < 13; i++) {
            int specialBlockID = i;
            getMenu().getSlot(i).setClickHandler((clickPlayer, clickInformation) -> {
                if(!clickPlayer.getInventory().contains(getSpecialBlock(specialBlockID))) {
                    clickPlayer.getInventory().addItem(getSpecialBlock(specialBlockID));
                    clickPlayer.playSound(clickPlayer.getLocation(), Sound.ENTITY_ITEM_PICKUP, 5.0f, 1.0f);
                }
            });
        }

        // Add Click Event For Back Button
        getMenu().getSlot(22).setClickHandler((clickPlayer, clickInformation) -> new BuilderUtilitiesMenu(clickPlayer));
    }

    public static ItemStack getMenuItem() {
        return new ItemBuilder(Material.GOLD_BLOCK ,1)
                .setName("§b§lSPECIAL BLOCKS")
                .setLore(new LoreBuilder()
                        .addLine("Open the special blocks menu to get a variety of inaccessible blocks.").build())
                .build();
    }

    private ItemStack getSpecialBlock(int ID) {
        switch (ID) {
            // First Row
            // Seamless Sandstone
            case 0:
                return SpecialBlocks.SeamlessSandstone;
            // Seamless Stone
            case 1:
                return SpecialBlocks.SeamlessStone;
            // Red Mushroom
            case 2:
                return SpecialBlocks.RedMushroom;
            // Mushroom Stem
            case 3:
                return SpecialBlocks.MushroomStem;
            // Brown Mushroom
            case 4:
                return SpecialBlocks.BrownMushroom;
            // Light Brown Mushroom
            case 5:
                return SpecialBlocks.LightBrownMushroom;
            // Barrier
            case 6:
                return SpecialBlocks.Barrier;

            // Second Row
            // Bark Oak Log
            case 9:
                return SpecialBlocks.BarkOakLog;
            // Bark Spruce Log
            case 10:
               return SpecialBlocks.BarkSpruceLog;
            // Bark Birch Log
            case 11:
                return SpecialBlocks.BarkBirchLog;
            // Bark Jungle Log
            case 12:
                return SpecialBlocks.BarkJungleLog;
            // Bark Acacia Log
            case 13:
                return SpecialBlocks.BarkAcaciaLog;
            // Bark Dark Oak Log
            case 14:
                return SpecialBlocks.BarkDarkOakLog;
            default:
                return errorItem();
        }
    }
}
