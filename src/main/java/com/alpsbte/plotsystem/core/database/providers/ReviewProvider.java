/*
 *  The MIT License (MIT)
 *
 *  Copyright © 2021-2025, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.core.database.providers;

import com.alpsbte.alpslib.io.database.SqlHelper;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.core.system.review.BuildTeamToggleCriteria;
import com.alpsbte.plotsystem.core.system.review.PlotReview;
import com.alpsbte.plotsystem.core.system.review.ReviewNotification;
import com.alpsbte.plotsystem.core.system.review.ReviewRating;
import com.alpsbte.plotsystem.core.system.review.ToggleCriteria;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ReviewProvider {
    protected static final List<ReviewNotification> NOTIFICATIONS = new ArrayList<>();
    protected static final List<ToggleCriteria> TOGGLE_CRITERIA = new ArrayList<>();
    protected static final List<BuildTeamToggleCriteria> BUILD_TEAM_TOGGLE_CRITERIA = new ArrayList<>();

    public ReviewProvider() {
        // cache all review notifications
        String qAllNotifys = "SELECT review_id, uuid FROM builder_has_review_notification;";
        Utils.handleSqlException(() -> SqlHelper.runQuery(qAllNotifys, ps -> {
            ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    NOTIFICATIONS.add(new ReviewNotification(rs.getInt(1), UUID.fromString(rs.getString(2))));
                }
        }));

        // cache all toggle criteria
        String qAllToggleCriterias = "SELECT criteria_name, is_optional FROM review_toggle_criteria;";
        Utils.handleSqlException(() -> SqlHelper.runQuery(qAllToggleCriterias, ps -> {
            ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    TOGGLE_CRITERIA.add(new ToggleCriteria(rs.getString(1), rs.getBoolean(2)));
                }
        }));

        // cache all build team toggle criteria
        String qAllBuildteamCriteria = "SELECT criteria_name, build_team_id FROM build_team_uses_toggle_criteria;";
        Utils.handleSqlException(() -> SqlHelper.runQuery(qAllBuildteamCriteria, ps -> {
            ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String criteriaName = rs.getString(1);
                    ToggleCriteria toggle = TOGGLE_CRITERIA.stream().filter(c -> c.criteriaName().equals(criteriaName)).findFirst().orElseThrow();
                    BUILD_TEAM_TOGGLE_CRITERIA.add(new BuildTeamToggleCriteria(rs.getInt(2), toggle));
                }
        }));
    }

    public Optional<PlotReview> getReview(int reviewId) {
        String qById = "SELECT plot_id, rating, score, feedback, reviewed_by FROM plot_review WHERE review_id = ?;";
        return Utils.handleSqlException(Optional.empty(), () -> SqlHelper.runQuery(qById, ps -> {
            ps.setInt(1, reviewId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return Optional.empty();

            int plotId = rs.getInt(1);
            ReviewRating rating = getReviewRating(reviewId, rs.getString(2));
            int score = rs.getInt(3);
            String feedback = rs.getString(4);
            UUID reviewedBy = UUID.fromString(rs.getString(5));

            return Optional.of(new PlotReview(reviewId, plotId, rating, score, feedback, reviewedBy));
        }));
    }

    public Optional<PlotReview> getLatestReview(int plotId) {
        String qLatestByPlotId = "SELECT review_id, rating, score, feedback, reviewed_by FROM plot_review WHERE plot_id = ? ORDER BY review_date DESC LIMIT 1;";
        return Utils.handleSqlException(Optional.empty(), () -> SqlHelper.runQuery(qLatestByPlotId, ps -> {
            ps.setInt(1, plotId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return Optional.empty();

            int reviewId = rs.getInt(1);
            ReviewRating rating = getReviewRating(reviewId, rs.getString(2));
            int score = rs.getInt(3);
            String feedback = rs.getString(4);
            UUID reviewedBy = UUID.fromString(rs.getString(5));

            return Optional.of(new PlotReview(reviewId, plotId, rating, score, feedback, reviewedBy));
        }));
    }

    public List<PlotReview> getPlotReviewHistory(int plotId) {
        String qByPlotId = "SELECT review_id, rating, score, feedback, reviewed_by FROM plot_review WHERE plot_id = ?;";
        return Utils.handleSqlException(new ArrayList<>(), () -> SqlHelper.runQuery(qByPlotId, ps -> {
            ps.setInt(1, plotId);
            ResultSet rs = ps.executeQuery();
            List<PlotReview> reviews = new ArrayList<>();
            while (rs.next()) {
                int reviewId = rs.getInt(1);
                ReviewRating rating = getReviewRating(reviewId, rs.getString(2));
                int score = rs.getInt(3);
                String feedback = rs.getString(4);
                UUID reviewedBy = UUID.fromString(rs.getString(5));

                reviews.add(new PlotReview(reviewId, plotId, rating, score, feedback, reviewedBy));
            }
            return reviews;
        }));
    }

    private @NotNull ReviewRating getReviewRating(int reviewId, @NotNull String ratingString) {
        int accuracyPoints = Integer.parseInt(ratingString.split(",")[0]);
        int blockPalettePoints = Integer.parseInt(ratingString.split(",")[1]);
        Map<ToggleCriteria, Boolean> toggleCriteria = getReviewToggleCriteria(reviewId);
        return new ReviewRating(accuracyPoints, blockPalettePoints, toggleCriteria);
    }

    public boolean updateFeedback(int reviewId, String newFeedback) {
        String qSetFeedback = "UPDATE plot_review SET feedback = ? WHERE review_id = ?;";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qSetFeedback, ps -> {
            ps.setString(1, newFeedback);
            ps.setInt(2, reviewId);
            return ps.executeUpdate() > 0;
        })));
    }

    public PlotReview createReview(@NotNull Plot plot, ReviewRating rating, int score, UUID reviewerUUID) {
        boolean result = DataProvider.PLOT.setMcVersion(plot.getID());
        if (!result) return null;

        // Create Review
        String qInsert = "INSERT INTO plot_review (plot_id, rating, score, reviewed_by) VALUES (?, ?, ?, ?);";
        result = Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qInsert, ps -> {
            ps.setInt(1, plot.getID());
            ps.setString(2, rating.getRatingDatabaseString());
            ps.setInt(3, score);
            ps.setString(4, reviewerUUID.toString());
            return ps.executeUpdate() > 0;
        })));

        if (!result) return null;

        PlotReview review = plot.getLatestReview().orElseThrow();
        Map<ToggleCriteria, Boolean> allToggles = rating.getAllToggles();
        for (Map.Entry<ToggleCriteria, Boolean> criteriaEntry : allToggles.entrySet()) {
            if (!addReviewToggleCriteria(review.getReviewId(), criteriaEntry.getKey(), criteriaEntry.getValue()))
                return null;
        }


        // create feedback notifications
        createReviewNotification(review.getReviewId(), plot.getPlotOwner().getUUID());
        for (Builder builder : plot.getPlotMembers()) {
            createReviewNotification(review.getReviewId(), builder.getUUID());
        }
        return review;
    }

    public boolean removeReview(int reviewId) {
        // remove all review notifications
        List<ReviewNotification> notifications = getReviewNotifications(reviewId);
        for (ReviewNotification notification : notifications) {
            removeReviewNotification(notification.reviewId(), notification.uuid());
        }

        // remove checked toggle criteria
        removeCheckedToggleCriteria(reviewId);

        // remove review
        String qDelete = "DELETE FROM plot_review WHERE review_id = ?;";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qDelete, ps -> {
            ps.setInt(1, reviewId);
            return ps.executeUpdate() > 0;
        })));
    }

    public boolean removeAllReviewsOfPlot(int plotId) {
        String qIdsByPlotId = "SELECT review_id FROM plot_review WHERE plot_id = ?;";
        List<Integer> reviewIds = new ArrayList<>();
        Utils.handleSqlException(() -> SqlHelper.runQuery(qIdsByPlotId, ps -> {
            ps.setInt(1, plotId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                reviewIds.add(rs.getInt(1));
            }
        }));

        boolean successful = true;
        for (int reviewId : reviewIds) {
            successful = successful && removeReview(reviewId);
        }
        return successful;
    }

    // --- Toggle Criteria ---
    public Optional<ToggleCriteria> getToggleCriteria(String criteriaName) {
        return TOGGLE_CRITERIA.stream().filter(c -> c.criteriaName().equals(criteriaName)).findFirst();
    }

    public boolean addToggleCriteria(String criteriaName, boolean isOptional) {
        Optional<ToggleCriteria> criteria = TOGGLE_CRITERIA.stream().filter(t -> t.criteriaName().equals(criteriaName)).findFirst();
        if (criteria.isPresent()) return false;

        String qInsertToggleCriteria = "INSERT INTO review_toggle_criteria (criteria_name, is_optional) VALUES (?, ?);";
        if (!Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qInsertToggleCriteria, ps -> {
            ps.setString(1, criteriaName);
            ps.setBoolean(2, isOptional);
            return ps.executeUpdate() > 0;
        })))) return false;
        TOGGLE_CRITERIA.add(new ToggleCriteria(criteriaName, isOptional));
        return true;
    }

    public boolean removeToggleCriteria(String criteriaName) {
        Optional<ToggleCriteria> criteria = TOGGLE_CRITERIA.stream().filter(t -> t.criteriaName().equals(criteriaName)).findFirst();
        if (criteria.isEmpty()) return false;

        String qDeleteToggleCriteria = "DELETE FROM review_toggle_criteria WHERE criteria_name = ?;";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qDeleteToggleCriteria, ps -> {
            ps.setString(1, criteriaName);
            boolean successful = ps.executeUpdate() > 0;
            if (successful) TOGGLE_CRITERIA.remove(criteria.get());
            return successful;
        })));
    }

    public boolean setToggleCriteriaOptional(String criteriaName, boolean isOptional) {
        Optional<ToggleCriteria> criteria = TOGGLE_CRITERIA.stream().filter(t -> t.criteriaName().equals(criteriaName)).findFirst();
        if (criteria.isEmpty()) return false;

        String qSetToggleCriteriaOptional = "UPDATE review_toggle_criteria SET is_optional = ? WHERE criteria_name = ?;";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qSetToggleCriteriaOptional, ps -> {
            ps.setBoolean(1, isOptional);
            ps.setString(2, criteriaName);
            return ps.executeUpdate() > 0;
        })));
    }

    public List<ToggleCriteria> getAllToggleCriteria() {return TOGGLE_CRITERIA.stream().toList();}

    public List<ToggleCriteria> getBuildTeamToggleCriteria(int buildTeamId) {
        return BUILD_TEAM_TOGGLE_CRITERIA.stream().filter(c -> c.buildTeamId() == buildTeamId).map(BuildTeamToggleCriteria::criteria).toList();
    }

    public boolean assignBuildTeamToggleCriteria(int buildTeamId, ToggleCriteria criteria) {
        Optional<ToggleCriteria> existingCriteria = getBuildTeamToggleCriteria(buildTeamId).stream().filter(t ->
                t.criteriaName().equals(criteria.criteriaName())).findFirst();
        if (existingCriteria.isPresent()) return false;
        String qInsertCriteriaToBuildteam = "INSERT INTO build_team_uses_toggle_criteria (build_team_id, criteria_name) VALUES (?, ?);";
        if (!Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qInsertCriteriaToBuildteam, ps -> {
            ps.setInt(1, buildTeamId);
            ps.setString(2, criteria.criteriaName());
            return ps.executeUpdate() > 0;
        })))) return false;
        BUILD_TEAM_TOGGLE_CRITERIA.add(new BuildTeamToggleCriteria(buildTeamId, criteria));
        return true;
    }

    public boolean removeBuildTeamToggleCriteria(int buildTeamId, ToggleCriteria criteria) {
        Optional<BuildTeamToggleCriteria> existingCriteria = BUILD_TEAM_TOGGLE_CRITERIA.stream().filter(btc ->
                btc.buildTeamId() == buildTeamId && btc.criteria().criteriaName().equals(criteria.criteriaName())).findFirst();
        if (existingCriteria.isEmpty()) return false;
        String qDeleteCriteriaToBuildteam = "DELETE FROM build_team_uses_toggle_criteria WHERE build_team_id = ? AND criteria_name = ?;";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qDeleteCriteriaToBuildteam, ps -> {
            ps.setInt(1, buildTeamId);
            ps.setString(2, criteria.criteriaName());
            boolean successful = ps.executeUpdate() > 0;
            if (successful) BUILD_TEAM_TOGGLE_CRITERIA.remove(existingCriteria.get());
            return successful;
        })));
    }

    public Map<ToggleCriteria, Boolean> getReviewToggleCriteria(int reviewId) {
        String qReviewToggleCriteriasByReviewId = "SELECT criteria_name, is_checked FROM review_contains_toggle_criteria WHERE review_id = ?;";
        return Utils.handleSqlException(new HashMap<>(), () -> SqlHelper.runQuery(qReviewToggleCriteriasByReviewId, ps -> {
            ps.setInt(1, reviewId);
            ResultSet rs = ps.executeQuery();
            HashMap<ToggleCriteria, Boolean> toggleCriteriaList = new HashMap<>();
            while (rs.next()) {
                boolean isChecked = rs.getBoolean(2);
                toggleCriteriaList.put(getToggleCriteria(rs.getString(1)).orElseThrow(), isChecked);
            }
            return toggleCriteriaList;
        }));
    }

    public boolean addReviewToggleCriteria(int reviewId, @NotNull ToggleCriteria toggle, boolean isChecked) {
        String qInsertToggleCriteriaToReview = "INSERT INTO review_contains_toggle_criteria (review_id, criteria_name, is_checked) VALUES (?, ?, ?);";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qInsertToggleCriteriaToReview, ps -> {
            ps.setInt(1, reviewId);
            ps.setString(2, toggle.criteriaName());
            ps.setBoolean(3, isChecked);
            return ps.executeUpdate() > 0;
        })));
    }

    public void removeCheckedToggleCriteria(int reviewId) {
        String qDeleteToggleCriteriaToReview = "DELETE FROM review_contains_toggle_criteria WHERE review_id = ?;";
        Utils.handleSqlException(() -> SqlHelper.runStatement(qDeleteToggleCriteriaToReview, ps -> ps.setInt(1, reviewId)));
    }

    // --- Review Notification ---
    public Optional<ReviewNotification> getReviewNotification(int reviewId, UUID uuid) {
        return NOTIFICATIONS.stream().filter(n -> n.reviewId() == reviewId && n.uuid() == uuid).findFirst();
    }

    public List<ReviewNotification> getReviewNotifications(int reviewId) {
        return NOTIFICATIONS.stream().filter(n -> n.reviewId() == reviewId).toList();
    }

    public List<ReviewNotification> getReviewNotifications(UUID uuid) {
        return NOTIFICATIONS.stream().filter(n -> n.uuid() == uuid).toList();
    }

    public void removeReviewNotification(int reviewId, UUID uuid) {
        Optional<ReviewNotification> notification = getReviewNotification(reviewId, uuid);
        if (notification.isEmpty()) return;

        String qDeleteReviewNotify = "DELETE FROM builder_has_review_notification WHERE review_id = ? AND uuid = ?;";
        Utils.handleSqlException(() -> SqlHelper.runQuery(qDeleteReviewNotify, ps -> {
            ps.setInt(1, reviewId);
            ps.setString(2, uuid.toString());
            if (ps.executeUpdate() > 0) NOTIFICATIONS.remove(notification.get());
        }));
    }

    public void createReviewNotification(int reviewId, UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            PlotUtils.ChatFormatting.sendFeedbackMessage(List.of(new ReviewNotification(reviewId, uuid)), player);
            return;
        }

        String qInsertReviewNotify = "INSERT INTO builder_has_review_notification (review_id, uuid) VALUES (?, ?);";
        Utils.handleSqlException(() -> SqlHelper.runQuery(qInsertReviewNotify, ps -> {
            ps.setInt(1, reviewId);
            ps.setString(2, uuid.toString());
            if (ps.executeUpdate() > 0) NOTIFICATIONS.add(new ReviewNotification(reviewId, uuid));
        }));
    }
}
