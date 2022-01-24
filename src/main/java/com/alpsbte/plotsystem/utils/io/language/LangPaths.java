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
        public static final String CANCEL_PLOT = MENU_TITLES + "cancel-plot";
        public static final String ADD_MEMBER_TO_PLOT = MENU_TITLES + "add-member-to-plot";
        public static final String COMPANION = MENU_TITLES + "companion";
        public static final String PLAYER_PLOTS = MENU_TITLES + "player-plots";
        public static final String LEAVE_PLOT = MENU_TITLES + "leave-plot";
    }

    public static final class MenuDescription {
        private static final String MENU_DESCRIPTIONS = "menu-description.";
        public static final String CLOSE = MENU_DESCRIPTIONS + "close-desc";
        public static final String BACK = MENU_DESCRIPTIONS + "back-desc";
        public static final String NEXT_PAGE = MENU_DESCRIPTIONS + "next-page-desc";
        public static final String PREVIOUS_PAGE = MENU_DESCRIPTIONS + "previous-page-desc";
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
    }

    public static final class Review {
        private static final String REVIEW = "review.";
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
            public static final String CLICK_TO_REMOVE_PLOT_MEMBER = ACTION + "click-to-remove-plot-member";
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
            public static final String CLICK_TO_OPEN_LINK = INFO + "click-to-open-link";
            public static final String CLICK_TO_SHOW_FEEDBACK = INFO + "click-to-show-feedback";
            public static final String CLICK_TO_SHOW_OPEN_REVIEWS = INFO + "click-to-show-open-reviews";
            public static final String CLICK_TO_SHOW_PLOTS = INFO + "click-to-show-plots";
            public static final String REMOVED_PLOT_MEMBER = INFO + "removed-plot-member";
            public static final String LEFT_PLOT = INFO + "left-plot";
            public static final String WANT_TO_PLAY_WITH_FRIENDS = INFO + "want-to-play-with-friends";
        }

        public static final class Error {
            private static final String ERROR = MESSAGE + "error.";
            public static final String TELEPORTING_PLOT = ERROR + "could-not-find-player";
            public static final String PLOT_DOES_NOT_EXIST = ERROR + "plot-does-not-exist";
            public static final String NO_PERMISSIONS = ERROR + "no-permissions";
            public static final String NOT_ALLOWED = ERROR + "not-allowed";
            public static final String ONLY_ABANDON_UNFINISHED_PLOTS = ERROR + "only-abandon-unfinished-plots";
            public static final String ONLY_SUBMIT_UNFINISHED_PLOTS = ERROR + "only-submit-unfinished-plots";
            public static final String ONLY_UNDO_SUBMISSIONS_UNREVIEWED_PLOTS = ERROR + "only-undo-submissions-unreviewed-plots";
            public static final String ONLY_MANAGE_MEMBERS_UNFINISHED = ERROR + "only-manage-members-unfinished-plots";
            public static final String ONLY_TELEPORT_PLOT = ERROR + "only-teleport-plot";
            public static final String PLOT_EITHER_UNCLAIMED_OR_UNREVIEWED = ERROR + "plot-either-unclaimed-or-unreviewed";
            public static final String CANNOT_UNDO_REVIEW = ERROR + "cannot-undo-review";
            public static final String ERROR_OCCURRED = ERROR + "error-occurred";
            public static final String NOT_YET_REVIEWED = ERROR + "not-yet-reviewed";
            public static final String NO_INVITATIONS = ERROR + "no-invitations";
            public static final String PLAYER_IS_PLOT_OWNER = ERROR + "player-is-plot-owner";
            public static final String PLAYER_IS_NOT_MEMBER = ERROR + "player-is-plot-member";
            public static final String PLAYER_IS_NOT_ONLINE = ERROR + "player-is-not-online";
            public static final String PLAYER_NOT_FOUND = ERROR + "player-not-found";
            public static final String INVALID_INPUT = ERROR + "invalid-input";
            public static final String COMMAND_DISABLED = ERROR + "command-disabled";
            public static final String CANNOT_SEND_FEEDBACK = ERROR + "cannot-send-feedback";
            public static final String NEED_TO_BE_ON_PLOT = ERROR + "need-to-be-on-plot";
            public static final String NEED_HIGHER_SCORE = ERROR + "need-higher-score";
            public static final String NO_PLOTS_LEFT = ERROR + "no-plots-left";
        }
    }

    public static final String CONFIG_VERSION = "config-version";
}
