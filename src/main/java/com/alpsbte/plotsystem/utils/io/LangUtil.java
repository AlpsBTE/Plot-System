package com.alpsbte.plotsystem.utils.io;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.utils.Utils.ChatUtils;
import li.cinnazeyy.langlibs.core.LangLibAPI;
import li.cinnazeyy.langlibs.core.file.LanguageFile;
import li.cinnazeyy.langlibs.core.language.Language;
import li.cinnazeyy.langlibs.core.language.LanguageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class LangUtil extends LanguageUtil {
    private static LangUtil langUtilInstance;

    public static void init() {
        if (langUtilInstance != null) return;
        Plugin plugin = PlotSystem.getPlugin();
        LangLibAPI.register(plugin, new LanguageFile[]{
                new LanguageFile(plugin, 2.5, Language.en_GB),
                new LanguageFile(plugin, 2.5, Language.de_DE, "de_AT", "de_CH"),
                new LanguageFile(plugin, 2.5, Language.fr_FR, "fr_CA"),
                new LanguageFile(plugin, 2.5, Language.pt_PT, "pt_BR"),
                new LanguageFile(plugin, 2.5, Language.ko_KR),
                new LanguageFile(plugin, 2.5, Language.ru_RU, "ba_RU", "tt_RU"),
                new LanguageFile(plugin, 2.5, Language.zh_CN),
                new LanguageFile(plugin, 2.5, Language.zh_TW, "zh_HK"),
                new LanguageFile(plugin, 1.1, Language.he_IL),
                new LanguageFile(plugin, 1.0, Language.es_ES),
                new LanguageFile(plugin, 1.0, Language.hu_HU),
                new LanguageFile(plugin, 1.0, Language.nl_NL),
                new LanguageFile(plugin, 1.0, Language.ro_RO),
                new LanguageFile(plugin, 1.0, Language.da_DK),
        });
        langUtilInstance = new LangUtil();
    }

    public LangUtil() {
        super(PlotSystem.getPlugin());
    }

    public void broadcast(String key, String... args) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(ChatUtils.getInfoFormat(get(player, key, args)));
        }
    }

    public static LangUtil getInstance() {
        return langUtilInstance;
    }
}
