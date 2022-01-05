package com.alpsbte.plotsystem.core.language;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.config.Config;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Language {
    public static final List<Language> languageList = Arrays.asList(
            new Language("en_GB")
    );

    private final String name;
    private final String tag;
    private final String headID;

    public Language(String tag) {
        this.tag = tag;
        this.name = getFile().getString("lang.name");
        this.headID = getFile().getString("lang.head-id");
    }

    public String getName() {
        return name;
    }

    public ItemStack getHead() {
        return Utils.getItemHead(new Utils.CustomHead(headID));
    }

    public String getTag() { return tag; }

    public String get(String key) {
        return getFile().getString(key);
    }

    public String get(String key, HashMap<String, String> params) {
        String translation = getFile().getString(key);
        for (String paramKey : params.keySet()) {
            translation = translation.replace("{" + paramKey + "}", params.get(paramKey));
        }
        return translation;
    }

    private Config getFile() {
        return PlotSystem.getPlugin().getConfigManager().getLanguageConfigByTag(tag);
    }
}
