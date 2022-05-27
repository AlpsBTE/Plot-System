package com.alpsbte.plotsystem.utils.enums;

import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.utils.io.language.LangPaths;
import com.alpsbte.plotsystem.utils.io.language.LangUtil;
import com.alpsbte.plotsystem.utils.items.builder.ItemBuilder;
import com.alpsbte.plotsystem.utils.items.builder.LoreBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public enum Continent {
    EUROPE("europe", LangPaths.Continent.EUROPE),
    ASIA("asia", LangPaths.Continent.ASIA),
    AFRICA("africa", LangPaths.Continent.AFRICA),
    OCEANIA("oceania", LangPaths.Continent.OCEANIA),
    SOUTH_AMERICA("south america", LangPaths.Continent.SOUTH_AMERICA),
    NORTH_AMERICA("north america", LangPaths.Continent.NORTH_AMERICA);

    public final String databaseEnum;
    public final String langPath;

    Continent(String databaseEnum, String langPath) {
        this.databaseEnum = databaseEnum;
        // although LangPath.Continent keys match the enum name, you cannot get the value without reflection
        this.langPath = langPath;
    }

    public static Continent fromDatabase(String databaseEnum) {
        return Arrays.stream(Continent.values()).filter(c -> c.databaseEnum.equals(databaseEnum)).findFirst().orElse(null);
    }

    /**
     * Get menu item for this continent
     *
     * @param player Used to translate the menu item
     * @return Menu item
     */
    public ItemStack getItem(Player player) {
        List<Country> countries = Country.getCountries(this);

        return new ItemBuilder(Material.COMPASS)
                .setName("§e§l" + LangUtil.get(player, langPath))
                .setLore(new LoreBuilder().addLines("§6" + countries.size() + " §7" + LangUtil.get(player, LangPaths.Country.COUNTRIES)).build())
                .build();
    }
}
