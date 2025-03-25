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

import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import com.alpsbte.plotsystem.utils.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class TutorialPlotProvider {
    public static final HashMap<TutorialPlot, Integer> tutorialPlots = new HashMap<>();
    public static final LinkedList<Integer> freeTutorialPlotIds = new LinkedList<>();

    public Optional<TutorialPlot> getById(int id) {
        return tutorialPlots.keySet().stream().filter(t -> tutorialPlots.get(t) == id).findFirst();
    }

    public Optional<TutorialPlot> getByTutorialId(int tutorialId, String playerUUID) {
        Optional<TutorialPlot> tutorialPlot = tutorialPlots.keySet().stream()
                .filter(t -> t.getTutorialID() == tutorialId && t.getUUID().toString().equals(playerUUID)).findFirst();

        if (tutorialPlot.isEmpty()) {
            String query = "SELECT stage_id, is_complete, last_stage_complete_date FROM " +
                    "tutorial WHERE tutorial_id = ? AND uuid = ?;";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, tutorialId);
                stmt.setString(2, playerUUID);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int plotId = freeTutorialPlotIds.isEmpty() ? 0 : freeTutorialPlotIds.poll();
                        TutorialPlot newTutorialPlot = new TutorialPlot(plotId, tutorialId, playerUUID, rs.getInt(1),
                                rs.getBoolean(2), rs.getDate(3).toLocalDate());
                        tutorialPlots.put(newTutorialPlot, plotId);

                        return Optional.of(newTutorialPlot);
                    }
                }
            } catch (SQLException ex) {
                Utils.logSqlException(ex);
            }
        }
        return tutorialPlot;
    }

    public boolean add(int tutorialId, String playerUUID) {
        if (getByTutorialId(tutorialId, playerUUID).isPresent()) return false;

        String query = "INSERT INTO tutorial (tutorial_id, uuid) VALUES (?, ?);";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, tutorialId);
            stmt.setString(2, playerUUID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public boolean setStageId(int tutorialId, String playerUUID, int stageId) {
        String query = "UPDATE tutorial SET stage_id = ?, last_stage_complete_date = ? WHERE tutorial_id = ? AND uuid = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, stageId);
            stmt.setObject(2, LocalDate.now());
            stmt.setInt(3, tutorialId);
            stmt.setString(4, playerUUID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }

    public boolean setComplete(int tutorialId, String playerUUID) {
        String query = "UPDATE tutorial SET is_complete = ?, last_stage_complete_date = ? WHERE tutorial_id = ? AND uuid = ?;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setBoolean(1, true);
            stmt.setObject(2, LocalDate.now());
            stmt.setInt(3, tutorialId);
            stmt.setString(4, playerUUID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            Utils.logSqlException(ex);
        }
        return false;
    }
}
