package com.alpsbte.plotsystem.utils.enums;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

import static net.kyori.adventure.text.Component.text;

public enum Continent {
    EUROPE("EU", LangPaths.Continent.EUROPE),
    ASIA("AS", LangPaths.Continent.ASIA),
    AFRICA("AF", LangPaths.Continent.AFRICA),
    OCEANIA("OC", LangPaths.Continent.OCEANIA),
    SOUTH_AMERICA("SA", LangPaths.Continent.SOUTH_AMERICA),
    NORTH_AMERICA("NA", LangPaths.Continent.NORTH_AMERICA);

    public final String databaseEnum;
    public final String langPath;

    Continent(String databaseEnum, String langPath) {
        this.databaseEnum = databaseEnum;
        // you cannot get the value from the database enum without reflection
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
        List<Country> countries = DataProvider.COUNTRY.getCountriesByContinent(this);

        return new ItemBuilder(Material.COMPASS)
                .setName(text(LangUtil.getInstance().get(player, langPath), NamedTextColor.YELLOW).decoration(TextDecoration.BOLD, true))
                .setLore(new LoreBuilder().addLines(text(countries.size() + " ", NamedTextColor.GOLD).append(text(LangUtil.getInstance().get(player, LangPaths.Country.COUNTRIES), NamedTextColor.GRAY))).build())
                .build();
    }
}
