package com.alpsbte.plotsystem.core.database.providers;

import com.alpsbte.alpslib.io.database.SqlHelper;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Status;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlotProvider {
    private static final String PLOT_SQL_COLUMNS = "plot.plot_id, plot.city_project_id, plot.difficulty_id, " +
            "plot.owner_uuid, plot.status, plot.outline_bounds, plot.last_activity_date, " +
            "plot.plot_version, plot.plot_type";

    public Plot getPlotById(int plotId) {
        String qGet = "SELECT " + PLOT_SQL_COLUMNS + " FROM plot WHERE plot_id = ?;";
        return Utils.handleSqlException(null, () -> SqlHelper.runQuery(qGet, ps -> {
            ps.setInt(1, plotId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null; // No plot found
            return extractPlot(rs);
        }));
    }

    public List<Plot> getPlots(@NotNull Status status) {
        String qAllByStatus = "SELECT " + PLOT_SQL_COLUMNS + " FROM plot WHERE status = ?;";
        return Utils.handleSqlException(List.of(), () -> SqlHelper.runQuery(qAllByStatus, ps -> {
            ps.setString(1, status.name());
            ResultSet rs = ps.executeQuery();
            List<Plot> plots = new ArrayList<>();
            while (rs.next()) {
                plots.add(extractPlot(rs));
            }
            return plots;
        }));
    }

    public List<Plot> getPlots(@NotNull CityProject city, Status @NotNull ... statuses) {
        String query = "SELECT " + PLOT_SQL_COLUMNS + " FROM plot WHERE city_project_id = ?";
        query += statuses.length > 0 ? " AND status IN (" + "?,".repeat(statuses.length - 1) + "?);" : ";";
        String qPlotsWithCityAndStatuses = query;
        return Utils.handleSqlException(List.of(), () -> SqlHelper.runQuery(qPlotsWithCityAndStatuses, ps -> {
            ps.setString(1, city.getID());
            for (int i = 0; i < statuses.length; i++) ps.setString(i + 2, statuses[i].name());
            ResultSet rs = ps.executeQuery();
            List<Plot> plots = new ArrayList<>();
            while (rs.next()) plots.add(extractPlot(rs));
            return plots;
        }));
    }

    public List<Plot> getPlots(@NotNull CityProject city, @NotNull PlotDifficulty plotDifficulty, @NotNull Status status) {
        String qAllByCityDifficultyStatus = "SELECT " + PLOT_SQL_COLUMNS + " FROM plot WHERE city_project_id = ? AND difficulty_id = ? AND status = ?;";
        return Utils.handleSqlException(List.of(), () -> SqlHelper.runQuery(qAllByCityDifficultyStatus, ps -> {
            ps.setString(1, city.getID());
            ps.setString(2, plotDifficulty.name());
            ps.setString(3, status.name());
            ResultSet rs = ps.executeQuery();
            List<Plot> plots = new ArrayList<>();
            while (rs.next()) plots.add(extractPlot(rs));
            return plots;
        }));
    }

    public List<Plot> getPlots(@NotNull List<CityProject> cities, Status... statuses) {
        if (cities.isEmpty()) return List.of();

        String cityPlaceholders = "?,".repeat(cities.size() - 1) + "?";
        String query = "SELECT " + PLOT_SQL_COLUMNS + " FROM plot WHERE city_project_id IN (" + cityPlaceholders + ")";
        query += statuses.length > 0 ? " AND status IN (" + "?,".repeat(statuses.length - 1) + "?);" : ";";
        String qPlotsWithCitiesAndStatuses = query;

        return Utils.handleSqlException(List.of(), () -> SqlHelper.runQuery(qPlotsWithCitiesAndStatuses, ps -> {
            int index = 1;
            for (CityProject city : cities) ps.setString(index++, city.getID());
            for (Status status : statuses) ps.setString(index++, status.name());

            ResultSet rs = ps.executeQuery();
                List<Plot> plots = new ArrayList<>();
                while (rs.next()) plots.add(extractPlot(rs));
                return plots;
        }));
    }

    public List<Plot> getPlots(@NotNull Builder builder, Status @NotNull ... statuses) {
        String query = "SELECT " + PLOT_SQL_COLUMNS + " FROM plot LEFT JOIN builder_is_plot_member pm ON " +
                "plot.plot_id = pm.plot_id WHERE (plot.owner_uuid = ? OR pm.uuid = ?)";
        query += statuses.length > 0 ? "AND plot.status IN (" + "?,".repeat(statuses.length - 1) + "?);" : ";";
        String qPlotsWithBuilderAndStatuses = query;
        return Utils.handleSqlException(List.of(), () -> SqlHelper.runQuery(qPlotsWithBuilderAndStatuses, ps -> {
            String builderUUID = builder.getUUID().toString();
            ps.setString(1, builderUUID);
            ps.setString(2, builderUUID);
            for (int i = 0; i < statuses.length; i++) ps.setString(i + 3, statuses[i].name());

            ResultSet rs = ps.executeQuery();
                List<Plot> plots = new ArrayList<>();
                while (rs.next()) plots.add(extractPlot(rs));
                return plots;
        }));
    }

    private @NotNull Plot extractPlot(@NotNull ResultSet rs) throws SQLException {
        int plotId = rs.getInt("plot_id");
        CityProject cityProject = DataProvider.CITY_PROJECT.getById(rs.getString("city_project_id")).orElseThrow();
        PlotDifficulty difficulty = DataProvider.DIFFICULTY.getDifficultyById(rs.getString("difficulty_id")).orElseThrow().getDifficulty();

        String ownerUUIDString = rs.getString("owner_uuid");
        UUID ownerUUID = ownerUUIDString != null ? UUID.fromString(ownerUUIDString) : null;

        Status status = Status.valueOf(rs.getString("status"));
        String outlineBounds = rs.getString("outline_bounds");
        Date lastActivity = rs.getDate("last_activity_date");

        double version = rs.getDouble("plot_version");
        PlotType type = PlotType.byId(rs.getInt("plot_type"));

        return new Plot(
                plotId,
                cityProject,
                difficulty,
                ownerUUID,
                status,
                outlineBounds,
                lastActivity != null ? lastActivity.toLocalDate() : null,
                version,
                type,
                getPlotMembers(plotId));
    }

    public List<Builder> getPlotMembers(int plotId) {
        String qAllMembers = "SELECT uuid FROM builder_is_plot_member WHERE plot_id = ?;";
        return Utils.handleSqlException(new ArrayList<>(), () -> SqlHelper.runQuery(qAllMembers, ps -> {
            ps.setInt(1, plotId);
            ResultSet rs = ps.executeQuery();
            List<Builder> members = new ArrayList<>();
            while (rs.next()) {
                    members.add(Builder.byUUID(UUID.fromString(rs.getString(1))));
            }
            return members;
        }));
    }
    
    public byte[] getInitialSchematic(int plotId) {
        return getSchematic(plotId, "initial_schematic");
    }

    public byte[] getCompletedSchematic(int plotId) {
        return getSchematic(plotId, "complete_schematic");
    }

    private byte @Nullable [] getSchematic(int plotId, String name) {
        String qName = "SELECT " + name + " FROM plot WHERE plot_id = ?;";
        return Utils.handleSqlException(null, () -> SqlHelper.runQuery(qName, ps -> {
            ps.setInt(1, plotId);
            ResultSet rs = ps.executeQuery();
                if (!rs.next()) return null;
                return rs.getBytes(1);
        }));
    }

    public boolean setCompletedSchematic(int plotId, byte[] completedSchematic) {
        String qSetCompleteSchematic = "UPDATE plot SET complete_schematic = ? WHERE plot_id = ?;";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qSetCompleteSchematic, ps -> {
            ps.setBytes(1, completedSchematic);
            ps.setInt(2, plotId);
            return ps.executeUpdate() > 0;
        })));
    }

    public boolean setPlotOwner(int plotId, UUID ownerUUID) {
        String qSetOwner = "UPDATE plot SET owner_uuid = " + (ownerUUID == null ? "DEFAULT(owner_uuid)" : "?") + " WHERE plot_id = ?;";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qSetOwner, ps -> {
            if (ownerUUID == null) {
                ps.setInt(1, plotId);
            } else {
                ps.setString(1, ownerUUID.toString());
                ps.setInt(2, plotId);
            }
            return ps.executeUpdate() > 0;
        })));
    }

    public boolean setLastActivity(int plotId, LocalDate activityDate) {
        String qSetLastActivityDate = "UPDATE plot SET last_activity_date = ? WHERE plot_id = ?;";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qSetLastActivityDate, ps -> {
            ps.setDate(1, activityDate == null ? null : Date.valueOf(activityDate));
            ps.setInt(2, plotId);
            return ps.executeUpdate() > 0;
        })));
    }

    public boolean setStatus(int plotId, @NotNull Status status) {
        String qSetStatus = "UPDATE plot SET status = ? WHERE plot_id = ?;";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qSetStatus, ps -> {
            ps.setString(1, status.name());
            ps.setInt(2, plotId);
            return ps.executeUpdate() > 0;
        })));
    }

    public boolean setMcVersion(int plotId) {
        String qSetMcVersion = "UPDATE plot SET mc_version = ? WHERE plot_id = ?;";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qSetMcVersion, ps -> {
            ps.setString(1, Bukkit.getMinecraftVersion());
            ps.setInt(2, plotId);
            return ps.executeUpdate() > 0;
        })));
    }

    public boolean setPlotType(int plotId, @NotNull PlotType type) {
        String qSetType = "UPDATE plot SET plot_type = ? WHERE plot_id = ?;";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qSetType, ps -> {
            ps.setInt(1, type.getId());
            ps.setInt(2, plotId);
            return ps.executeUpdate() > 0;
        })));
    }

    public boolean setPasted(int plotId, boolean pasted) {
        String qSetPasted = "UPDATE plot SET is_pasted = ? WHERE plot_id = ?;";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qSetPasted, ps -> {
            ps.setBoolean(1, pasted);
            ps.setInt(2, plotId);
            return ps.executeUpdate() > 0;
        })));
    }

    public boolean deletePlot(int plotId) {
        String qDelete = "DELETE FROM plot WHERE plot_id = ?;";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qDelete, ps -> {
            ps.setInt(1, plotId);
            return ps.executeUpdate() > 0;
        })));
    }

    public boolean addPlotMember(int id, Builder member) {
        String qInsertPlotMember = "INSERT INTO builder_is_plot_member (plot_id, uuid) VALUES (?, ?);";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qInsertPlotMember, ps -> {
            ps.setInt(1, id);
            ps.setString(2, member.getUUID().toString());
            return ps.executeUpdate() > 0;
        })));
    }

    public boolean removePlotMember(int id, Builder member) {
        String qDeletePlotMember = "DELETE FROM builder_is_plot_member WHERE plot_id = ? AND uuid = ?;";
        return Boolean.TRUE.equals(Utils.handleSqlException(false, () -> SqlHelper.runQuery(qDeletePlotMember, ps -> {
            ps.setInt(1, id);
            ps.setString(2, member.getUUID().toString());
            return ps.executeUpdate() > 0;
        })));
    }
}
