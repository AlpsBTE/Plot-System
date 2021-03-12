package github.BTEPlotSystem.utils;

import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.utils.enums.Category;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

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

    public int getPlotID() throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT idplot FROM plots WHERE idreview = '" + getReviewID() + "'");
        if(rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    public Builder getReviewer() throws SQLException {
        return new Builder(reviewerUUID);
    }

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

    public String getFeedback() throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT feedbackText FROM reviews WHERE id_review = '" + getReviewID() + "'");

        if(rs.next()) {
            return rs.getString("feedbackText");
        }
        return "";
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

    public void setFeedback(String feedback) throws SQLException {
        PreparedStatement statement = DatabaseConnection.prepareStatement("UPDATE reviews SET feedbackText = ? WHERE id_review = '" + getReviewID() + "'");
        statement.setString(1, feedback);
        statement.executeUpdate();
    }

    public void setFeedbackSent(boolean isSent) throws SQLException {
        PreparedStatement statement = DatabaseConnection.prepareStatement("UPDATE reviews SET isSent = ? WHERE id_review = '" + getReviewID() + "'");
        statement.setInt(1, isSent ? 1 : 0);
        statement.executeUpdate();
    }

    public boolean isFeedbackSent() throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT isSent FROM reviews WHERE id_review = '" + getReviewID() + "'");

        if(rs.next()) {
            return rs.getInt("isSent") != 0;
        }

        return false;
    }

    public static Review createReview(int plotID) {
        try {
            ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT idreview FROM plots WHERE idplot = '" + plotID + "'");

            if(!rs.next()) {
                ResultSet rs_reviewID = DatabaseConnection.createStatement().executeQuery("SELECT (t1.id_review + 1) AS firstID FROM reviews t1 " +
                        "WHERE NOT EXISTS (SELECT t2.id_review FROM reviews t2 WHERE t2.id_review = t1.id_review + 1)");
                if(rs_reviewID.next()) {
                    int reviewID = rs_reviewID.getInt(1);

                    PreparedStatement ps_reviews = DatabaseConnection.prepareStatement("INSERT INTO reviews (idreview) VALUES (?)");
                    ps_reviews.setInt(1, reviewID);

                    PreparedStatement ps_plots = DatabaseConnection.prepareStatement("UPDATE reviews SET idreview = ? WHERE idplot = '" + plotID + "'");
                    ps_plots.setInt(1, reviewID);

                    ps_reviews.executeUpdate();
                    ps_plots.executeUpdate();

                    return new Review(reviewID);
                }
            } else {
                return new Review(rs.getInt(1));
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
        return null;
    }
}
