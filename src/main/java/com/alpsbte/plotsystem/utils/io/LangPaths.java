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

public abstract class LangPaths {
    private LangPaths() {}

    public static final class Plot {
        private Plot() {}

        private static final String PLOT_PREFIX = "plot.";
        public static final String PLOT_NAME = PLOT_PREFIX + "plot-name";
        public static final String ID = PLOT_PREFIX + "id";
        public static final String OWNER = PLOT_PREFIX + "owner";
        public static final String MEMBERS = PLOT_PREFIX + "members";
        public static final String MEMBER = PLOT_PREFIX + "member";
        public static final String CITY = PLOT_PREFIX + "city";
        public static final String COUNTRY = PLOT_PREFIX + "country";
        public static final String DIFFICULTY = PLOT_PREFIX + "difficulty";
        public static final String STATUS = PLOT_PREFIX + "status";
        public static final String SCORE = PLOT_PREFIX + "score";
        public static final String TOTAL_SCORE = PLOT_PREFIX + "total-score";
        public static final String COMPLETED_PLOTS = PLOT_PREFIX + "completed-plots";

        public static final class GroupSystem {
            private GroupSystem() {}

            private static final String GROUP_SYSTEM = PLOT_PREFIX + "group-system.";
            public static final String EMPTY_MEMBER_SLOTS = GROUP_SYSTEM + "empty-member-slot";
            public static final String SHARED_BY_MEMBERS = GROUP_SYSTEM + "shared-by-members";
        }
    }

    public static final class CityProject {
        private CityProject() {}
        private static final String CITY_PROJECT = "city-project.";
        public static final String CITIES = CITY_PROJECT + "cities";
        public static final String PROJECT_OPEN = CITY_PROJECT + "open";
        public static final String PROJECT_IN_PROGRESS = CITY_PROJECT + "in-progress";
        public static final String PROJECT_COMPLETED = CITY_PROJECT + "completed";
        public static final String PROJECT_PLOTS_AVAILABLE = CITY_PROJECT + "plots-available";
        public static final String PROJECT_NO_PLOTS_AVAILABLE = CITY_PROJECT + "no-plots-available";
        public static final String FOR_YOUR_DIFFICULTY = CITY_PROJECT + "for-your-difficulty";
    }

    public static final class Country {
        private Country() {}

        private static final String COUNTRY_PREFIX = "country.";
        public static final String COUNTRIES = COUNTRY_PREFIX + "countries";
    }

    public static final class Continent {
        private Continent() {}

        private static final String CONTINENT_PREFIX = "continent.";
        public static final String EUROPE = CONTINENT_PREFIX + "europe";
        public static final String ASIA = CONTINENT_PREFIX + "asia";
        public static final String AFRICA = CONTINENT_PREFIX + "africa";
        public static final String OCEANIA = CONTINENT_PREFIX + "oceania";
        public static final String SOUTH_AMERICA = CONTINENT_PREFIX + "south-america";
        public static final String NORTH_AMERICA = CONTINENT_PREFIX + "north-america";
    }

    public static final class Difficulty {
        private Difficulty() {}

        private static final String DIFFICULTY_PREFIX = "difficulty.";
        public static final String AUTOMATIC = DIFFICULTY_PREFIX + "automatic";
        public static final String SCORE_MULTIPLIER = DIFFICULTY_PREFIX + "score-multiplier";
    }

