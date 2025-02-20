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

public abstract class ConfigPaths {
    private ConfigPaths() {throw new IllegalStateException("Utility class");}

    // General Behaviour
    public static final String SPAWN_WORLD = "spawn-world";
    public static final String CHECK_FOR_UPDATES = "check-for-updates";
    public static final String ENABLE_SCORE_REQUIREMENT = "enable-score-requirement";
    public static final String DEV_MODE = "dev-mode";
    public static final String INACTIVITY_INTERVAL = "inactivity-interval";
    public static final String REJECTED_INACTIVITY_INTERVAL = "rejected-inactivity-interval";
    public static final String ENABLE_GROUP_SUPPORT = "enable-group-support";
    public static final String UNFINISHED_REMINDER_INTERVAL = "unfinished-reminder-interval";

    // Database
    private static final String DATABASE = "database.";
    public static final String DATABASE_URL = DATABASE + "url";
    public static final String DATABASE_NAME = DATABASE + "dbname";
    public static final String DATABASE_USERNAME = DATABASE + "username";
    public static final String DATABASE_PASSWORD = DATABASE + "password";

    // Leaderboards
    private static final String HOLOGRAMS = "holograms.";
    public static final String SCORE_LEADERBOARD = "score-leaderboard";
    public static final String SCORE_LEADERBOARD_ENABLE = HOLOGRAMS + SCORE_LEADERBOARD + ".sl-enable";
    public static final String SCORE_LEADERBOARD_X = HOLOGRAMS + SCORE_LEADERBOARD + ".sl-x";
    public static final String SCORE_LEADERBOARD_Y = HOLOGRAMS + SCORE_LEADERBOARD + ".sl-y";
    public static final String SCORE_LEADERBOARD_Z = HOLOGRAMS + SCORE_LEADERBOARD + ".sl-z";

    private static final String DISPLAY_OPTIONS = "display-options.";
    public static final String DISPLAY_OPTIONS_INTERVAL = DISPLAY_OPTIONS + "interval";
    public static final String DISPLAY_OPTIONS_SHOW_DAILY = DISPLAY_OPTIONS + "show-daily";
    public static final String DISPLAY_OPTIONS_SHOW_WEEKLY = DISPLAY_OPTIONS + "show-weekly";
    public static final String DISPLAY_OPTIONS_SHOW_MONTHLY = DISPLAY_OPTIONS + "show-monthly";
    public static final String DISPLAY_OPTIONS_SHOW_YEARLY = DISPLAY_OPTIONS + "show-yearly";
    public static final String DISPLAY_OPTIONS_SHOW_LIFETIME = DISPLAY_OPTIONS + "show-lifetime";
    public static final String DISPLAY_OPTIONS_ACTION_BAR_ENABLE = DISPLAY_OPTIONS + "action-bar-enable";
    public static final String DISPLAY_OPTIONS_ACTION_BAR_RADIUS = DISPLAY_OPTIONS + "action-bar-radius";

    // FORMATTING
    private static final String CHAT_FORMAT = "chat-format.";
    public static final String CHAT_FORMAT_INFO_PREFIX = CHAT_FORMAT + "info-prefix";
    public static final String CHAT_FORMAT_ALERT_PREFIX = CHAT_FORMAT + "alert-prefix";

    // COMMANDS
    public static final String EDITPLOT_ENABLED = "editplot-enabled";
    public static final String BLOCKED_COMMANDS_BUILDERS = "blocked-commands-builders";

    // SHORTLINKS
    private static final String SHORTLINK = "shortlink.";
    public static final String SHORTLINK_ENABLE = SHORTLINK + "enable";
    public static final String SHORTLINK_APIKEY = SHORTLINK + "apikey";
    public static final String SHORTLINK_HOST = SHORTLINK + "host";

    // TUTORIALS
    private static final String TUTORIALS = "tutorials.";
    public static final String TUTORIAL_ENABLE = TUTORIALS + "tutorial-enable";
    public static final String TUTORIAL_REQUIRE_BEGINNER_TUTORIAL = TUTORIALS + "require-beginner-tutorial";
    public static final String TUTORIAL_NPC_NAME = TUTORIALS + "tutorial-npc-name";
    public static final String TUTORIAL_NPC_TEXTURE = TUTORIALS + "tutorial-npc-texture";
    public static final String TUTORIAL_NPC_SIGNATURE = TUTORIALS + "tutorial-npc-signature";
    public static final String TUTORIAL_CHAT_PREFIX = TUTORIALS + "tutorial-chat-prefix";
}