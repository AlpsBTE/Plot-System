package github.BTEPlotSystem.core;

import github.BTEPlotSystem.utils.ItemBuilder;
import github.BTEPlotSystem.utils.LoreBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class SpecialBlocks {
    public ItemStack SeamlessSandstone = new ItemBuilder(Material.SANDSTONE, 1, (byte) 2).setName("§6§lSeamless Sandstone").setLore(new LoreBuilder()
            .emptyLine()
            .description("§7", "Use this tool to place a §fSeamless Sandstone§7 block.")
            .emptyLine()
            .build())
            .setEnchantment(Enchantment.ARROW_DAMAGE)
            .setItemFlag(ItemFlag.HIDE_ENCHANTS)
            .build();

    public ItemStack SeamlessStone = new ItemBuilder(Material.STONE, 1).setName("§6§lSeamless Stone").setLore(new LoreBuilder()
            .emptyLine()
            .description("§7", "Use this tool to place a §fSeamless Stone§7 block.")
            .emptyLine()
            .build())
            .setEnchantment(Enchantment.ARROW_DAMAGE)
            .setItemFlag(ItemFlag.HIDE_ENCHANTS)
            .build();

    public ItemStack RedMushroom = new ItemBuilder(Material.HUGE_MUSHROOM_2, 1).setName("§6§lRed Mushroom").setLore(new LoreBuilder()
            .emptyLine()
            .description("§7", "Use this tool to place a §fRed Mushroom§7 block.")
            .emptyLine()
            .build())
            .setEnchantment(Enchantment.ARROW_DAMAGE)
            .setItemFlag(ItemFlag.HIDE_ENCHANTS)
            .build();

    public ItemStack BrownMushroom = new ItemBuilder(Material.HUGE_MUSHROOM_1, 1).setName("§6§lBrown Mushroom Block").setLore(new LoreBuilder()
            .emptyLine()
            .description("§7", "Use this tool to place a §fBrown Mushroom§7 block.")
            .emptyLine()
            .build())
            .setEnchantment(Enchantment.ARROW_DAMAGE)
            .setItemFlag(ItemFlag.HIDE_ENCHANTS)
            .build();

    public ItemStack MushroomStem = new ItemBuilder(Material.HUGE_MUSHROOM_2, 1).setName("§6§lMushroom Stem Block").setLore(new LoreBuilder()
            .emptyLine()
            .description("§7", "Use this tool to place a §fMushroom Stem§7 block.")
            .emptyLine()
            .build())
            .setEnchantment(Enchantment.ARROW_DAMAGE)
            .setItemFlag(ItemFlag.HIDE_ENCHANTS)
            .build();

    public ItemStack LightBrownMushroom = new ItemBuilder(Material.HUGE_MUSHROOM_1, 1).setName("§6§lLight Brown Mushroom Block").setLore(new LoreBuilder()
            .emptyLine()
            .description("§7", "Use this tool to place a §fLight Brown Mushroom§7 block.")
            .emptyLine()
            .build())
            .setEnchantment(Enchantment.ARROW_DAMAGE)
            .setItemFlag(ItemFlag.HIDE_ENCHANTS)
            .build();

    public ItemStack Barrier = new ItemBuilder(Material.BARRIER, 1).setName("§6§lBarrier").setLore(new LoreBuilder()
            .emptyLine()
            .description("§7", "Use this tool to place an §fInvisible§7 block.")
            .emptyLine()
            .build())
            .setEnchantment(Enchantment.ARROW_DAMAGE)
            .setItemFlag(ItemFlag.HIDE_ENCHANTS)
            .build();

    public ItemStack BarkOakLog = new ItemBuilder(Material.LOG, 1, (byte) 0).setName("§6§lBark Oak Log").setLore(new LoreBuilder()
            .emptyLine()
            .description("§7", "Use this tool to place a §f6-Sided Bark Oak Log§7 block.")
            .emptyLine()
            .build())
            .setEnchantment(Enchantment.ARROW_DAMAGE)
            .setItemFlag(ItemFlag.HIDE_ENCHANTS)
            .build();

    public ItemStack BarkBirchLog = new ItemBuilder(Material.LOG, 1, (byte) 2).setName("§6§lBark Birch Log").setLore(new LoreBuilder()
            .emptyLine()
            .description("§7", "Use this tool to place a §f6-Sided Bark Birch Log§7 block.")
            .emptyLine()
            .build())
            .setEnchantment(Enchantment.ARROW_DAMAGE)
            .setItemFlag(ItemFlag.HIDE_ENCHANTS)
            .build();

    public ItemStack BarkSpruceLog = new ItemBuilder(Material.LOG, 1, (byte) 1).setName("§6§lBark Spruce Log").setLore(new LoreBuilder()
            .emptyLine()
            .description("§7", "Use this tool to place a §f6-Sided Bark Spruce Log§7 block.")
            .emptyLine()
            .build())
            .setEnchantment(Enchantment.ARROW_DAMAGE)
            .setItemFlag(ItemFlag.HIDE_ENCHANTS)
            .build();

    public ItemStack BarkJungleLog = new ItemBuilder(Material.LOG, 1, (byte) 3).setName("§6§lBark Jungle Log").setLore(new LoreBuilder()
            .emptyLine()
            .description("§7", "Use this tool to place a §f6-Sided Bark Jungle Log§7 block.")
            .emptyLine()
            .build())
            .setEnchantment(Enchantment.ARROW_DAMAGE)
            .setItemFlag(ItemFlag.HIDE_ENCHANTS)
            .build();

    public ItemStack BarkAcaciaLog = new ItemBuilder(Material.LOG_2, 1, (byte) 0).setName("§6§lBark Acacia Log").setLore(new LoreBuilder()
            .emptyLine()
            .description("§7", "Use this tool to place a §f6-Sided Bark Acacia Log§7 block.")
            .emptyLine()
            .build())
            .setEnchantment(Enchantment.ARROW_DAMAGE)
            .setItemFlag(ItemFlag.HIDE_ENCHANTS)
            .build();

    public ItemStack BarkDarkOakLog = new ItemBuilder(Material.LOG_2, 1, (byte) 1).setName("§6§lBark Dark Oak Log").setLore(new LoreBuilder()
            .emptyLine()
            .description("§7", "Use this tool to place a §f6-Sided Bark Dark Oak Log§7 block.")
            .emptyLine()
            .build())
            .setEnchantment(Enchantment.ARROW_DAMAGE)
            .setItemFlag(ItemFlag.HIDE_ENCHANTS)
            .build();
}