    public static final class MenuTitle {
        private MenuTitle() {}
        private static final String MENU_TITLES = "menu-title.";
        public static final String CLOSE = MENU_TITLES + "close";
        public static final String BACK = MENU_TITLES + "back";
        public static final String NEXT_PAGE = MENU_TITLES + "next-page";
        public static final String PREVIOUS_PAGE = MENU_TITLES + "previous-page";
        public static final String ERROR = MENU_TITLES + "error";
        public static final String LOADING = MENU_TITLES + "loading";
        public static final String PLOT_DIFFICULTY = MENU_TITLES + "plot-difficulty";
        public static final String SLOT = MENU_TITLES + "slot";
        public static final String BUILDER_UTILITIES = MENU_TITLES + "builder-utilities";
        public static final String SHOW_PLOTS = MENU_TITLES + "show-plots";
        public static final String SETTINGS = MENU_TITLES + "settings";
        public static final String SUBMIT = MENU_TITLES + "submit";
        public static final String TELEPORT = MENU_TITLES + "teleport";
        public static final String ABANDON = MENU_TITLES + "abandon";
        public static final String UNDO_SUBMIT = MENU_TITLES + "undo-submit";
        public static final String MANAGE_MEMBERS = MENU_TITLES + "manage-members";
        public static final String FEEDBACK = MENU_TITLES + "feedback";
        public static final String CUSTOM_HEADS = MENU_TITLES + "custom-heads";
        public static final String BANNER_MAKER = MENU_TITLES + "banner-maker";
        public static final String SPECIAL_TOOLS = MENU_TITLES + "special-tools";
        public static final String REVIEW_POINT = MENU_TITLES + "review-point";
        public static final String REVIEW_POINTS = MENU_TITLES + "review-points";
        public static final String CANCEL = MENU_TITLES + "cancel";
        public static final String ADD_MEMBER_TO_PLOT = MENU_TITLES + "add-member-to-plot";
        public static final String COMPANION = MENU_TITLES + "companion";
        public static final String COMPANION_SELECT_CONTINENT = MENU_TITLES + "companion-select-continent";
        public static final String COMPANION_SELECT_COUNTRY = MENU_TITLES + "companion-select-country";
        public static final String COMPANION_SELECT_CITY = MENU_TITLES + "companion-select-city";
        public static final String PLAYER_PLOTS = MENU_TITLES + "player-plots";
        public static final String LEAVE_PLOT = MENU_TITLES + "leave-plot";
        public static final String REVIEW_PLOTS = MENU_TITLES + "review-plots";
        public static final String REVIEW_PLOT = MENU_TITLES + "review-plot";
        public static final String SELECT_LANGUAGE = MENU_TITLES + "select-language";
        public static final String SELECT_PLOT_TYPE = MENU_TITLES + "select-plot-type";
        public static final String SELECT_FOCUS_MODE = MENU_TITLES + "select-focus-mode";
        public static final String SELECT_INSPIRATION_MODE = MENU_TITLES + "select-local-inspiration-mode";
        public static final String SELECT_CITY_INSPIRATION_MODE = MENU_TITLES + "select-city-inspiration-mode";
        public static final String FILTER_BY_COUNTRY = MENU_TITLES + "filter-by-country";
        public static final String INFORMATION = MENU_TITLES + "information";
        public static final String TUTORIALS = MENU_TITLES + "tutorials";
        public static final String TUTORIAL_STAGES = MENU_TITLES + "tutorial-stages";
        public static final String TUTORIAL_END = MENU_TITLES + "tutorial-end";
        public static final String TUTORIAL_BEGINNER = MENU_TITLES + "tutorial-beginner";
        public static final String COMPANION_RANDOM = MENU_TITLES + "companion-random";
    }

