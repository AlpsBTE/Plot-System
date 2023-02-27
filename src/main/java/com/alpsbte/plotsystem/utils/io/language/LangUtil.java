package com.alpsbte.plotsystem.utils.io.language;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.YamlFile;
import com.alpsbte.plotsystem.utils.io.YamlFileFactory;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;

public class LangUtil extends YamlFileFactory {

    public final static LanguageFile[] languages = new LanguageFile[] {
        new LanguageFile("en_GB", 1.2),
        new LanguageFile("de_DE", 1.2, "de_AT", "de_CH"),
        new LanguageFile("fr_FR", 1.2, "fr_CA"),
        new LanguageFile("ko_KR", 1.2),
        new LanguageFile("ru_RU", 1.2, "ba_RU", "tt_RU"),
        new LanguageFile("zh_CN", 1.2),
        new LanguageFile("zh_TW", 1.2, "zh_HK"),
    };

    public LangUtil() {
        super(languages);

        Arrays.stream(languages).forEach(lang -> {
            if (!lang.getFile().exists()) {
                createFile(lang);
            } else if (reloadFile(lang) && lang.getDouble(LangPaths.CONFIG_VERSION) != lang.getVersion()) {
                updateFile(lang);
            }
            reloadFile(lang);
        });
    }

    public static String get(CommandSender sender, String key) {
        return getLanguageFileByLocale(sender instanceof Player ? getLocaleTagByPlayer((Player) sender) : languages[0].tag).getTranslation(key);
    }

    public static String get(CommandSender sender, String key, String... args) {
        return getLanguageFileByLocale(sender instanceof Player ? getLocaleTagByPlayer((Player) sender) : languages[0].tag).getTranslation(key, args);
    }

    public static LanguageFile getLanguageFileByLocale(String locale) {
        return Arrays.stream(languages)
                .filter(lang -> lang.tag.equalsIgnoreCase(locale))
                .findFirst()
                .orElseGet(() -> Arrays.stream(languages)
                        .filter(lang -> lang.additionalLang != null && Arrays.stream(lang.additionalLang).anyMatch(l -> l.equalsIgnoreCase(locale)))
                        .findFirst()
                        .orElse(languages[0]));
    }

    private static String getLocaleTagByPlayer(Player player) {
        Builder builder = Builder.byUUID(player.getUniqueId());
        if (builder.getLanguageTag() != null) {
            return builder.getLanguageTag();
        } else return player.getPlayer().getLocale();
    }

    public static void broadcast(String key, String... args) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(Utils.getInfoMessageFormat(get(player, key, args)));
        }
    }

    public static class LanguageFile extends YamlFile {
        private final String tag;
        private String[] additionalLang;

        public LanguageFile(String lang, double version) {
            super(Paths.get("lang", lang + ".yml"), version);

            this.tag = lang;
        }

        public LanguageFile(String lang, double version, String... additionalLang) {
            this(lang, version);
            this.additionalLang = additionalLang;
        }

        public String getTranslation(String key) {
            String translation = getString(key);
            return translation != null ? translation : "undefined";
        }

        public String getTranslation(String key, String... args) {
            String translation = getTranslation(key);
            for (int i = 0; i < args.length; i++) {
                translation = translation.replace("{" + i + "}", args[i]);
            }
            return translation;
        }

        public String getTag() {
            return tag;
        }

        public String getLangName() {
            return getString(LangPaths.LANG_NAME);
        }

        public ItemStack getHead() {
            return Utils.getItemHead(new Utils.CustomHead(getString(LangPaths.LANG_HEAD_ID)));
        }

        @Override
        public InputStream getDefaultFileStream() {
            return PlotSystem.getPlugin().getResource("lang/" + getFile().getName());
        }

        @Override
        public int getMaxConfigWidth() {
            return 400;
        }
    }
}
