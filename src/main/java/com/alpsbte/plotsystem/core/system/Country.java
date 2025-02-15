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

package com.alpsbte.plotsystem.core.system;

import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Continent;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Country {

    private final String code;

    private String material;
    private String customModelData;

    private final Continent continent;

    public Country(String code, Continent continent, String material, String customModelData) {
        this.code = code;
        this.continent = continent;
        this.material = material;
        this.customModelData = customModelData;
    }

    public String getCode() {return code;}

    public Continent getContinent() {return continent;}

    public List<CityProject> getCityProjects() {
        return DataProvider.CITY_PROJECT.getByCountryCode(code, true);
    }

    public String getName(Player player) {
        return LangUtil.getInstance().get(player, LangPaths.Database.COUNTRY + "." + code + ".name");
    }

    public boolean setMaterialAndModelData(String material, @Nullable String customModelData) {
        if (DataProvider.COUNTRY.setMaterialAndCustomModelData(code, material, customModelData)) {
            this.material = material;
            this.customModelData = customModelData;
            return true;
        }
        return false;
    }

    public ItemStack getCountryItem() {
        return Utils.getConfiguredItem(material, customModelData);
    }
}