    public static final class MenuDescription {
        private MenuDescription() {}
        private static final String MENU_DESCRIPTIONS = "menu-description.";
        public static final String ERROR = MENU_DESCRIPTIONS + "error-desc";
        public static final String PLOT_DIFFICULTY = MENU_DESCRIPTIONS + "plot-difficulty-desc";
        public static final String SLOT = MENU_DESCRIPTIONS + "slot-desc";
        public static final String BUILDER_UTILITIES = MENU_DESCRIPTIONS + "builder-utilities-desc";
        public static final String SHOW_PLOTS = MENU_DESCRIPTIONS + "show-plots-desc";
        public static final String SETTINGS = MENU_DESCRIPTIONS + "settings-desc";
        public static final String SUBMIT_PLOT = MENU_DESCRIPTIONS + "submit-plot-desc";
        public static final String TELEPORT = MENU_DESCRIPTIONS + "teleport-desc";
        public static final String ABANDON = MENU_DESCRIPTIONS + "abandon-desc";
        public static final String UNDO_SUBMIT = MENU_DESCRIPTIONS + "undo-submit-desc";
        public static final String MANAGE_MEMBERS = MENU_DESCRIPTIONS + "manage-members-desc";
        public static final String FEEDBACK = MENU_DESCRIPTIONS + "feedback-desc";
        public static final String CUSTOM_HEADS = MENU_DESCRIPTIONS + "custom-heads-desc";
        public static final String BANNER_MAKER = MENU_DESCRIPTIONS + "banner-maker-desc";
        public static final String SPECIAL_TOOLS = MENU_DESCRIPTIONS + "special-tools-desc";
        public static final String ADD_MEMBER_TO_PLOT = MENU_DESCRIPTIONS + "add-member-to-plot-desc";
        public static final String REVIEW_POINTS = MENU_DESCRIPTIONS + "review-points-desc";
        public static final String SUBMIT_REVIEW = MENU_DESCRIPTIONS + "submit-review-desc";
        public static final String LEAVE_PLOT = MENU_DESCRIPTIONS + "leave-plot-desc";
        public static final String SELECT_LANGUAGE = MENU_DESCRIPTIONS + "select-language-desc";
        public static final String SELECT_PLOT_TYPE = MENU_DESCRIPTIONS + "select-plot-type-desc";
        public static final String SELECT_FOCUS_MODE = MENU_DESCRIPTIONS + "select-focus-mode-desc";
        public static final String SELECT_INSPIRATION_MODE = MENU_DESCRIPTIONS + "select-local-inspiration-mode-desc";
        public static final String SELECT_CITY_INSPIRATION_MODE = MENU_DESCRIPTIONS + "select-city-inspiration-mode-desc";
        public static final String FILTER = MENU_DESCRIPTIONS + "filter-desc";
        public static final String INFORMATION = MENU_DESCRIPTIONS + "information-desc";
        public static final String TUTORIALS = MENU_DESCRIPTIONS + "tutorials-desc";
        public static final String TUTORIAL_END = MENU_DESCRIPTIONS + "tutorial-end-desc";
        public static final String TUTORIAL_BEGINNER = MENU_DESCRIPTIONS + "tutorial-beginner-desc";
        public static final String COMPANION_RANDOM = MENU_DESCRIPTIONS + "companion-random-desc";
    }

    public static final class Review {
        private Review() {}

        private static final String REVIEW_PREFIX = "review.";
        public static final String MANAGE_AND_REVIEW_PLOTS = REVIEW_PREFIX + "manage-and-review-plots";
        public static final String REVIEW_PLOT = REVIEW_PREFIX + "review-plot";
        public static final String MANAGE_PLOT = REVIEW_PREFIX + "manage-plot";
        public static final String ACCEPTED = REVIEW_PREFIX + "accepted";
        public static final String ABANDONED = REVIEW_PREFIX + "abandoned";
        public static final String REJECTED = REVIEW_PREFIX + "rejected";
        public static final String FEEDBACK = REVIEW_PREFIX + "feedback";
        public static final String REVIEWER = REVIEW_PREFIX + "reviewer";
        public static final String PLAYER_LANGUAGE = REVIEW_PREFIX + "player-language";

        public static final class Criteria {
            private Criteria() {}

