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

package com.alpsbte.plotsystem.utils.enums;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
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

import static net.kyori.adventure.text.Component.*;

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
                .setName(text(LangUtil.getInstance().get(player, langPath), NamedTextColor.YELLOW).decoration(TextDecoration.BOLD, true))
                .setLore(new LoreBuilder().addLines(text(countries.size() + " ", NamedTextColor.GOLD).append(text(LangUtil.getInstance().get(player, LangPaths.Country.COUNTRIES), NamedTextColor.GRAY))).build())
                .build();
    }
}
