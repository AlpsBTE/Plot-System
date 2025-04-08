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

package com.alpsbte.plotsystem.core.holograms.leaderboards;

import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.LangPaths;

public enum LeaderboardTimeframe {
    DAILY(ConfigPaths.DISPLAY_OPTIONS_SHOW_DAILY),
    WEEKLY(ConfigPaths.DISPLAY_OPTIONS_SHOW_WEEKLY),
    MONTHLY(ConfigPaths.DISPLAY_OPTIONS_SHOW_MONTHLY),
    YEARLY(ConfigPaths.DISPLAY_OPTIONS_SHOW_YEARLY),
    LIFETIME(ConfigPaths.DISPLAY_OPTIONS_SHOW_LIFETIME);

    public final String configPath;
    public final String langPath;

    LeaderboardTimeframe(String configPath) {
        this.configPath = configPath;
        this.langPath = LangPaths.Leaderboards.PAGES + name();
    }
}