            private static final String CRITERIA_PREFIX = REVIEW_PREFIX + "criteria.";
            public static final String ACCURACY = CRITERIA_PREFIX + "accuracy";
            public static final String ACCURACY_DESC = CRITERIA_PREFIX + "accuracy-desc";
            public static final String BLOCK_PALETTE = CRITERIA_PREFIX + "block-palette";
            public static final String BLOCK_PALETTE_DESC = CRITERIA_PREFIX + "block-palette-desc";
            public static final String DETAILING = CRITERIA_PREFIX + "detailing";
            public static final String DETAILING_DESC = CRITERIA_PREFIX + "detailing-desc";
            public static final String TECHNIQUE = CRITERIA_PREFIX + "technique";
            public static final String TECHNIQUE_DESC = CRITERIA_PREFIX + "technique-desc";
        }
    }

    public static final class Note {
        private Note() {}
        private static final String NOTES = "note.";
        public static final String TIP = NOTES + "tip";
        public static final String UNDER_CONSTRUCTION = NOTES + "under-construction";
        public static final String WONT_BE_ABLE_CONTINUE_BUILDING = NOTES + "wont-be-able-continue-building";
        public static final String SCORE_WILL_BE_SPLIT = NOTES + "score-will-be-split";
        public static final String PLAYER_HAS_TO_BE_ONLINE = NOTES + "player-has-to-be-online";

        public static final class Action {
            private Action() {}

            private static final String ACTION_PREFIX = NOTES + "action.";
            public static final String READ = ACTION_PREFIX + "read";
            public static final String READ_MORE = ACTION_PREFIX + "read-more";
            public static final String MARK_AS_READ = ACTION_PREFIX + "mark-as-read";
            public static final String START = ACTION_PREFIX + "start";
            public static final String CONTINUE = ACTION_PREFIX + "continue";
            public static final String CONTINUE_TUTORIAL = ACTION_PREFIX + "continue-tutorial";
            public static final String CREATE_PLOT = ACTION_PREFIX + "create-plot";
            public static final String RIGHT_CLICK = ACTION_PREFIX + "right-click";
            public static final String LEFT_CLICK = ACTION_PREFIX + "left-click";
            public static final String ACCEPT = ACTION_PREFIX + "accept";
            public static final String REJECT = ACTION_PREFIX + "reject";
            public static final String CLICK_TO_CREATE = ACTION_PREFIX + "click-to-create-plot";
            public static final String CLICK_TO_PROCEED = ACTION_PREFIX + "click-to-proceed";
            public static final String CLICK_TO_REMOVE_PLOT_MEMBER = ACTION_PREFIX + "click-to-remove-plot-member";
            public static final String CLICK_TO_OPEN_LINK = ACTION_PREFIX + "click-to-open-link";
            public static final String CLICK_TO_OPEN_LINK_WITH_SHORTLINK = ACTION_PREFIX + "click-to-open-link-with-shortlink";
            public static final String CLICK_TO_SHOW_FEEDBACK = ACTION_PREFIX + "click-to-show-feedback";
            public static final String CLICK_TO_SHOW_OPEN_REVIEWS = ACTION_PREFIX + "click-to-show-open-reviews";
            public static final String CLICK_TO_SHOW_PLOTS = ACTION_PREFIX + "click-to-show-plots";
            public static final String CLICK_TO_PLAY_WITH_FRIENDS = ACTION_PREFIX + "click-to-play-with-friends";
            public static final String TUTORIAL_SHOW_STAGES = ACTION_PREFIX + "tutorial-show-stages";
        }
    }

    public static final class Message {
        private Message() {}

        private static final String MESSAGE_PREFIX = "message.";

        public static final class Info {
            private Info() {}

