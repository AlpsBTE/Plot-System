/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.core.system;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.enums.Category;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.FTPManager;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

public class Review {
    private final int reviewID;

    public Review(int reviewID) {
        this.reviewID = reviewID;
    }

    public Review(int plotID, UUID reviewer, String rating) throws SQLException {
        this.reviewID = DatabaseConnection.getTableID("plotsystem_reviews");

        DatabaseConnection.createStatement("INSERT INTO plotsystem_reviews (id, reviewer_uuid, rating, review_date, feedback) VALUES (?, ?, ?, ?, ?)")
                .setValue(this.reviewID)
                .setValue(reviewer.toString())
                .setValue(rating)
                .setValue(java.sql.Date.valueOf(java.time.LocalDate.now()))
                .setValue("No Feedback")
                .executeUpdate();

        DatabaseConnection.createStatement("UPDATE plotsystem_plots SET review_id = ? WHERE id = ?")
                .setValue(this.reviewID)
                .setValue(plotID)
                .executeUpdate();
    }

    public int getReviewID() {
        return reviewID;
    }

    public int getPlotID() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT id FROM plotsystem_plots WHERE review_id = ?")
                .setValue(this.reviewID).executeQuery()) {

            if (rs.next()) {
                int i = rs.getInt(1);
                DatabaseConnection.closeResultSet(rs);
                return i;
            }

            DatabaseConnection.closeResultSet(rs);
            return 0;
        }
    }

    public Builder getReviewer() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT reviewer_uuid FROM plotsystem_reviews WHERE id = ?")
                .setValue(this.reviewID).executeQuery()) {

            if (rs.next()) {
                String s = rs.getString(1);
                DatabaseConnection.closeResultSet(rs);
                return Builder.byUUID(UUID.fromString(s));
            }

            DatabaseConnection.closeResultSet(rs);
            return null;
        }
    }

    public int getRating(Category category) throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT rating FROM plotsystem_reviews WHERE id = ?")
                .setValue(this.reviewID).executeQuery()) {

            if (rs.next()) {
                String[] scoreAsString = rs.getString("rating").split(",");
                DatabaseConnection.closeResultSet(rs);

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

            DatabaseConnection.closeResultSet(rs);
            return 0;
        }
    }

    public String getFeedback() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT feedback FROM plotsystem_reviews WHERE id = ?")
                .setValue(this.reviewID).executeQuery()) {

            if (rs.next()) {
                String s = rs.getString(1);
                DatabaseConnection.closeResultSet(rs);
                return s;
            }

            DatabaseConnection.closeResultSet(rs);
            return null;
        }
    }

    public Date getReviewDate() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT review_date FROM plotsystem_reviews WHERE id = ?")
                .setValue(this.reviewID).executeQuery()) {

            if (rs.next()) {
                Date d = rs.getDate(1);
                DatabaseConnection.closeResultSet(rs);
                return d;
            }

            DatabaseConnection.closeResultSet(rs);
            return null;
        }
    }

    public void setReviewer(UUID reviewer) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_reviews SET reviewer_uuid = ? WHERE id = ?")
                .setValue(reviewer.toString()).setValue(this.reviewID).executeUpdate();
    }

    public void setRating(String ratingFormat) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_reviews SET rating = ? WHERE id = ?")
                .setValue(ratingFormat).setValue(this.reviewID).executeUpdate();
    }

    public void setFeedback(String feedback) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_reviews SET feedback = ? WHERE id = ?")
                .setValue(feedback).setValue(this.reviewID).executeUpdate();
    }

    public void setFeedbackSent(boolean isSent) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_reviews SET sent = ? WHERE id = ?")
                .setValue(isSent ? 1 : 0).setValue(this.reviewID).executeUpdate();
    }

    public void setReviewDate() throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_reviews SET review_date = ? WHERE id = ?")
                .setValue(Date.valueOf(LocalDate.now())).setValue(this.reviewID).executeUpdate();
    }

    public boolean isFeedbackSent() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT sent FROM plotsystem_reviews WHERE id = ?")
                .setValue(this.reviewID).executeQuery()) {

            if (rs.next()) {
                int i = rs.getInt(1);
                DatabaseConnection.closeResultSet(rs);
                return i != 0;
            }

            DatabaseConnection.closeResultSet(rs);
            return false;
        }
    }

    public static void undoReview(Review review) {
        Bukkit.getScheduler().runTaskAsynchronously(PlotSystem.getPlugin(), () -> {
            try {
                Plot plot = new Plot(review.getPlotID());

                for (Builder member : plot.getPlotMembers()) {
                    member.addScore(-plot.getSharedScore());
                    member.addCompletedBuild(-1);

                    if (member.getFreeSlot() != null) {
                        member.setPlot(plot.getID(), member.getFreeSlot());
                    }
                }

                plot.getPlotOwner().addScore(-plot.getSharedScore());
                plot.getPlotOwner().addCompletedBuild(-1);
                plot.setTotalScore(-1);
                plot.setStatus(Status.unreviewed);
                plot.setPasted(false);

                if (plot.getPlotOwner().getFreeSlot() != null) {
                    plot.getPlotOwner().setPlot(plot.getID(), plot.getPlotOwner().getFreeSlot());
                }

                int cityId = plot.getCity().getID();
                Server plotServer = plot.getCity().getCountry().getServer();
                boolean hasFTPConfiguration = plotServer.getFTPConfiguration() != null;
                Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
                    plot.getWorld().loadWorld();

                    try {
                        Files.deleteIfExists(plot.getCompletedSchematic().toPath());

                        if (hasFTPConfiguration) {
                            FTPManager.deleteSchematic(FTPManager.getFTPUrl(plotServer, cityId), plot.getID() + ".schem");
                            FTPManager.deleteSchematic(FTPManager.getFTPUrl(plotServer, cityId), plot.getID() + ".schematic");
                        }
                    } catch (IOException | SQLException | URISyntaxException ex) {
                        PlotSystem.getPlugin().getComponentLogger().error(text("An error occurred while undoing review!"), ex);
                    }

                    plot.getWorld().unloadWorld(true);
                });

                DatabaseConnection.createStatement("UPDATE plotsystem_plots SET review_id = DEFAULT(review_id) WHERE id = ?")
                        .setValue(review.getPlotID()).executeUpdate();

                DatabaseConnection.createStatement("DELETE FROM plotsystem_reviews WHERE id = ?")
                        .setValue(review.reviewID).executeUpdate();
            } catch (SQLException ex) {
                PlotSystem.getPlugin().getComponentLogger().error(text("An error occurred while undoing review!"), ex);
            }
        });
    }
}
