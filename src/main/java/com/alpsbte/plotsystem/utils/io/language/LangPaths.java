package com.alpsbte.plotsystem.utils.io.language;

public class LangPaths {
    private static final String LANG = "lang.";
    public static final String LANG_NAME = LANG + "name";
    public static final String LANG_HEAD_ID = LANG + "head-id";

    public static final class Plot {
        private static final String PLOT = "plot.";
        public static final String PLOT_NAME = PLOT + "plot-name";
        public static final String ID = PLOT + "id";
        public static final String OWNER = PLOT + "owner";
        public static final String MEMBERS = PLOT + "members";
        public static final String MEMBER = PLOT + "member";
        public static final String CITY = PLOT + "city";
        public static final String DIFFICULTY = PLOT + "difficulty";
        public static final String STATUS = PLOT + "status";
        public static final String SCORE = PLOT + "score";
        public static final String TOTAL_SCORE = PLOT + "total-score";
        public static final String EFFECTIVE_SCORE = PLOT + "effective-score";
        public static final String COMPLETED_PLOTS = PLOT + "completed-plots";

        public static final class GroupSystem {
            private static final String GROUP_SYSTEM = PLOT + "group-system.";
            public static final String EMPTY_MEMBER_SLOTS = GROUP_SYSTEM + "empty-member-slot";
            public static final String SHARED_BY_MEMBERS = GROUP_SYSTEM + "shared-by-members";
        }
    }

    public static final class CityProject {
        private static final String CITY_PROJECT = "city-project.";
        public static final String PROJECT_OPEN = CITY_PROJECT + "open";
        public static final String PROJECT_IN_PROGRESS = CITY_PROJECT + "in-progress";
        public static final String PROJECT_COMPLETED = CITY_PROJECT + "completed";
        public static final String PROJECT_NO_PLOTS = CITY_PROJECT + "no-plots-available";
    }

    public static final class Difficulty {
        private static final String DIFFICULTY = "difficulty.";
        public static final String AUTOMATIC = DIFFICULTY + "automatic";
        public static final String SCORE_MULTIPLIER = DIFFICULTY + "score-multiplier";
    }

    public static final class MenuTitle {
        private static final String MENU_TITLES = "menu-title.";
        public static final String CLOSE = MENU_TITLES + "close";
        public static final String BACK = MENU_TITLES + "back";
        public static final String NEXT_PAGE = MENU_TITLES + "next-page";
        public static final String PREVIOUS_PAGE = MENU_TITLES + "previous-page";
        public static final String ERROR = MENU_TITLES + "error";
        public static final String LOADING = MENU_TITLES + "loading";
        public static final String NAVIGATOR = MENU_TITLES + "navigator";
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
        public static final String SPECIAL_BLOCKS = MENU_TITLES + "special-blocks";
        public static final String REVIEW_POINT = MENU_TITLES + "review-point";
        public static final String REVIEW_POINTS = MENU_TITLES + "review-points";
        public static final String CANCEL = MENU_TITLES + "cancel";
        public static final String ADD_MEMBER_TO_PLOT = MENU_TITLES + "add-member-to-plot";
        public static final String COMPANION = MENU_TITLES + "companion";
        public static final String PLAYER_PLOTS = MENU_TITLES + "player-plots";
        public static final String LEAVE_PLOT = MENU_TITLES + "leave-plot";
        public static final String REVIEW_PLOTS = MENU_TITLES + "review-plots";
        public static final String REVIEW_PLOT = MENU_TITLES + "review-plot";
        public static final String ENTER_PLAYER_NAME = MENU_TITLES + "enter-player-name";
        public static final String SELECT_LANGUAGE = MENU_TITLES + "select-language";
        public static final String AUTO_DETECT_LANGUAGE = MENU_TITLES + "auto-detect-language";
    }

    public static final class MenuDescription {
        private static final String MENU_DESCRIPTIONS = "menu-description.";
        public static final String ERROR = MENU_DESCRIPTIONS + "error-desc";
        public static final String NAVIGATOR = MENU_DESCRIPTIONS + "navigator-desc";
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
        public static final String SPECIAL_BLOCKS = MENU_DESCRIPTIONS + "special-blocks-desc";
        public static final String ADD_MEMBER_TO_PLOT = MENU_DESCRIPTIONS + "add-member-to-plot-desc";
        public static final String REVIEW_POINTS = MENU_DESCRIPTIONS + "review-points-desc";
        public static final String SUBMIT_REVIEW = MENU_DESCRIPTIONS + "submit-review-desc";
        public static final String LEAVE_PLOT = MENU_DESCRIPTIONS + "leave-plot-desc";
        public static final String SELECT_LANGUAGE = MENU_DESCRIPTIONS + "select-language-desc";
        public static final String AUTO_DETECT_LANGUAGE = MENU_DESCRIPTIONS + "auto-detect-language-desc";
    }

