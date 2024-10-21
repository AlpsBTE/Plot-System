package com.alpsbte.plotsystem.utils.items;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum BaseItems {
    COMPANION_ITEM("companion-item");

    final ItemStack itemStack;

    BaseItems(String configPath) {
        String materialString = ConfigUtil.getInstance().configs[2].getString(configPath + ".material");
        Material material = Material.getMaterial(materialString == null ? "" : materialString, false);
        material = material == null ? Material.BARRIER : material;

        int modelId = ConfigUtil.getInstance().configs[2].getInt(configPath + ".modelId");

        itemStack = new ItemBuilder(material).setItemModel(modelId).build();
    }

    public ItemStack getItem() {
        return itemStack;
    }
}
