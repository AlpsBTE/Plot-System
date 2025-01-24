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

package com.alpsbte.plotsystem.core.system.plot;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.core.system.Review;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.core.system.plot.world.CityPlotWorld;
import com.alpsbte.plotsystem.core.system.plot.world.OnePlotWorld;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Slot;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.FTPManager;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.text;

public class Plot extends AbstractPlot {
    private CityProject city;
    private CityPlotWorld cityPlotWorld;

    public Plot(int id) {
        super(id);
    }

    public CityProject getCity() throws SQLException {
        if (this.city != null)
            return this.city;

        try (ResultSet rs = DatabaseConnection.createStatement("SELECT city_project_id FROM plotsystem_plots WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                int i = rs.getInt(1);
                DatabaseConnection.closeResultSet(rs);
                CityProject cityProject = new CityProject(i);

                this.city = cityProject;

                return cityProject;
            }

            DatabaseConnection.closeResultSet(rs);
            return null;
        }
    }

    public PlotDifficulty getDifficulty() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT difficulty_id FROM plotsystem_plots WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                int i = rs.getInt(1);
                DatabaseConnection.closeResultSet(rs);
                return PlotDifficulty.values()[i - 1];
            }

            DatabaseConnection.closeResultSet(rs);
            return null;
        }
    }

    @Override
    public Builder getPlotOwner() throws SQLException {
        if (plotOwner != null)
            return plotOwner;

        if (getStatus() != Status.unclaimed) {
            try (ResultSet rs = DatabaseConnection.createStatement("SELECT owner_uuid FROM plotsystem_plots WHERE id = ?")
                    .setValue(this.ID).executeQuery()) {

                if (rs.next()) {
                    String s = rs.getString(1);
                    DatabaseConnection.closeResultSet(rs);

                    plotOwner = Builder.byUUID(UUID.fromString(s));

                    return plotOwner;
                }

                DatabaseConnection.closeResultSet(rs);
            }
        }
        return null;
    }

    public void setPlotOwner(String UUID) throws SQLException {
        if (UUID == null) {
            DatabaseConnection.createStatement("UPDATE plotsystem_plots SET owner_uuid = DEFAULT(owner_uuid) WHERE id = ?")
                    .setValue(this.ID).executeUpdate();
        } else {
            DatabaseConnection.createStatement("UPDATE plotsystem_plots SET owner_uuid = ? WHERE id = ?")
                    .setValue(UUID).setValue(this.ID).executeUpdate();
        }

        plotOwner = null;
    }

    public List<Builder> getPlotMembers() throws SQLException {
        List<Builder> builders = new ArrayList<>();

        try (ResultSet rs = DatabaseConnection.createStatement("SELECT member_uuids FROM plotsystem_plots WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                String members = rs.getString(1);
                if (!rs.wasNull()) {
                    String[] uuidMembers = members.split(",");

                    for (String uuid : uuidMembers) {
                        builders.add(Builder.byUUID(UUID.fromString(uuid)));
                    }
                }
            }

            DatabaseConnection.closeResultSet(rs);
        }
        return builders;
    }

    public void setPlotMembers(@NotNull List<Builder> plotMembers) throws SQLException {
        // Convert plot member list to string
        String plotMemberAsString = plotMembers.stream().map(member -> member.getUUID().toString()).collect(Collectors.joining(","));

        if (!plotMembers.isEmpty()) {
            DatabaseConnection.createStatement("UPDATE plotsystem_plots SET member_uuids = ? WHERE id = ?")
                    .setValue(plotMemberAsString).setValue(this.ID).executeUpdate();
        } else {
            DatabaseConnection.createStatement("UPDATE plotsystem_plots SET member_uuids = DEFAULT(member_uuids) WHERE id = ?")
                    .setValue(this.ID).executeUpdate();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends PlotWorld> T getWorld() {
        try {
            if (getVersion() <= 2 || getPlotType().hasOnePlotPerWorld()) {
                if (onePlotWorld == null) onePlotWorld = new OnePlotWorld(this);
                return (T) onePlotWorld;
            } else {
                if (cityPlotWorld == null) cityPlotWorld = new CityPlotWorld(this);
                return (T) cityPlotWorld;
            }
        } catch (SQLException ex) {Utils.logSqlException(ex);}
        return null;
    }

    @Override
    public List<BlockVector2> getOutline() throws SQLException, IOException {
        if (outline != null)
            return this.outline;

        try (ResultSet rs = DatabaseConnection.createStatement("SELECT outline FROM plotsystem_plots WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            List<BlockVector2> pointVectors = new ArrayList<>();
            if (rs.next()) {
                String points = rs.getString(1);
                pointVectors = getOutlinePoints((rs.wasNull() || points.isEmpty() || getVersion() <= 2) ? null : points);
            }

            DatabaseConnection.closeResultSet(rs);
            return pointVectors;
        }
    }

    @Override
    public Date getLastActivity() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT last_activity FROM plotsystem_plots WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                Date d = rs.getDate(1);
                DatabaseConnection.closeResultSet(rs);
                return d;
            }

            DatabaseConnection.closeResultSet(rs);

            return null;
        }
    }

    @Override
    public void setLastActivity(boolean setNull) throws SQLException {
        if (setNull) {
            DatabaseConnection.createStatement("UPDATE plotsystem_plots SET last_activity = DEFAULT(last_activity) WHERE id = ?")
                    .setValue(this.ID).executeUpdate();
        } else {
            DatabaseConnection.createStatement("UPDATE plotsystem_plots SET last_activity = ? WHERE id = ?")
                    .setValue(java.sql.Date.valueOf(LocalDate.now())).setValue(this.ID).executeUpdate();
        }
    }

    @Override
    public Status getStatus() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT status FROM plotsystem_plots WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                String s = rs.getString(1);
                DatabaseConnection.closeResultSet(rs);
                return Status.valueOf(s);
            }

            DatabaseConnection.closeResultSet(rs);
            return null;
        }
    }

    @Override
    public void setStatus(@NotNull Status status) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_plots SET status = ? WHERE id = ?")
                .setValue(status.name()).setValue(this.ID).executeUpdate();
    }

    public int getTotalScore() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT score FROM plotsystem_plots WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                int score = rs.getInt(1);
                if (!rs.wasNull()) {
                    DatabaseConnection.closeResultSet(rs);
                    return score;
                }
            }

            DatabaseConnection.closeResultSet(rs);

            return -1;
        }
    }

    public void setTotalScore(int score) throws SQLException {
        if (score == -1) {
            DatabaseConnection.createStatement("UPDATE plotsystem_plots SET score = DEFAULT(score) WHERE id = ?")
                    .setValue(this.ID).executeUpdate();
        } else {
            DatabaseConnection.createStatement("UPDATE plotsystem_plots SET score = ? WHERE id = ?")
                    .setValue(score).setValue(this.ID).executeUpdate();
        }
    }

    public int getSharedScore() throws SQLException {
        int score = getTotalScore();
        if (score != -1 && !getPlotMembers().isEmpty()) {
            return (int) Math.floor(score / (getPlotMembers().size() + 1d));
        }
        return score;
    }

    @Override
    public PlotType getPlotType() throws SQLException {
        if (plotType != null) return plotType;

        try (ResultSet rs = DatabaseConnection.createStatement("SELECT type FROM plotsystem_plots WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                int typeId = rs.getInt(1);
                DatabaseConnection.closeResultSet(rs);

                plotType = PlotType.byId(typeId);
                return plotType;
            }

            DatabaseConnection.closeResultSet(rs);
            return null;
        }
    }

    public void setPlotType(@NotNull PlotType type) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_plots SET type = ? WHERE id = ?")
                .setValue(type.ordinal()).setValue(this.ID).executeUpdate();
        plotType = type;
    }

    @Override
    public double getVersion() {
        if (plotVersion != -1) return plotVersion;

        try (ResultSet rs = DatabaseConnection.createStatement("SELECT version FROM plotsystem_plots WHERE id = ?")
                .setValue(this.ID).executeQuery()) {

            if (rs.next()) {
                double version = rs.getDouble(1);
                if (!rs.wasNull()) {
                    plotVersion = version;
                } else {
                    plotVersion = 2; // Plot version was implemented since v3, so we assume that the plot is v2.
                }

                DatabaseConnection.closeResultSet(rs);
                return plotVersion;
            }

            DatabaseConnection.closeResultSet(rs);
        } catch (SQLException ex) {Utils.logSqlException(ex);}
        return PLOT_VERSION;
    }

    @Override
    public File getOutlinesSchematic() {
        return getSchematicFile(String.valueOf(getID()));
    }

    @Override
    public File getEnvironmentSchematic() {
        return getSchematicFile(getID() + "-env");
    }

    @Override
    protected File getSchematicFile(String fileName) {
        try {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    File file = Paths.get(PlotUtils.getDefaultSchematicPath(), String.valueOf(getCity().getCountry().getServer().getID()), String.valueOf(getCity().getID()), fileName + PlotUtils.SCHEM_ENDING).toFile();

                    if (!file.exists()) {
                        // if .schem doesn't exist, it looks for old .schematic format for backwards compatibility
                        file = Paths.get(PlotUtils.getDefaultSchematicPath(), String.valueOf(getCity().getCountry().getServer().getID()), String.valueOf(getCity().getID()), fileName + PlotUtils.SCHEMATIC_ENDING).toFile();
                    }

                    if (!file.exists() && getCity().getCountry().getServer().getFTPConfiguration() != null && !FTPManager.downloadSchematic(FTPManager.getFTPUrl(getCity().getCountry().getServer(), getCity().getID()), file)) {
                        file = Paths.get(PlotUtils.getDefaultSchematicPath(), String.valueOf(getCity().getCountry().getServer().getID()), String.valueOf(getCity().getID()), fileName + PlotUtils.SCHEM_ENDING).toFile();
                        FTPManager.downloadSchematic(FTPManager.getFTPUrl(getCity().getCountry().getServer(), getCity().getID()), file);
                    }

                    return file;
                } catch (SQLException | URISyntaxException ex) {Utils.logSqlException(ex);}
                return null;
            }).get();
        } catch (InterruptedException | ExecutionException ex) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public File getCompletedSchematic() {
        try {
            File file = Paths.get(PlotUtils.getDefaultSchematicPath(), String.valueOf(getCity().getCountry().getServer().getID()), "finishedSchematics", String.valueOf(getCity().getID()), getID() + PlotUtils.SCHEM_ENDING).toFile();
            if (!file.exists()) {
                // if .schem doesn't exist, it looks for old .schematic format for backwards compatibility
                file = Paths.get(PlotUtils.getDefaultSchematicPath(), String.valueOf(getCity().getCountry().getServer().getID()), "finishedSchematics", String.valueOf(getCity().getID()), getID() + PlotUtils.SCHEMATIC_ENDING).toFile();
            }
            return file;
        } catch (SQLException ex) {Utils.logSqlException(ex);}
        return null;
    }

    @Override
    public BlockVector3 getCenter() {
        try {
            if (getVersion() >= 3) {
                return super.getCenter();
            } else return BlockVector3.at(PlotWorld.PLOT_SIZE / 2d, this.getWorld().getPlotHeightCentered(), PlotWorld.PLOT_SIZE / 2d);
        } catch (IOException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("Failed to load schematic file to clipboard!"), ex);
        }
        return null;
    }

    public Slot getSlot() throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT first_slot, second_slot, third_slot FROM plotsystem_builders WHERE uuid = ?")
                .setValue(this.getPlotOwner().getUUID().toString()).executeQuery()) {

            if (rs.next()) {
                for (int i = 1; i <= 3; i++) {
                    int slot = rs.getInt(i);
                    if (!rs.wasNull() && slot == getID()) {
                        DatabaseConnection.closeResultSet(rs);
                        return Slot.values()[i - 1];
                    }
                }
            }

            DatabaseConnection.closeResultSet(rs);

            return null;
        }
    }

    public Review getReview() throws SQLException {
        if (getStatus() == Status.completed || isRejected()) {
            try (ResultSet rs = DatabaseConnection.createStatement("SELECT review_id FROM plotsystem_plots WHERE id = ?")
                    .setValue(this.ID).executeQuery()) {

                if (rs.next()) {
                    int i = rs.getInt(1);
                    DatabaseConnection.closeResultSet(rs);
                    return new Review(i);
                }

                DatabaseConnection.closeResultSet(rs);
            }
        }
        return null;
    }

    public void setPasted(boolean pasted) throws SQLException {
        DatabaseConnection.createStatement("UPDATE plotsystem_plots SET pasted = ? WHERE id = ?")
                .setValue(pasted).setValue(this.ID).executeUpdate();
    }

    public void addPlotMember(Builder member) throws SQLException {
        List<Builder> members = getPlotMembers();
        if (members.size() < 3 && members.stream().noneMatch(m -> m.getUUID().equals(member.getUUID()))) {
            Slot slot = member.getFreeSlot();
            if (slot != null) {
                members.add(member);
                setPlotMembers(members);

                member.setPlot(this.ID, slot);
                getPermissions().addBuilderPerms(member.getUUID());
            }
        }
    }

    public void removePlotMember(Builder member) throws SQLException {
        List<Builder> members = getPlotMembers();
        if (!members.isEmpty() && members.stream().anyMatch(m -> m.getUUID().equals(member.getUUID()))) {
            members.remove(members.stream().filter(m -> m.getUUID().equals(member.getUUID())).findFirst().orElse(null));
            setPlotMembers(members);

            Slot slot = member.getSlot(this);
            if (slot != null) {
                member.removePlot(slot);
            }
            if (getWorld().isWorldGenerated()) getPermissions().removeBuilderPerms(member.getUUID());
        }
    }

    public boolean isReviewed() throws SQLException {
        return getReview() != null;
    }

    public boolean isRejected() throws SQLException {
        return (getStatus() == Status.unfinished || getStatus() == Status.unreviewed) && getTotalScore() != -1; // -1 == null
    }


    public static @NotNull List<Plot> getPlots() throws SQLException {
        return listPlots(DatabaseConnection.createStatement("SELECT id FROM plotsystem_plots").executeQuery());
    }

    public static @NotNull List<Plot> getPlots(Status... statuses) throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement(getStatusQuery("", statuses)).executeQuery();
        return listPlots(rs);
    }

    public static @NotNull List<Plot> getPlots(@NotNull Builder builder) throws SQLException {
        List<Plot> plots = listPlots(DatabaseConnection.createStatement("SELECT id FROM plotsystem_plots WHERE owner_uuid = '" + builder.getUUID() + "' ORDER BY CAST(status AS CHAR)").executeQuery());
        plots.addAll(listPlots(DatabaseConnection.createStatement("SELECT id FROM plotsystem_plots WHERE INSTR(member_uuids, '" + builder.getUUID() + "') > 0 ORDER BY CAST(status AS CHAR)").executeQuery()));
        return plots;
    }

    public static @NotNull List<Plot> getPlots(@NotNull Builder builder, Status... statuses) throws SQLException {
        List<Plot> plots = listPlots(DatabaseConnection.createStatement(getStatusQuery(" AND owner_uuid = '" + builder.getUUID().toString() + "'", statuses)).executeQuery());
        plots.addAll(getPlotsAsMember(builder, statuses));
        return plots;
    }

    public static @NotNull List<Plot> getPlots(@NotNull List<Country> countries, Status status) throws SQLException {
        List<CityProject> cities = new ArrayList<>();
        countries.forEach(c -> cities.addAll(c.getCityProjects()));
        return getPlots(cities, status);
    }

    // Temporary fix to receive plots of builder as member
    private static @NotNull List<Plot> getPlotsAsMember(Builder builder, Status @NotNull ... status) throws SQLException {
        List<Plot> plots = new ArrayList<>();
        for (Status stat : status) {
            plots.addAll(listPlots(DatabaseConnection.createStatement("SELECT id FROM plotsystem_plots WHERE status = '" + stat.name() + "' AND INSTR(member_uuids, '" + builder.getUUID() + "') > 0 ORDER BY CAST(status AS CHAR)").executeQuery()));
        }
        return plots;
    }

    public static @NotNull List<Plot> getPlots(int cityID, Status... statuses) throws SQLException {
        return listPlots(DatabaseConnection.createStatement(getStatusQuery(" AND city_project_id = '" + cityID + "'", statuses)).executeQuery());
    }

    public static @NotNull List<Plot> getPlots(@NotNull List<CityProject> cities, Status... statuses) throws SQLException {
        if (cities.isEmpty()) {
            return new ArrayList<>();
        }
        StringBuilder query = new StringBuilder(" AND (city_project_id = ");

        for (int i = 0; i < cities.size(); i++) {
            query.append(cities.get(i).getID());
            query.append((i != cities.size() - 1) ? " OR city_project_id = " : ")");
        }

        return listPlots(DatabaseConnection.createStatement(getStatusQuery(query.toString(), statuses)).executeQuery());
    }

    public static @NotNull List<Plot> getPlots(int cityID, @NotNull PlotDifficulty plotDifficulty, @NotNull Status status) throws SQLException {
        return listPlots(DatabaseConnection.createStatement("SELECT id FROM plotsystem_plots WHERE city_project_id = ? AND difficulty_id = ? AND status = ?")
                .setValue(cityID)
                .setValue(plotDifficulty.ordinal() + 1)
                .setValue(status.name())
                .executeQuery());
    }

    private static @NotNull String getStatusQuery(String additionalQuery, Status @NotNull ... statuses) {
        StringBuilder query = new StringBuilder("SELECT id FROM plotsystem_plots WHERE status = ");

        for (int i = 0; i < statuses.length; i++) {
            query.append("'").append(statuses[i].name()).append("'").append(additionalQuery);
            query.append((i != statuses.length - 1) ? " OR status = " : "");
        }
        return query.toString();
    }

    private static @NotNull List<Plot> listPlots(@NotNull ResultSet rs) throws SQLException {
        List<Plot> plots = new ArrayList<>();

        while (rs.next()) {
            plots.add(new Plot(rs.getInt(1)));
        }

        DatabaseConnection.closeResultSet(rs);
        return plots;
    }

    public static double getMultiplierByDifficulty(@NotNull PlotDifficulty plotDifficulty) throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement("SELECT multiplier FROM plotsystem_difficulties WHERE id = ?")
                .setValue(plotDifficulty.ordinal() + 1).executeQuery();

        if (rs.next()) {
            double d = rs.getDouble(1);
            DatabaseConnection.closeResultSet(rs);
            return d;
        }

        DatabaseConnection.closeResultSet(rs);
        return 1;
    }

    public static int getScoreRequirementByDifficulty(PlotDifficulty plotDifficulty) throws SQLException {
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT score_requirment FROM plotsystem_difficulties WHERE id = ?")
                .setValue(plotDifficulty.ordinal() + 1).executeQuery()) {

            if (rs.next()) {
                int i = rs.getInt(1);
                DatabaseConnection.closeResultSet(rs);
                return i;
            }

            DatabaseConnection.closeResultSet(rs);
            return 0;
        }
    }

    public static boolean hasPlotDifficultyScoreRequirement(@NotNull Builder builder, PlotDifficulty plotDifficulty) throws SQLException {
        int playerScore = builder.getScore();
        int scoreRequirement = Plot.getScoreRequirementByDifficulty(plotDifficulty);
        return playerScore >= scoreRequirement;
    }

    public static @NotNull CompletableFuture<PlotDifficulty> getPlotDifficultyForBuilder(int cityID, Builder builder) throws SQLException {
        // Check if plot difficulties are available
        boolean easyHasPlots = false;
        boolean mediumHasPlots = false;
        boolean hardHasPlots = false;
        if (!getPlots(cityID, PlotDifficulty.EASY, Status.unclaimed).isEmpty()) easyHasPlots = true;
        if (!getPlots(cityID, PlotDifficulty.MEDIUM, Status.unclaimed).isEmpty()) mediumHasPlots = true;
        if (!getPlots(cityID, PlotDifficulty.HARD, Status.unclaimed).isEmpty()) hardHasPlots = true;

        if (hardHasPlots && hasPlotDifficultyScoreRequirement(builder, PlotDifficulty.HARD)) { // Return hard
            return CompletableFuture.completedFuture(PlotDifficulty.HARD);
        } else if (mediumHasPlots && hasPlotDifficultyScoreRequirement(builder, PlotDifficulty.MEDIUM)) { // Return medium
            return CompletableFuture.completedFuture(PlotDifficulty.MEDIUM);
        } else if (easyHasPlots && hasPlotDifficultyScoreRequirement(builder, PlotDifficulty.EASY)) { // Return easy
            return CompletableFuture.completedFuture(PlotDifficulty.EASY);
        } else if (mediumHasPlots && hasPlotDifficultyScoreRequirement(builder, PlotDifficulty.HARD)) { // If hard has no plots return medium
            return CompletableFuture.completedFuture(PlotDifficulty.EASY);
        } else if (easyHasPlots && hasPlotDifficultyScoreRequirement(builder, PlotDifficulty.MEDIUM)) { // If medium has no plots return easy
            return CompletableFuture.completedFuture(PlotDifficulty.MEDIUM);
        } else if (!PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.ENABLE_SCORE_REQUIREMENT)) { // If score requirement is disabled get plot from any available difficulty
            if (easyHasPlots) {
                return CompletableFuture.completedFuture(PlotDifficulty.EASY);
            } else if (mediumHasPlots) {
                return CompletableFuture.completedFuture(PlotDifficulty.MEDIUM);
            }
        }
        return CompletableFuture.completedFuture(null); // If nothing is available return null
    }
}
