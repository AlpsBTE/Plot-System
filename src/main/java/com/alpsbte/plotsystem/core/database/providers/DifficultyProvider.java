package com.alpsbte.plotsystem.core.database.providers;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.Difficulty;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.kyori.adventure.text.Component.text;

public class DifficultyProvider {
    private static final List<Difficulty> cachedDifficulties = new ArrayList<>();

    public DifficultyProvider() {
        // cache all difficulties
        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("SELECT difficulty_id, multiplier, score_requirement FROM difficulty;")) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString(1);
                    double multiplier = rs.getDouble(2);
                    int scoreRequirement = rs.getInt(3);
                    PlotDifficulty plotDifficulty = PlotDifficulty.valueOf(id);

                    Difficulty difficulty = new Difficulty(plotDifficulty, id, multiplier, scoreRequirement);
                    cachedDifficulties.add(difficulty);
                }
            }
        } catch (SQLException ex) {Utils.logSqlException(ex);}
    }

    public List<Difficulty> getDifficulties() {
        return cachedDifficulties;
    }

    public Optional<Difficulty> getDifficultyById(String id) {
        return cachedDifficulties.stream().filter(d -> d.getID().equals(id)).findAny();
    }

    public Optional<Difficulty> getDifficultyByEnum(PlotDifficulty difficulty) {
        return cachedDifficulties.stream().filter(d -> d.getID().equals(difficulty.name())).findFirst();
    }

    public boolean setMultiplier(String id, double multiplier) {
        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("UPDATE difficulty SET multiplier = ? WHERE difficulty_id = ?;")) {
            stmt.setDouble(1, multiplier);
            stmt.setString(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {Utils.logSqlException(ex);}
        return false;
    }

    public boolean setScoreRequirement(String id, int scoreRequirement) {
        try (PreparedStatement stmt = DatabaseConnection.getConnection()
                .prepareStatement("UPDATE difficulty SET score_requirement = ? WHERE difficulty_id = ?;")) {
            stmt.setInt(1, scoreRequirement);
            stmt.setString(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {Utils.logSqlException(ex);}
        return false;
    }

    public boolean builderMeetsRequirements(Builder builder, PlotDifficulty plotDifficulty) {
        Optional<Difficulty> cachedDifficulty = getDifficultyByEnum(plotDifficulty);
        if (cachedDifficulty.isEmpty()) {
            PlotSystem.getPlugin().getComponentLogger().error(text("No database entry for difficulty " + plotDifficulty.name() + " was found!"));
            return false;
        }

        int playerScore = builder.getScore();
        int scoreRequirement = cachedDifficulty.get().getScoreRequirement();
        return playerScore >= scoreRequirement;
    }
}
