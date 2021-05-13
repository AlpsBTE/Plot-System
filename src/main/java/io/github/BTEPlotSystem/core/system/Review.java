/*
 * The MIT License (MIT)
 *
 *  Copyright © 2021, Alps BTE <bte.atchli@gmail.com>
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

package github.BTEPlotSystem.core.system;

import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.utils.enums.Category;
import github.BTEPlotSystem.utils.enums.Status;

import java.sql.*;
import java.util.UUID;

public class Review {

    private final int reviewID;

    public Review(int reviewID) {
        this.reviewID = reviewID;
    }

    public Review(int plotID, UUID reviewer, String rating) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps; ResultSet rs;
            /*rs = con.createStatement().executeQuery("SELECT (t1.id_review + 1) AS firstID FROM reviews t1 " +
                    "WHERE NOT EXISTS (SELECT t2.id_review FROM reviews t2 WHERE t2.id_review = t1.id_review + 1)");*/
            rs = con.createStatement().executeQuery(
                    "SELECT id_review + 1 available_id FROM reviews t WHERE NOT EXISTS (SELECT * FROM reviews WHERE id_review = t.id_review + 1) ORDER BY id_review LIMIT 1"
                    // TODO: Try to remove available_id
            );
            rs.next();
            this.reviewID = rs.getInt(1);

            ps = con.prepareStatement("INSERT INTO reviews (id_review, uuid_reviewer, rating) VALUES (?, ?, ?)");
            ps.setInt(1, reviewID);
            ps.setString(2, reviewer.toString());
            ps.setString(3, rating);
            ps.execute();

            ps = con.prepareStatement("UPDATE plots SET idreview = ? WHERE idplot = ?");
            ps.setInt(1, reviewID);
            ps.setInt(2, plotID);
            ps.executeUpdate();
        }
    }

    public int getReviewID() { return reviewID; }

    public int getPlotID() throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT idplot FROM plots WHERE idreview = ?");
            ps.setInt(1, getReviewID());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    public Builder getReviewer() throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT uuid_reviewer FROM reviews WHERE id_review = ?");
            ps.setInt(1, getReviewID());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return new Builder(UUID.fromString(rs.getString(1)));
        }
    }

    public int getRating(Category category) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT rating FROM reviews WHERE id_review = ?");
            ps.setInt(1, getReviewID());
            ResultSet rs = ps.executeQuery();
            rs.next();

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
                default:
                    return 0;
            }
        }
    }

    public String getFeedback() throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT feedbackText FROM reviews WHERE id_review = ?");
            ps.setInt(1, getReviewID());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getString(1);
        }
    }

    public void setReviewer(UUID reviewer) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE reviews SET uuid_reviewer = ? WHERE id_review = ?");
            ps.setString(1, reviewer.toString());
            ps.setInt(2, getReviewID());
            ps.executeUpdate();
        }
    }

    public void setRating(String ratingFormat) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE reviews SET rating = ? WHERE id_review = ?");
            ps.setString(1, ratingFormat);
            ps.setInt(2, getReviewID());
            ps.executeUpdate();
        }
    }

    public void setFeedback(String feedback) throws SQLException {
        String[] feedbackArr = feedback.split(" ");
        StringBuilder finalFeedback = new StringBuilder();
        int lineLength = 0;
        int lines = 0;

        for (String word : feedbackArr) {
            if((lineLength + word.length()) <= 60) {
                finalFeedback.append((lines == 0 && lineLength == 0) ? "" : " ").append(word);
                lineLength += word.length();
            } else {
                finalFeedback.append("//").append(word);
                lineLength = 0;
                lines++;
            }
        }

        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE reviews SET feedbackText = ? WHERE id_review = ?");
            ps.setString(1, finalFeedback.toString());
            ps.setInt(2, getReviewID());
            ps.executeUpdate();
        }
    }

    public void setFeedbackSent(boolean isSent) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE reviews SET isSent = ? WHERE id_review = ?");
            ps.setInt(1, isSent ? 1 : 0);
            ps.setInt(2, getReviewID());
            ps.executeUpdate();
        }
    }

    public boolean isFeedbackSent() throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT isSent FROM reviews WHERE id_review = ?");
            ps.setInt(1, getReviewID());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) != 0;
        }
    }

    public static void undoReview(Review review) throws SQLException {
        Plot plot = new Plot(review.getPlotID());

        plot.getBuilder().addScore(-plot.getScore());
        plot.getBuilder().addCompletedBuild(-1);
        plot.setScore(-1);
        plot.setStatus(Status.unreviewed);

        if(plot.getBuilder().getFreeSlot() != null) {
            plot.getBuilder().setPlot(plot.getID(), plot.getBuilder().getFreeSlot());
        }

        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM reviews WHERE id_review = ?");
            ps.setInt(1, review.getReviewID());
            ps.execute();

            ps = con.prepareStatement("UPDATE plots SET idreview = DEFAULT(idreview) WHERE idplot = ?");
            ps.setInt(1, review.getPlotID());
            ps.executeUpdate();
        }
    }
}