    public static final class Review {
        private static final String REVIEW = "review.";
        public static final String MANAGE_AND_REVIEW_PLOTS = "manage-and-review-plots";
        public static final String REVIEW_PLOT = REVIEW + "review-plot";
        public static final String MANAGE_PLOT = REVIEW + "manage-plot";
        public static final String ACCEPTED = REVIEW + "accepted";
        public static final String REJECTED = REVIEW + "rejected";
        public static final String FEEDBACK = REVIEW + "feedback";
        public static final String REVIEWER = REVIEW + "reviewer";

        public static final class Criteria {
            private static final String CRITERIA = REVIEW + "criteria.";
            public static final String ACCURACY = CRITERIA + "accuracy";
            public static final String ACCURACY_DESC = CRITERIA + "accuracy-desc";
            public static final String BLOCK_PALETTE = CRITERIA + "block-palette";
            public static final String BLOCK_PALETTE_DESC = CRITERIA + "block-palette-desc";
            public static final String DETAILING = CRITERIA + "detailing";
            public static final String DETAILING_DESC = CRITERIA + "detailing-desc";
            public static final String TECHNIQUE = CRITERIA + "technique";
            public static final String TECHNIQUE_DESC = CRITERIA + "technique-desc";
        }
    }

    public static final class Note {
        private static final String NOTES = "note.";
        public static final String WONT_BE_ABLE_CONTINUE_BUILDING = NOTES + "wont-be-able-continue-building";
        public static final String SCORE_WILL_BE_SPLIT = NOTES + "score-will-be-split";
        public static final String PLAYER_HAS_TO_BE_ONLINE = NOTES + "player-has-to-be-online";

        public static final class Action {
            private static final String ACTION = NOTES + "action.";
            public static final String RIGHT_CLICK = ACTION + "right-click";
            public static final String CLICK_TO_ENABLE = ACTION + "click-to-enable";
            public static final String CLICK_TO_DISABLE = ACTION + "click-to-disable";
            public static final String CLICK_TO_REMOVE_PLOT_MEMBER = ACTION + "click-to-remove-plot-member";
            public static final String CLICK_TO_OPEN_LINK = ACTION + "click-to-open-link";
            public static final String CLICK_TO_OPEN_LINK_WITH_SHORTLINK = ACTION + "click-to-open-link-with-shortlink";
            public static final String CLICK_TO_SHOW_FEEDBACK = ACTION + "click-to-show-feedback";
            public static final String CLICK_TO_SHOW_OPEN_REVIEWS = ACTION + "click-to-show-open-reviews";
            public static final String CLICK_TO_SHOW_PLOTS = ACTION + "click-to-show-plots";
            public static final String CLICK_TO_PLAY_WITH_FRIENDS = ACTION + "click-to-play-with-friends";
        }

        public static final class Anvil {
            private static final String ANVIL = NOTES + "anvil.";
            public static final String PLAYER_IS_OWNER = ANVIL + "player-is-owner";
            public static final String PLAYER_ALREADY_ADDED = ANVIL + "player-already-added";
            public static final String PLAYER_NOT_ONLINE = ANVIL + "player-not-online";
            public static final String INVALID_INPUT = ANVIL + "invalid-input";
            public static final String ENTER_PLAYER_NAME = ANVIL + "enter-player-name";
        }
    }

    public static final class Message {
        private static final String MESSAGE = "message.";

