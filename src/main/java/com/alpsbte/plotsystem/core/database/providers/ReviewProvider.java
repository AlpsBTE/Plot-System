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

package com.alpsbte.plotsystem.core.database.providers;

import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.core.system.review.*;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;

public class ReviewProvider {
    private static final List<ReviewNotification> cachedNotifications = new ArrayList<>();
    private static final List<ToggleCriteria> cachedToggleCriteria = new ArrayList<>();
    private static final List<BuildTeamToggleCriteria> cachedBuildTeamToggleCriteria = new ArrayList<>();

    public ReviewProvider() {
        // cache all review notifications
        String query = "SELECT review_id, uuid FROM builder_has_review_notification;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cachedNotifications.add(new ReviewNotification(rs.getInt(1), UUID.fromString(rs.getString(2))));
                }
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }

        // cache all toggle criteria
        query = "SELECT criteria_name, is_optional FROM review_toggle_criteria;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cachedToggleCriteria.add(new ToggleCriteria(rs.getString(1), rs.getBoolean(2)));
                }
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }

        // cache all build team toggle criteria
        query = "SELECT criteria_name, build_team_id FROM build_team_uses_toggle_criteria;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String criteriaName = rs.getString(1);
                    ToggleCriteria toggle = cachedToggleCriteria.stream().filter(c -> c.getCriteriaName().equals(criteriaName)).findFirst().orElseThrow();
                    cachedBuildTeamToggleCriteria.add(new BuildTeamToggleCriteria(rs.getInt(2), toggle));
                }
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
    }

    public Optional<PlotReview> getReview(int reviewId) {
        String query = "SELECT plot_id, rating, score, feedback, reviewed_by FROM plot_review WHERE review_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, reviewId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                int plotId = rs.getInt(1);
                ReviewRating rating = getReviewRating(reviewId, rs.getString(2));
                int score = rs.getInt(3);
                String feedback = rs.getString(4);
                UUID reviewedBy = UUID.fromString(rs.getString(5));

                return Optional.of(new PlotReview(reviewId, plotId, rating, score, feedback, reviewedBy));
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return Optional.empty();
    }

    public Optional<PlotReview> getLatestReview(int plotId) {
        String query = "SELECT review_id, rating, score, feedback, reviewed_by FROM plot_review " +
                "WHERE plot_id = ? ORDER BY review_date DESC LIMIT 1;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, plotId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                int reviewId = rs.getInt(1);
                ReviewRating rating = getReviewRating(reviewId, rs.getString(2));
                int score = rs.getInt(3);
                String feedback = rs.getString(4);
                UUID reviewedBy = UUID.fromString(rs.getString(5));

                return Optional.of(new PlotReview(reviewId, plotId, rating, score, feedback, reviewedBy));
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return Optional.empty();
    }

    public List<PlotReview> getPlotReviewHistory(int plotId) {
        List<PlotReview> reviews = new ArrayList<>();
        String query = "SELECT review_id, rating, score, feedback, reviewed_by FROM plot_review WHERE plot_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, plotId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int reviewId = rs.getInt(1);
                    ReviewRating rating = getReviewRating(reviewId, rs.getString(2));
                    int score = rs.getInt(3);
                    String feedback = rs.getString(4);
                    UUID reviewedBy = UUID.fromString(rs.getString(5));

                    reviews.add(new PlotReview(reviewId, plotId, rating, score, feedback, reviewedBy));
                }
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return reviews;
    }

    private ReviewRating getReviewRating(int reviewId, String ratingString) {
        int accuracyPoints = Integer.parseInt(ratingString.split(",")[0]);
        int blockPalettePoints = Integer.parseInt(ratingString.split(",")[1]);
        HashMap<ToggleCriteria, Boolean> toggleCriteria = getReviewToggleCriteria(reviewId);
        return new ReviewRating(accuracyPoints, blockPalettePoints, toggleCriteria);
    }

    public boolean updateFeedback(int reviewId, String newFeedback) {
        String query = "UPDATE plot_review SET feedback = ? WHERE review_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newFeedback);
            stmt.setInt(2, reviewId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public PlotReview createReview(Plot plot, ReviewRating rating, int score, UUID reviewerUUID) {
        boolean result = DataProvider.PLOT.setMcVersion(plot.getID());
        if (!result) return null;

        // Create Review
        String query = "INSERT INTO plot_review (plot_id, rating, score, split_score, reviewed_by) " +
                "VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, plot.getID());
            stmt.setString(2, rating.getRatingDatabaseString());
            stmt.setInt(3, score);
            stmt.setString(4, reviewerUUID.toString());
            result = stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }

        if (!result) return null;

        PlotReview review = plot.getLatestReview().orElseThrow();
        HashMap<ToggleCriteria, Boolean> allToggles = rating.getAllToggles();
        for (ToggleCriteria criteria : allToggles.keySet()) {
            if (!addReviewToggleCriteria(review.getReviewId(), criteria, allToggles.get(criteria)))
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
            removeReviewNotification(notification.getReviewId(), notification.getUuid());
        }

        // remove checked toggle criteria
        removeCheckedToggleCriteria(reviewId);

        // remove review
        String query = "DELETE FROM plot_review WHERE review_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, reviewId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public boolean removeAllReviewsOfPlot(int plotId) {
        List<Integer> reviewIds = new ArrayList<>();
        String query = "SELECT review_id FROM plot_review WHERE plot_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, plotId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) reviewIds.add(rs.getInt(1));
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }

        boolean successful = true;
        for (int reviewId : reviewIds) {
            successful = successful && removeReview(reviewId);
        }
        return successful;
    }

    // --- Toggle Criteria ---
    public Optional<ToggleCriteria> getToggleCriteria(String criteriaName) {
        return cachedToggleCriteria.stream().filter(c -> c.getCriteriaName().equals(criteriaName)).findFirst();
    }

    public List<ToggleCriteria> getBuildTeamToggleCriteria(int buildTeamId) {
        return cachedBuildTeamToggleCriteria.stream().filter(c -> c.getBuildTeamId() == buildTeamId).map(BuildTeamToggleCriteria::getCriteria).toList();
    }

    public HashMap<ToggleCriteria, Boolean> getReviewToggleCriteria(int reviewId) {
        HashMap<ToggleCriteria, Boolean> toggleCriteriaList = new HashMap<>();
        String query = "SELECT criteria_name, is_checked FROM review_contains_toggle_criteria WHERE review_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, reviewId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    boolean isChecked = rs.getBoolean(2);
                    toggleCriteriaList.put(getToggleCriteria(rs.getString(1)).orElseThrow(), isChecked);
                }
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return toggleCriteriaList;
    }

    public boolean addReviewToggleCriteria(int reviewId, ToggleCriteria toggle, boolean isChecked) {
        String query = "INSERT INTO review_contains_toggle_criteria (review_id, criteria_name, is_checked) " +
                "VALUES (?, ?, ?);";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, reviewId);
            stmt.setString(2, toggle.getCriteriaName());
            stmt.setBoolean(3, isChecked);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public boolean removeCheckedToggleCriteria(int reviewId) {
        String query = "DELETE FROM review_contains_toggle_criteria WHERE review_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, reviewId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    // --- Review Notification ---
    public Optional<ReviewNotification> getReviewNotification(int reviewId, UUID uuid) {
        return cachedNotifications.stream().filter(n -> n.getReviewId() == reviewId && n.getUuid() == uuid).findFirst();
    }

    public List<ReviewNotification> getReviewNotifications(int reviewId) {
        return cachedNotifications.stream().filter(n -> n.getReviewId() == reviewId).toList();
    }

    public List<ReviewNotification> getReviewNotifications(UUID uuid) {
        return cachedNotifications.stream().filter(n -> n.getUuid() == uuid).toList();
    }

    public boolean removeReviewNotification(int reviewId, UUID uuid) {
        Optional<ReviewNotification> notification = getReviewNotification(reviewId, uuid);
        if (notification.isEmpty()) return false;

        String query = "DELETE FROM builder_has_review_notification WHERE review_id = ? AND uuid = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, reviewId);
            stmt.setString(2, uuid.toString());
            boolean result = stmt.executeUpdate() > 0;
            if (result) cachedNotifications.remove(notification.get());
            return result;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public void createReviewNotification(int reviewId, UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            PlotUtils.ChatFormatting.sendFeedbackMessage(List.of(new ReviewNotification(reviewId, uuid)), player);
            return;
        }

        String query = "INSERT INTO builder_has_review_notification (review_id, uuid) VALUES (?, ?);";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, reviewId);
            stmt.setString(2, uuid.toString());
            boolean result = stmt.executeUpdate() > 0;
            if (result) cachedNotifications.add(new ReviewNotification(reviewId, uuid));
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
    }
}
