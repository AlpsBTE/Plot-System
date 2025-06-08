/*
 * The MIT License (MIT)
 *
 *  Copyright © 2025, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.utils.io;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.utils.Utils.ChatUtils;
import li.cinnazeyy.langlibs.core.LangLibAPI;
import li.cinnazeyy.langlibs.core.language.Language;
import li.cinnazeyy.langlibs.core.file.LanguageFile;
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
                new LanguageFile(plugin, 1.0, Language.he_IL),
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
