/*
 *  The MIT License (MIT)
 *
 *  Copyright © 2021-2025, Alps BTE <bte.atchli@gmail.com>
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

import com.alpsbte.alpslib.io.database.SqlHelper;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.Difficulty;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.kyori.adventure.text.Component.text;

public class DifficultyProvider {
    protected static final List<Difficulty> DIFFICULTIES = new ArrayList<>();

    public DifficultyProvider() {
        // cache all difficulties
        String qAll = "SELECT difficulty_id, multiplier, score_requirement FROM plot_difficulty;";
        Utils.handleSqlException(() -> SqlHelper.runQuery(qAll, ps -> {
            ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String id = rs.getString(1);
                    double multiplier = rs.getDouble(2);
                    int scoreRequirement = rs.getInt(3);

                    Difficulty difficulty = new Difficulty(PlotDifficulty.valueOf(id), id, multiplier, scoreRequirement);
                    DIFFICULTIES.add(difficulty); // cache all difficulties
                }
        }));
    }

    public List<Difficulty> getDifficulties() {
        return DIFFICULTIES;
    }

    public Optional<Difficulty> getDifficultyById(String id) {
        return DIFFICULTIES.stream().filter(d -> d.getID().equalsIgnoreCase(id)).findAny();
    }

    public Optional<Difficulty> getDifficultyByEnum(PlotDifficulty difficulty) {
        if (difficulty == null) return Optional.empty();
        return DIFFICULTIES.stream().filter(d -> d.getID().equalsIgnoreCase(difficulty.name())).findFirst();
    }

    public boolean setMultiplier(String id, double multiplier) {
        String qSetMulti = "UPDATE plot_difficulty SET multiplier = ? WHERE difficulty_id = ?;";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qSetMulti, ps -> {
            ps.setDouble(1, multiplier);
            ps.setString(2, id);
            return ps.executeUpdate() > 0;
        })));
    }

    public boolean setScoreRequirement(String id, int scoreRequirement) {
        String qSetScoreRequirement = "UPDATE plot_difficulty SET score_requirement = ? WHERE difficulty_id = ?;";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qSetScoreRequirement, ps -> {
            ps.setInt(1, scoreRequirement);
            ps.setString(2, id);
            return ps.executeUpdate() > 0;
        })));
    }

    public boolean builderMeetsRequirements(Builder builder, PlotDifficulty plotDifficulty) {
        Optional<Difficulty> cachedDifficulty = getDifficultyByEnum(plotDifficulty);
        if (cachedDifficulty.isEmpty()) {
            PlotSystem.getPlugin().getComponentLogger().error(text("No database entry for difficulty "
                    + plotDifficulty.toString() + " was found!"));
            return false;
        }

        int playerScore = builder.getScore();
        int scoreRequirement = cachedDifficulty.get().getScoreRequirement();
        return playerScore >= scoreRequirement;
    }
}
