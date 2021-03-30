package github.BTEPlotSystem.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta itemMeta;

    public ItemBuilder(ItemStack item) {
        itemMeta = item.getItemMeta();
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        this.item = item;
    }

    public ItemBuilder(Material material, int amount, byte color) {
        item = new ItemStack(material, amount, color);
        itemMeta = item.getItemMeta();
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    }

    public ItemBuilder(Material material) {
        this(material, 1, (byte) 0);
    }

    public ItemBuilder(Material material, int amount) {
        this(material, amount, (byte) 0);
    }

    public ItemBuilder setName(String name) {
        itemMeta.setDisplayName(name);
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        itemMeta.setLore(lore);
        return this;
    }

    public ItemBuilder setEnchantment(boolean setEnchanted) {
        if(setEnchanted) {
            itemMeta.addEnchant(Enchantment.ARROW_DAMAGE,1,true);
        } else {
            itemMeta.removeEnchant(Enchantment.ARROW_DAMAGE);
        }
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(itemMeta);
        return item;
    }
}
