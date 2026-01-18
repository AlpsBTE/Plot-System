package com.alpsbte.plotsystem.utils.io;

public abstract class ConfigPaths {

    private ConfigPaths() {throw new IllegalStateException("Utility class");}

    // General Behaviour
    public static final String SPAWN_WORLD = "spawn-world";
    public static final String ENABLE_SCORE_REQUIREMENT = "enable-score-requirement";
    public static final String DEV_MODE = "dev-mode";
    public static final String INACTIVITY_INTERVAL = "inactivity-interval";
    public static final String INACTIVITY_NOTIFICATION_TIME = "inactivity-notification-time";
    public static final String INACTIVITY_NOTIFICATION_DAYS = "inactivity-notification-days";
    public static final String REJECTED_INACTIVITY_INTERVAL = "rejected-inactivity-interval";
    public static final String ENABLE_GROUP_SUPPORT = "enable-group-support";
    public static final String UNFINISHED_REMINDER_INTERVAL = "unfinished-reminder-interval";
    public static final String DISABLE_CITY_INSPIRATION_MODE = "disable-city-inspiration-mode";
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