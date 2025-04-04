package com.alpsbte.plotsystem.core.database.providers;

import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.system.review.*;
import com.alpsbte.plotsystem.utils.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        String query = "SELECT plot_id, rating, feedback, score, reviewed_by, review_date FROM plot_review WHERE review_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, reviewId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                int plotId = rs.getInt(1);
                String feedback = rs.getString(3);
                int score = rs.getInt(4);

                String ratingString = rs.getString(2);
                int accuracyPoints = Integer.parseInt(ratingString.split(",")[0]);
                int blockPalettePoints = Integer.parseInt(ratingString.split(",")[1]);
                List<ToggleCriteria> checkedCriteria = getCheckedToggleCriteria(reviewId);
                ReviewRating rating = new ReviewRating(accuracyPoints, blockPalettePoints, checkedCriteria);

                return Optional.of(new PlotReview(reviewId, plotId, rating, score, feedback));
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return Optional.empty();
    }

    public Optional<PlotReview> getLatestReview(int plotId) {
        // TODO: implement
        return null;
    }

    public List<PlotReview> getPlotReviewHistory(int plotId) {
        // TODO: implement
        return List.of();
    }

    public boolean updateFeedback(int reviewId, String newFeedback) {
        // TODO: implement
        return false;
    }

    public boolean createReview(ReviewRating rating, int score, UUID reviewerUUID, boolean isRejected) {
        // TODO: implement
        // also create feedback notification
        return false;
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

    public List<ToggleCriteria> getCheckedToggleCriteria(int reviewId) {
        List<ToggleCriteria> toggleCriteriaList = new ArrayList<>();
        String query = "SELECT criteria_name FROM review_contains_toggle_criteria WHERE review_id = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, reviewId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    toggleCriteriaList.add(getToggleCriteria(rs.getString(1)).orElseThrow());
                }
            }
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return toggleCriteriaList;
    }

    public List<ToggleCriteria> getBuildTeamToggleCriteria(int buildTeamId) {
        return cachedBuildTeamToggleCriteria.stream().filter(c -> c.getBuildTeamId() == buildTeamId).map(BuildTeamToggleCriteria::getCriteria).toList();
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
}
