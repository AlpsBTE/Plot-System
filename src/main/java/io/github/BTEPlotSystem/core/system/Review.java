package github.BTEPlotSystem.core.system;

import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.core.system.Builder;
import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.utils.enums.Category;
import github.BTEPlotSystem.utils.enums.Status;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class Review {

    private int reviewID;

    public Review(int reviewID) throws SQLException {
        this.reviewID = reviewID;
    }

    public Review(int plotID, UUID reviewer, String rating) throws SQLException {
        ResultSet rs_reviewID = DatabaseConnection.createStatement().executeQuery("SELECT (t1.id_review + 1) AS firstID FROM reviews t1 " +
                "WHERE NOT EXISTS (SELECT t2.id_review FROM reviews t2 WHERE t2.id_review = t1.id_review + 1)");
        rs_reviewID.next();

        try {
            this.reviewID = rs_reviewID.getInt(1);
        } catch (SQLDataException ex) {
            this.reviewID = 1;
        }

        PreparedStatement ps_reviews = DatabaseConnection.prepareStatement("INSERT INTO reviews (id_review, uuid_reviewer, rating) VALUES (?, ?, ?)");
        ps_reviews.setInt(1, reviewID);
        ps_reviews.setString(2, reviewer.toString());
        ps_reviews.setString(3, rating);

        PreparedStatement ps_plots = DatabaseConnection.prepareStatement("UPDATE plots SET idreview = ? WHERE idplot = '" + plotID + "'");
        ps_plots.setInt(1, reviewID);

        ps_reviews.execute();
        ps_plots.executeUpdate();
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
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT uuid_reviewer FROM reviews WHERE id_review = '" + getReviewID() + "'");
        if(rs.next()) {
            return new Builder(UUID.fromString(rs.getString(1)));
        }
        return null;
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
        rs.next();

        return rs.getString(1);
    }

    public void setReviewer(UUID reviewer) throws SQLException {
        PreparedStatement statement = DatabaseConnection.prepareStatement("UPDATE reviews SET uuid_reviewer = ? WHERE id_review = '" + getReviewID() + "'");
        statement.setString(1, reviewer.toString());
        statement.executeUpdate();
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
        String[] feedbackArr = feedback.split(" ");
        StringBuilder finalFeedback = new StringBuilder();
        int lineLength = 0;

        for (String word : feedbackArr) {
            if((lineLength + word.length()) <= 60) {
                finalFeedback.append(lineLength == 0 ? "" : " ").append(word);
                lineLength += word.length();
            } else {
                finalFeedback.append("//").append(word);
                lineLength = 0;
            }
        }

        PreparedStatement statement = DatabaseConnection.prepareStatement("UPDATE reviews SET feedbackText = ? WHERE id_review = '" + getReviewID() + "'");
        statement.setString(1, finalFeedback.toString());
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

    public static void undoReview(Review review) throws SQLException {
        Plot plot = new Plot(review.getPlotID());

        PreparedStatement ps_review = DatabaseConnection.prepareStatement("DELETE FROM reviews WHERE id_review = '" + review.getReviewID() + "'");
        PreparedStatement ps_plot = DatabaseConnection.prepareStatement("UPDATE plots SET idreview = DEFAULT(idreview) WHERE idplot = '" + plot.getID() + "'");

        plot.getBuilder().addScore(-plot.getScore());
        plot.getBuilder().addCompletedBuild(-1);
        plot.setScore(-1);
        plot.setStatus(Status.unreviewed);

        if(plot.getBuilder().getFreeSlot() != null) {
            plot.getBuilder().setPlot(plot.getID(), plot.getBuilder().getFreeSlot());
        }

        ps_review.execute();
        ps_plot.executeUpdate();
    }
}
