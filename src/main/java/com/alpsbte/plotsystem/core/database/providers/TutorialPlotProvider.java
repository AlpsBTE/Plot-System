package com.alpsbte.plotsystem.core.database.providers;

import com.alpsbte.alpslib.io.database.SqlHelper;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import com.alpsbte.plotsystem.utils.Utils;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TutorialPlotProvider {

    private static final int UNASSIGNED_ID = -1;
    private final Map<Integer, TutorialPlot> activePlots = new HashMap<>();

    public synchronized Optional<TutorialPlot> getById(int id) {
        return Optional.ofNullable(activePlots.get(id));
    }

    /**
     * Returns the tutorial plot of a running tutorial, or otherwise the stored progress of the player.
     */
    public Optional<TutorialPlot> getByTutorialId(int tutorialId, String playerUUID) {
        Optional<TutorialPlot> active = getActive(tutorialId, playerUUID);
        if (active.isPresent()) return active;
        return load(tutorialId, playerUUID);
    }

    /**
     * Loads the tutorial plot of the player and assigns it to a free world slot.
     * Release it with {@link #release(TutorialPlot)} when the tutorial stops.
     */
    public Optional<TutorialPlot> claim(int tutorialId, String playerUUID) {
        Optional<TutorialPlot> stored = getByTutorialId(tutorialId, playerUUID);
        if (stored.isEmpty() || stored.get().getId() != UNASSIGNED_ID) return stored;

        synchronized (this) {
            // Another thread may have claimed a slot for this player in the meantime
            Optional<TutorialPlot> active = getActive(tutorialId, playerUUID);
            if (active.isPresent()) return active;

            int slot = 0;
            while (activePlots.containsKey(slot)) slot++;

            TutorialPlot progress = stored.get();
            TutorialPlot plot = new TutorialPlot(slot, tutorialId, playerUUID, progress.getStageID(),
                    progress.isComplete(), progress.getLastActivity());
            activePlots.put(slot, plot);
            return Optional.of(plot);
        }
    }

    /**
     * Frees the world slot of the plot.
     */
    public synchronized void release(TutorialPlot plot) {
        if (plot == null) return;
        activePlots.remove(plot.getId(), plot);
    }

    private synchronized Optional<TutorialPlot> getActive(int tutorialId, String playerUUID) {
        return activePlots.values().stream()
                .filter(t -> t.getTutorialID() == tutorialId && t.getUUID().toString().equals(playerUUID)).findFirst();
    }

    private Optional<TutorialPlot> load(int tutorialId, String playerUUID) {
        String qGet = "SELECT stage_id, is_complete, last_stage_complete_date FROM tutorial WHERE tutorial_id = ? AND uuid = ?;";
        return Utils.handleSqlException(Optional.empty(), () -> SqlHelper.runQuery(qGet, ps -> {
            ps.setInt(1, tutorialId);
            ps.setString(2, playerUUID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int stageId = rs.getInt(1);
                boolean isComplete = rs.getBoolean(2);
                Date lastStageCompleteDate = rs.getDate(3);
                return Optional.of(new TutorialPlot(UNASSIGNED_ID, tutorialId, playerUUID, stageId,
                        isComplete, lastStageCompleteDate != null ? lastStageCompleteDate.toLocalDate() : null));
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
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, tutorialId);
            ps.setString(4, playerUUID);
            return ps.executeUpdate() > 0;
        })));
    }

    public boolean setComplete(int tutorialId, String playerUUID) {
        String qSetComplete = "UPDATE tutorial SET is_complete = ?, last_stage_complete_date = ? WHERE tutorial_id = ? AND uuid = ?;";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qSetComplete, ps -> {
            ps.setBoolean(1, true);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, tutorialId);
            ps.setString(4, playerUUID);
            return ps.executeUpdate() > 0;
        })));
    }
}