            private static final String INFO_PREFIX = MESSAGE_PREFIX + "info.";
            public static final String TELEPORTING_PLOT = INFO_PREFIX + "teleporting-plot";
            public static final String TELEPORTING_TPLL = INFO_PREFIX + "teleporting-tpll";
            public static final String ABANDONED_PLOT = INFO_PREFIX + "abandoned-plot";
            public static final String FINISHED_PLOT = INFO_PREFIX + "finished-plot";
            public static final String PLOT_MARKED_REVIEWED = INFO_PREFIX + "plot-marked-as-reviewed";
            public static final String PLOT_REJECTED = INFO_PREFIX + "plot-rejected";
            public static final String UNDID_SUBMISSION = INFO_PREFIX + "undid-submission";
            public static final String UNDID_REVIEW = INFO_PREFIX + "undid-review";
            public static final String REVIEWED_PLOT = INFO_PREFIX + "reviewed-plot";
            public static final String UNREVIEWED_PLOT = INFO_PREFIX + "unreviewed-plot";
            public static final String UNREVIEWED_PLOTS = INFO_PREFIX + "unreviewed-plots";
            public static final String UNFINISHED_PLOT = INFO_PREFIX + "unfinished-plot";
            public static final String UNFINISHED_PLOTS = INFO_PREFIX + "unfinished-plots";
            public static final String ENABLED_PLOT_PERMISSIONS = INFO_PREFIX + "enabled-build-permissions";
            public static final String DISABLED_PLOT_PERMISSIONS = INFO_PREFIX + "disabled-build-permissions";
            public static final String UPDATED_PLOT_FEEDBACK = INFO_PREFIX + "updated-plot-feedback";
            public static final String REMOVED_PLOT_MEMBER = INFO_PREFIX + "removed-plot-member";
            public static final String LEFT_PLOT = INFO_PREFIX + "left-plot";
            public static final String PLOT_WILL_GET_ABANDONED = INFO_PREFIX + "plot-will-get-abandoned-warning";
            public static final String PLOT_WILL_GET_REJECTED = INFO_PREFIX + "plot-will-get-rejected-warning";
            public static final String SAVING_PLOT = INFO_PREFIX + "saving-plot";
            public static final String CREATING_PLOT = INFO_PREFIX + "creating-plot";
            public static final String CREATED_NEW_PLOT = INFO_PREFIX + "created-new-plot";
            public static final String CHAT_ENTER_PLAYER = INFO_PREFIX + "chat-enter-player";
            public static final String CHAT_ENTER_FEEDBACK = INFO_PREFIX + "chat-enter-feedback";
            public static final String CHAT_INPUT_EXPIRES_AFTER = INFO_PREFIX + "chat-input-expires-after";
            public static final String BEGINNER_TUTORIAL_REQUIRED = INFO_PREFIX + "beginner-tutorial-required";
            public static final String BEGINNER_TUTORIAL_COMPLETED = INFO_PREFIX + "beginner-tutorial-completed";
            public static final String PLAYER_INVITE_SENT = INFO_PREFIX + "player-invite-sent";
            public static final String PLAYER_INVITE_TO_SENT = INFO_PREFIX + "player-invite-to-sent";
            public static final String PLAYER_INVITE_ACCEPTED = INFO_PREFIX + "player-invite-accepted";
            public static final String PLAYER_INVITE_TO_ACCEPTED = INFO_PREFIX + "player-invite-to-accepted";
            public static final String PLAYER_INVITE_REJECTED = INFO_PREFIX + "player-invite-rejected";
        }

        public static final class Error {
            private Error() {}

            private static final String ERROR_PREFIX = MESSAGE_PREFIX + "error.";
            public static final String PLOT_DOES_NOT_EXIST = ERROR_PREFIX + "plot-does-not-exist";
            public static final String PLOT_EITHER_UNCLAIMED_OR_UNREVIEWED = ERROR_PREFIX + "plot-either-unclaimed-or-unreviewed";
            public static final String PLOT_HAS_NOT_YET_REVIEWED = ERROR_PREFIX + "plot-has-not-yet-reviewed";

