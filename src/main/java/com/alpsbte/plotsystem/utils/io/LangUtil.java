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

package com.alpsbte.plotsystem.utils.io;

import com.alpsbte.alpslib.io.lang.LanguageUtil;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.utils.Utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LangUtil extends LanguageUtil {
    private static LangUtil langUtilInstance;

    public static void init() {
        if (langUtilInstance != null) return;
        langUtilInstance = new LangUtil(new LanguageFile[]{
                new LanguageFile("en_GB", 1.3),
                new LanguageFile("de_DE", 1.3, "de_AT", "de_CH"),
                new LanguageFile("fr_FR", 1.3, "fr_CA"),
                new LanguageFile("pt_PT", 1.2, "pt_BR"),
                new LanguageFile("ko_KR", 1.3),
                new LanguageFile("ru_RU", 1.3, "ba_RU", "tt_RU"),
                new LanguageFile("zh_CN", 1.3),
                new LanguageFile("zh_TW", 1.3, "zh_HK"),
        });
    }


    public LangUtil(LanguageFile[] langFiles) {
        super(langFiles);
    }

    @Override
    public String getLocaleTagByPlayer(Player player) {
        Builder builder = Builder.byUUID(player.getUniqueId());
        if (builder.getLanguageTag() != null) {
            return builder.getLanguageTag();
        } else return player.getPlayer().getLocale();
    }

    public void broadcast(String key, String... args) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(ChatUtils.getInfoMessageFormat(get(player, key, args)));
        }
    }

    public static LangUtil getInstance() {
        return langUtilInstance;
    }
}
