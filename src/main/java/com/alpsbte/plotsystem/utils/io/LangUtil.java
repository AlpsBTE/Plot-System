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
import li.cinnazeyy.langlibs.core.Language;
import li.cinnazeyy.langlibs.core.file.LanguageFile;
import li.cinnazeyy.langlibs.core.language.LangLibAPI;
import li.cinnazeyy.langlibs.core.language.LanguageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LangUtil extends LanguageUtil {
    private static LangUtil langUtilInstance;

    public static void init() {
        if (langUtilInstance != null) return;
        LangLibAPI.register(PlotSystem.getPlugin(), new LanguageFile[]{
                new LanguageFile(Language.en_GB, 2.2),
                new LanguageFile(Language.de_DE, 2.2, "de_AT", "de_CH"),
                new LanguageFile(Language.fr_FR, 2.3, "fr_CA"),
                new LanguageFile(Language.pt_PT, 2.2, "pt_BR"),
                new LanguageFile(Language.ko_KR, 2.2),
                new LanguageFile(Language.ru_RU, 2.2, "ba_RU", "tt_RU"),
                new LanguageFile(Language.zh_CN, 2.2),
                new LanguageFile(Language.zh_TW, 2.3, "zh_HK"),
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