            public static final String CAN_ONLY_ABANDON_UNFINISHED_PLOTS = ERROR_PREFIX + "can-only-abandon-unfinished-plots";
            public static final String CAN_ONLY_SUBMIT_UNFINISHED_PLOTS = ERROR_PREFIX + "can-only-submit-unfinished-plots";
            public static final String CAN_ONLY_UNDO_SUBMISSIONS_UNREVIEWED_PLOTS = ERROR_PREFIX + "can-only-undo-submissions-unreviewed-plots";
            public static final String CAN_ONLY_MANAGE_MEMBERS_UNFINISHED = ERROR_PREFIX + "can-only-manage-members-unfinished-plots";
            public static final String CANNOT_TELEPORT_OUTSIDE_PLOT = ERROR_PREFIX + "cannot-teleport-outside-plot";
            public static final String CANNOT_UNDO_REVIEW = ERROR_PREFIX + "cannot-undo-review";
            public static final String CANNOT_SEND_FEEDBACK = ERROR_PREFIX + "cannot-send-feedback";
            public static final String CANNOT_REVIEW_OWN_PLOT = ERROR_PREFIX + "cannot-review-own-plot";

            public static final String PLAYER_HAS_NO_PERMISSIONS = ERROR_PREFIX + "player-has-no-permissions";
            public static final String PLAYER_HAS_NO_INVITATIONS = ERROR_PREFIX + "player-has-no-invitations";
            public static final String PLAYER_IS_NOT_ALLOWED = ERROR_PREFIX + "player-is-not-allowed";
            public static final String PLAYER_IS_PLOT_OWNER = ERROR_PREFIX + "player-is-plot-owner";
            public static final String PLAYER_IS_PLOT_MEMBER = ERROR_PREFIX + "player-is-plot-member";
            public static final String PLAYER_IS_NOT_ONLINE = ERROR_PREFIX + "player-is-not-online";
            public static final String PLAYER_NOT_FOUND = ERROR_PREFIX + "player-not-found";
            public static final String PLAYER_ALREADY_INVITED = ERROR_PREFIX + "player-already-invited";
            public static final String PLAYER_INVITE_EXPIRED = ERROR_PREFIX + "player-invite-expired";
            public static final String PLAYER_INVITE_TO_EXPIRED = ERROR_PREFIX + "player-invite-to-expired";
            public static final String PLAYER_INVITE_TO_REJECTED = ERROR_PREFIX + "player-invite-to-rejected";
            public static final String PLAYER_NEEDS_TO_BE_ON_PLOT = ERROR_PREFIX + "player-needs-to-be-on-plot";
            public static final String PLAYER_NEEDS_HIGHER_SCORE = ERROR_PREFIX + "player-needs-higher-score";

            public static final String ERROR_OCCURRED = ERROR_PREFIX + "error-occurred";
            public static final String COMMAND_DISABLED = ERROR_PREFIX + "command-disabled";
            public static final String NO_PLOTS_LEFT = ERROR_PREFIX + "no-plots-left";
            public static final String PLEASE_WAIT = ERROR_PREFIX + "please-wait";
            public static final String ALL_SLOTS_OCCUPIED = ERROR_PREFIX + "all-slots-occupied";
            public static final String CHAT_INPUT_EXPIRED = ERROR_PREFIX + "chat-input-expired";
            public static final String TUTORIAL_DISABLED = ERROR_PREFIX + "tutorial-disabled";
            public static final String TUTORIAL_ALREADY_RUNNING = ERROR_PREFIX + "tutorial-already-running";
        }
    }

    public static final class Leaderboards {
        private Leaderboards() {}
        private static final String LBS = "leaderboards.";
        public static final String PAGES = LBS + "pages.";
        public static final String ACTIONBAR_POSITION = LBS + "actionbar-position";
        public static final String ACTIONBAR_PERCENTAGE = LBS + "actionbar-percentage";
        public static final String NOT_ON_LEADERBOARD = LBS + "not-on-leaderboard";
    }

    public static final class Tutorials {
        private Tutorials() {}

