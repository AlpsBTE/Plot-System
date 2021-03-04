package github.BTEPlotSystem.utils;

import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.utils.enums.Category;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Review {

    private final int reviewID;
    private final UUID reviewerUUID;

    public Review(int reviewID) throws SQLException {
        this.reviewID = reviewID;

        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT * FROM reviews WHERE id_review = '" + getReviewID() + "'");
        rs.next();

        this.reviewerUUID = UUID.fromString(rs.getString("uuid_reviewer"));
    }

    public int getReviewID() { return reviewID; }

    public Builder getReviewer() throws SQLException { return new Builder(reviewerUUID); }

    // Get rating by category
    public int getRating(Category category) throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT rating FROM reviews WHERE id_review = '" + getReviewID() + "'");

        if(rs.next()) {
            String[] scoreAsString = rs.getString("rating").split(",");

            switch (category) {
                case ACCURACY:
                    return Integer.parseInt(scoreAsString[0]);
                case BLOCKPALETTE:
                    return Integer.parseInt(scoreAsString[1]);
                case DETAILING:
                    return Integer.parseInt(scoreAsString[2]);
                case TECHNIQUE:
                    return Integer.parseInt(scoreAsString[3]);
                case ALL:
                    return Integer.parseInt(scoreAsString[0]) + Integer.parseInt(scoreAsString[1]) + Integer.parseInt(scoreAsString[2]) + Integer.parseInt(scoreAsString[3]);
            }
        }
        return 0;
    }

    /**
     * Set plot rating [Accuracy, Blockpalette, Detailing, Technique]
     *
     * @param ratingFormat Format: 0,0,0,0
     */
    public void setRating(String ratingFormat) throws SQLException {
        PreparedStatement statement = DatabaseConnection.prepareStatement("UPDATE reviews SET rating = ? WHERE id_review = '" + getReviewID() + "'");
        statement.setString(1, ratingFormat);
        statement.executeUpdate();
    }

    public String getFeedback() throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT feedbackText FROM reviews WHERE id_review = '" + getReviewID() + "'");

        if(rs.next()) {
            return rs.getString("feedbackText");
        }

        return "";
    }

    public void setFeedback(String feedback) throws SQLException {
        PreparedStatement statement = DatabaseConnection.prepareStatement("UPDATE reviews SET feedbackText = ? WHERE id_review = '" + getReviewID() + "'");
        statement.setString(1, feedback);
        statement.executeUpdate();
    }

    public boolean isFeedbackSent() throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT isSent FROM reviews WHERE id_review = '" + getReviewID() + "'");

        if(rs.next()) {
            return rs.getInt("isSent") != 0;
        }

        return false;
    }

    public void setFeedbackSent(boolean isSent) throws SQLException {
        PreparedStatement statement = DatabaseConnection.prepareStatement("UPDATE reviews SET isSent = ? WHERE id_review = '" + getReviewID() + "'");
        statement.setInt(1, isSent ? 1 : 0);
        statement.executeUpdate();
    }
}