        public static final class Info {
            private static final String INFO = MESSAGE + "info.";
            public static final String TELEPORTING_PLOT = INFO + "teleporting-plot";
            public static final String TELEPORTING_SPAWN = INFO + "teleporting-spawn";
            public static final String TELEPORTING_TPLL = INFO + "teleporting-tpll";
            public static final String ABANDONED_PLOT = INFO + "abandoned-plot";
            public static final String FINISHED_PLOT = INFO + "finished-plot";
            public static final String PLOT_MARKED_REVIEWED = INFO + "plot-marked-as-reviewed";
            public static final String PLOT_REJECTED = INFO + "plot-rejected";
            public static final String UNDID_SUBMISSION = INFO + "undid-submission";
            public static final String UNDID_REVIEW = INFO + "undid-review";
            public static final String REVIEWED_PLOT = INFO + "reviewed-plots";
            public static final String UNREVIEWED_PLOT = INFO + "unreviewed-plot";
            public static final String UNREVIEWED_PLOTS = INFO + "unreviewed-plots";
            public static final String UNFINISHED_PLOT = INFO + "unfinished-plot";
            public static final String UNFINISHED_PLOTS = INFO + "unfinished-plots";
            public static final String ENABLED_PLOT_PERMISSIONS = INFO + "enabled-build-permissions";
            public static final String DISABLED_PLOT_PERMISSIONS = INFO + "disabled-build-permissions";
            public static final String UPDATED_PLOT_FEEDBACK = INFO + "updated-plot-feedback";
            public static final String REMOVED_PLOT_MEMBER = INFO + "removed-plot-member";
            public static final String LEFT_PLOT = INFO + "left-plot";
            public static final String PLOT_WILL_GET_ABANDONED = INFO + "plot-will-get-abandoned-warning";
            public static final String PLOT_WILL_GET_REJECTED = INFO + "plot-will-get-rejected-warning";
            public static final String SAVING_PLOT = INFO + "saving-plot";
            public static final String CREATING_PLOT = INFO + "creating-plot";
            public static final String CREATED_NEW_PLOT = INFO + "created-new-plot";
            public static final String CHANGED_LANGUAGE = INFO + "changed-language";
        }

        public static final class Error {
            private static final String ERROR = MESSAGE + "error.";
            public static final String PLOT_DOES_NOT_EXIST = ERROR + "plot-does-not-exist";
            public static final String PLOT_EITHER_UNCLAIMED_OR_UNREVIEWED = ERROR + "plot-either-unclaimed-or-unreviewed";
            public static final String PLOT_HAS_NOT_YET_REVIEWED = ERROR + "plot-has-not-yet-reviewed";

            public static final String CAN_ONLY_ABANDON_UNFINISHED_PLOTS = ERROR + "can-only-abandon-unfinished-plots";
            public static final String CAN_ONLY_SUBMIT_UNFINISHED_PLOTS = ERROR + "can-only-submit-unfinished-plots";
            public static final String CAN_ONLY_UNDO_SUBMISSIONS_UNREVIEWED_PLOTS = ERROR + "can-only-undo-submissions-unreviewed-plots";
            public static final String CAN_ONLY_MANAGE_MEMBERS_UNFINISHED = ERROR + "can-only-manage-members-unfinished-plots";
            public static final String CAN_ONLY_TELEPORT_TO_PLOT = ERROR + "can-only-teleport-to-plot";
            public static final String CANNOT_UNDO_REVIEW = ERROR + "cannot-undo-review";
            public static final String CANNOT_SEND_FEEDBACK = ERROR + "cannot-send-feedback";
            public static final String CANNOT_REVIEW_OWN_PLOT = ERROR + "cannot-review-own-plot";

            public static final String PLAYER_HAS_NO_PERMISSIONS = ERROR + "player-has-no-permissions";
            public static final String PLAYER_HAS_NO_INVITATIONS = ERROR + "player-has-no-invitations";
            public static final String PLAYER_IS_NOT_ALLOWED = ERROR + "player-is-not-allowed";
            public static final String PLAYER_IS_PLOT_OWNER = ERROR + "player-is-plot-owner";
            public static final String PLAYER_IS_PLOT_MEMBER = ERROR + "player-is-plot-member";
            public static final String PLAYER_IS_NOT_ONLINE = ERROR + "player-is-not-online";
            public static final String PLAYER_NOT_FOUND = ERROR + "player-not-found";
            public static final String PLAYER_NEEDS_TO_BE_ON_PLOT = ERROR + "player-needs-to-be-on-plot";
            public static final String PLAYER_NEEDS_HIGHER_SCORE = ERROR + "player-needs-higher-score";

            public static final String INVALID_INPUT = ERROR + "invalid-input";
            public static final String ERROR_OCCURRED = ERROR + "error-occurred";
            public static final String COMMAND_DISABLED = ERROR + "command-disabled";
            public static final String NO_PLOTS_LEFT = ERROR + "no-plots-left";
            public static final String PLEASE_WAIT = ERROR + "please-wait";
            public static final String ALL_SLOTS_OCCUPIED = ERROR + "all-slots-occupied";
        }
    }

    public static final String CONFIG_VERSION = "config-version";
}