        public static final String TUTORIALS_PREFIX = "tutorials.";
        public static final String STAGE = TUTORIALS_PREFIX + "stage";
        public static final String NEW_STAGE_UNLOCKED = TUTORIALS_PREFIX + "new-stage-unlocked";
        public static final String TUTORIAL_COMPLETED = TUTORIALS_PREFIX + "tutorial-completed";

        public static class Beginner {
            private Beginner() {}

            private static final String TUTORIALS_BEGINNER = TUTORIALS_PREFIX + "beginner.";

            private static final String STAGE1 = TUTORIALS_BEGINNER + "stage-1.";
            public static final String STAGE1_TITLE = STAGE1 + "stage-1-title";
            public static final String STAGE1_MESSAGES = STAGE1 + "stage-1-messages";
            public static final String STAGE1_TASKS = STAGE1 + "stage-1-tasks";


            private static final String STAGE2 = TUTORIALS_BEGINNER + "stage-2.";
            public static final String STAGE2_TITLE = STAGE2 + "stage-2-title";
            public static final String STAGE2_MESSAGES = STAGE2 + "stage-2-messages";
            public static final String STAGE2_TASKS = STAGE2 + "stage-2-tasks";


            private static final String STAGE3 = TUTORIALS_BEGINNER + "stage-3.";
            public static final String STAGE3_TITLE = STAGE3 + "stage-3-title";
            public static final String STAGE3_MESSAGES = STAGE3 + "stage-3-messages";
            public static final String STAGE3_TASKS = STAGE3 + "stage-3-tasks";


            private static final String STAGE4 = TUTORIALS_BEGINNER + "stage-4.";
            public static final String STAGE4_TITLE = STAGE4 + "stage-4-title";
            public static final String STAGE4_MESSAGES = STAGE4 + "stage-4-messages";
            public static final String STAGE4_TASKS = STAGE4 + "stage-4-tasks";


            private static final String STAGE5 = TUTORIALS_BEGINNER + "stage-5.";
            public static final String STAGE5_TITLE = STAGE5 + "stage-5-title";
            public static final String STAGE5_MESSAGES = STAGE5 + "stage-5-messages";
            public static final String STAGE5_TASKS = STAGE5 + "stage-5-tasks";


            private static final String STAGE6 = TUTORIALS_BEGINNER + "stage-6.";
            public static final String STAGE6_TITLE = STAGE6 + "stage-6-title";
            public static final String STAGE6_MESSAGES = STAGE6 + "stage-6-messages";
            public static final String STAGE6_TASKS = STAGE6 + "stage-6-tasks";


            private static final String STAGE7 = TUTORIALS_BEGINNER + "stage-7.";
            public static final String STAGE7_TITLE = STAGE7 + "stage-7-title";
            public static final String STAGE7_MESSAGES = STAGE7 + "stage-7-messages";
            public static final String STAGE7_TASKS = STAGE7 + "stage-7-tasks";


            private static final String STAGE8 = TUTORIALS_BEGINNER + "stage-8.";
            public static final String STAGE8_TITLE = STAGE8 + "stage-8-title";
            public static final String STAGE8_MESSAGES = STAGE8 + "stage-8-messages";
            public static final String STAGE8_TASKS = STAGE8 + "stage-8-tasks";


            private static final String STAGE9 = TUTORIALS_BEGINNER + "stage-9.";
            public static final String STAGE9_TITLE = STAGE9 + "stage-9-title";
            public static final String STAGE9_MESSAGES = STAGE9 + "stage-9-messages";
            public static final String STAGE9_TASKS = STAGE9 + "stage-9-tasks";


            private static final String STAGE10 = TUTORIALS_BEGINNER + "stage-10.";
            public static final String STAGE10_TITLE = STAGE10 + "stage-10-title";
            public static final String STAGE10_MESSAGES = STAGE10 + "stage-10-messages";
            public static final String STAGE10_TASKS = STAGE10 + "stage-10-tasks";
        }
    }
}