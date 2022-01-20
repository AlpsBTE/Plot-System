package com.alpsbte.plotsystem.core.system;

import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import org.bukkit.Bukkit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

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
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
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
