package com.alpsbte.plotsystem.core.database.providers;

import com.alpsbte.alpslib.io.database.SqlHelper;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import com.alpsbte.plotsystem.utils.Utils;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

public class TutorialPlotProvider {
    public static final Map<TutorialPlot, Integer> tutorialPlots = new HashMap<>();
    public static final Deque<Integer> freeTutorialPlotIds = new LinkedList<>();

    public Optional<TutorialPlot> getById(int id) {
        return tutorialPlots.keySet().stream().filter(t -> tutorialPlots.get(t) == id).findFirst();
    }

    public Optional<TutorialPlot> getByTutorialId(int tutorialId, String playerUUID) {
        Optional<TutorialPlot> tutorialPlot = tutorialPlots.keySet().stream()
                .filter(t -> t.getTutorialID() == tutorialId && t.getUUID().toString().equals(playerUUID)).findFirst();

        if (tutorialPlot.isPresent()) return tutorialPlot;

        String qGet = "SELECT stage_id, is_complete, last_stage_complete_date FROM tutorial WHERE tutorial_id = ? AND uuid = ?;";
        return Utils.handleSqlException(tutorialPlot, () -> SqlHelper.runQuery(qGet, ps -> {
            ps.setInt(1, tutorialId);
            ps.setString(2, playerUUID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int plotId = freeTutorialPlotIds.isEmpty() ? 0 : freeTutorialPlotIds.poll();
                int stageId = rs.getInt(1);
                boolean isComplete = rs.getBoolean(2);
                Date lastStageCompleteDate = rs.getDate(3);
                TutorialPlot newTutorialPlot = new TutorialPlot(plotId, tutorialId, playerUUID, stageId,
                        isComplete, lastStageCompleteDate != null ? lastStageCompleteDate.toLocalDate() : null);
                tutorialPlots.put(newTutorialPlot, plotId);

                return Optional.of(newTutorialPlot);
            }
            return Optional.empty();
        }));

    }

    public boolean add(int tutorialId, String playerUUID) {
        if (getByTutorialId(tutorialId, playerUUID).isPresent()) return false;

        String qInsert = "INSERT INTO tutorial (tutorial_id, uuid) VALUES (?, ?);";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qInsert, ps -> {
            ps.setInt(1, tutorialId);
            ps.setString(2, playerUUID);
            return ps.executeUpdate() > 0;
        })));
    }

    public boolean setStageId(int tutorialId, String playerUUID, int stageId) {
        String qSetStage = "UPDATE tutorial SET stage_id = ?, last_stage_complete_date = ? WHERE tutorial_id = ? AND uuid = ?;";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qSetStage, ps -> {
            ps.setInt(1, stageId);
            ps.setObject(2, LocalDate.now());
            ps.setInt(3, tutorialId);
            ps.setString(4, playerUUID);
            return ps.executeUpdate() > 0;
        })));
    }

    public boolean setComplete(int tutorialId, String playerUUID) {
        String qSetComplete = "UPDATE tutorial SET is_complete = ?, last_stage_complete_date = ? WHERE tutorial_id = ? AND uuid = ?;";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qSetComplete, ps -> {
            ps.setBoolean(1, true);
            ps.setObject(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, tutorialId);
            ps.setString(4, playerUUID);
            return ps.executeUpdate() > 0;
        })));
    }
}
