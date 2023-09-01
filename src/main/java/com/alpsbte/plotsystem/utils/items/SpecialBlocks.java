/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.alpsbte.plotsystem.utils.items;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SpecialBlocks {
    public static ItemStack SeamlessSandstone = new ItemBuilder(Material.SANDSTONE, 1, (byte) 2)
            .setName("§6§lSeamless Sandstone").setLore(new LoreBuilder()
                .emptyLine()
                .addLines("Use this tool to place a §fSeamless Sandstone§7 block",
                          "",
                          "§7ID: §b43:9")
                .emptyLine()
                .build())
            .setEnchanted(true)
            .build();

    public static ItemStack SeamlessRedSandstone = new ItemBuilder(Material.RED_SANDSTONE, 1, (byte) 2)
            .setName("§6§lSeamless Red Sandstone").setLore(new LoreBuilder()
                    .emptyLine()
                    .addLines("Use this tool to place a §fSeamless Red Sandstone§7 block",
                            "",
                            "§7ID: §b181:12")
                    .emptyLine()
                    .build())
            .setEnchanted(true)
            .build();

    public static ItemStack SeamlessStone = new ItemBuilder(Material.STONE, 1)
            .setName("§6§lSeamless Stone").setLore(new LoreBuilder()
                .emptyLine()
                .addLines("Use this tool to place a §fSeamless Stone§7 block",
                          "",
                          "§7ID: §b43:8")
                .emptyLine()
                .build())
            .setEnchanted(true)
            .build();

    public static ItemStack RedMushroom = new ItemBuilder(Material.LEGACY_HUGE_MUSHROOM_2, 1)
            .setName("§6§lRed Mushroom").setLore(new LoreBuilder()
                .emptyLine()
                .addLines("Use this tool to place a §fRed Mushroom§7 block",
                        "",
                        "§7ID: §b100")
                .emptyLine()
            .build())
            .setEnchanted(true)
            .build();

    public static ItemStack BrownMushroom = new ItemBuilder(Material.LEGACY_HUGE_MUSHROOM_1, 1)
            .setName("§6§lBrown Mushroom Block").setLore(new LoreBuilder()
                .emptyLine()
                .addLines("Use this tool to place a §fBrown Mushroom§7 block",
                        "",
                        "§7ID: §b99:14")
                .emptyLine()
            .build())
            .setEnchanted(true)
            .build();

    public static ItemStack SeamlessMushroomStem = new ItemBuilder(Material.LEGACY_HUGE_MUSHROOM_2, 1)
            .setName("§6§lSeamless Mushroom Stem Block").setLore(new LoreBuilder()
                .emptyLine()
                .addLines("Use this tool to place a §fSeamless Mushroom Stem§7 block",
                          "",
                          "§7ID: §b99:15")
                .emptyLine()
            .build())
            .setEnchanted(true)
            .build();

    public static ItemStack LightBrownMushroom = new ItemBuilder(Material.LEGACY_HUGE_MUSHROOM_1, 1)
            .setName("§6§lLight Brown Mushroom Block").setLore(new LoreBuilder()
                .emptyLine()
                .addLines("Use this tool to place a §fLight Brown Mushroom§7 block",
                          "",
                          "§7ID: §b99:11")
                .emptyLine()
            .build())
            .setEnchanted(true)
            .build();

    public static ItemStack Barrier = new ItemBuilder(Material.BARRIER, 1)
            .setName("§6§lBarrier").setLore(new LoreBuilder()
                .emptyLine()
                .addLines("Use this tool to place an §fInvisible§7 block",
                        "",
                        "§7ID: §b166")
                .emptyLine()
            .build())
            .setEnchanted(true)
            .build();

    public static ItemStack StructureVoid = new ItemBuilder(Material.STRUCTURE_VOID, 1)
            .setName("§6§lStructure Void").setLore(new LoreBuilder()
                    .emptyLine()
                    .addLines("Use this tool to place an §fInvisible§7 block.",
                            "",
                            "§7ID: §b217")
                    .emptyLine()
                    .build())
            .setEnchanted(true)
            .build();

    public static ItemStack BarkOakLog = new ItemBuilder(Material.OAK_WOOD)
            .setName("§6§lBark Oak Log").setLore(new LoreBuilder()
                .emptyLine()
                .addLines("Use this tool to place a §f6-Sided Bark Oak Log§7 block",
                          "",
                          "§7ID: §b17:12")
                .emptyLine()
            .build())
            .setEnchanted(true)
            .build();

    public static ItemStack BarkBirchLog = new ItemBuilder(Material.BIRCH_WOOD)
            .setName("§6§lBark Birch Log").setLore(new LoreBuilder()
                .emptyLine()
                .addLines("Use this tool to place a §f6-Sided Bark Birch Log§7 block",
                          "",
                          "§7ID: §b17:14")
                .emptyLine()
            .build())
            .setEnchanted(true)
            .build();

    public static ItemStack BarkSpruceLog = new ItemBuilder(Material.SPRUCE_WOOD)
            .setName("§6§lBark Spruce Log").setLore(new LoreBuilder()
                .emptyLine()
                .addLines("Use this tool to place a §f6-Sided Bark Spruce Log§7 block",
                          "",
                          "§7ID: §b17:13")
                .emptyLine()
            .build())
            .setEnchanted(true)
            .build();

    public static ItemStack BarkJungleLog = new ItemBuilder(Material.JUNGLE_WOOD)
            .setName("§6§lBark Jungle Log").setLore(new LoreBuilder()
                .emptyLine()
                .addLines("Use this tool to place a §f6-Sided Bark Jungle Log§7 block",
                          "",
                          "§7ID: §b17:15")
                .emptyLine()
            .build())
            .setEnchanted(true)
            .build();

    public static ItemStack BarkAcaciaLog = new ItemBuilder(Material.ACACIA_WOOD)
            .setName("§6§lBark Acacia Log").setLore(new LoreBuilder()
                .emptyLine()
                .addLines("Use this tool to place a §f6-Sided Bark Acacia Log§7 block.",
                          "",
                          "§7ID: §b162:12")
                .emptyLine()
            .build())
            .setEnchanted(true)
            .build();

    public static ItemStack BarkDarkOakLog = new ItemBuilder(Material.DARK_OAK_WOOD)
            .setName("§6§lBark Dark Oak Log").setLore(new LoreBuilder()
                .emptyLine()
                .addLines("Use this tool to place a §f6-Sided Bark Dark Oak Log§7 block.",
                          "",
                          "§7ID: §b162:13")
                .emptyLine()
            .build())
            .setEnchanted(true)
            .build();
}
