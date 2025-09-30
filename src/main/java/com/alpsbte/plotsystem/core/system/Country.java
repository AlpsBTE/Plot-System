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