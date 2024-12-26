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
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.Component.text;

public class Difficulty {
    private final int ID;

    private PlotDifficulty difficulty;
    private double multiplier;
    private int scoreRequirement;

    public Difficulty(int ID) throws SQLException {
        this.ID = ID;

        try (ResultSet rs = DatabaseConnection.createStatement("SELECT name, multiplier, score_requirment FROM plotsystem_difficulties WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                this.difficulty = PlotDifficulty.valueOf(rs.getString(1));
                this.multiplier = rs.getDouble(2);
                this.scoreRequirement = rs.getInt(3);
            }

            DatabaseConnection.closeResultSet(rs);
        }
    }

    public int getID() {
        return ID;
    }

    public PlotDifficulty getDifficulty() {
        return difficulty;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public int getScoreRequirement() {
        return scoreRequirement;
    }

    public static List<Difficulty> getDifficulties() {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT id FROM plotsystem_difficulties").executeQuery()) {
            List<Difficulty> difficulties = new ArrayList<>();
            while (rs.next()) {
                difficulties.add(new Difficulty(rs.getInt(1)));
            }

            DatabaseConnection.closeResultSet(rs);

            return difficulties;
        } catch (SQLException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
        }
        return new ArrayList<>();
    }

    public static void setMultiplier(int difficultyID, double multiplier) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_difficulties SET multiplier = ? WHERE id = ?")
                .setValue(multiplier).setValue(difficultyID).executeUpdate();
    }

    public static void setScoreRequirement(int difficultyID, int scoreRequirement) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_difficulties SET score_requirment = ? WHERE id = ?")
                .setValue(scoreRequirement).setValue(difficultyID).executeUpdate();
    }
}
