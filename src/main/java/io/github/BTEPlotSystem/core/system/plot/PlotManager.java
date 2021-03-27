package github.BTEPlotSystem.core.system.plot;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.core.system.Builder;
import github.BTEPlotSystem.utils.enums.Difficulty;
import github.BTEPlotSystem.utils.enums.Status;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class PlotManager {

    public static List<Plot> getPlots() throws SQLException {
        return listPlots(DatabaseConnection.createStatement().executeQuery("SELECT idplot FROM plots"));
    }

    public static List<Plot> getPlots(Status... status) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT idplot FROM plots WHERE status = ");

        for(int i = 0; i < status.length; i++) {
            query.append("'").append(status[i].name()).append("'");

            query.append((i != status.length - 1) ? " OR status = " : "");
        }

        return listPlots(DatabaseConnection.createStatement().executeQuery(query.toString()));
    }

    public static List<Plot> getPlots(Builder builder) throws SQLException {
        return listPlots(DatabaseConnection.createStatement().executeQuery("SELECT idplot FROM plots WHERE uuidplayer = '" + builder.getUUID() + "'"));
    }

    public static List<Plot> getPlots(Builder builder, Status... status) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT idplot FROM plots WHERE status = ");

        for(int i = 0; i < status.length; i++) {
            query.append("'").append(status[i].name()).append("' AND uuidplayer = '").append(builder.getUUID()).append("'");

            query.append((i != status.length - 1) ? " OR status = " : "");
        }
        return listPlots(DatabaseConnection.createStatement().executeQuery(query.toString()));
    }

    public static List<Plot> getPlots(int cityID, Status... status) throws SQLException {
        StringBuilder query = new StringBuilder("SELECT idplot FROM plots WHERE status = ");

        for(int i = 0; i < status.length; i++) {
            query.append("'").append(status[i].name()).append("' AND idcity = '").append(cityID).append("'");

            query.append((i != status.length - 1) ? " OR status = " : "");
        }

        return listPlots(DatabaseConnection.createStatement().executeQuery(query.toString()));
    }

    public static List<Plot> getPlots(int cityID, Difficulty difficulty, Status status) throws SQLException {
        return listPlots(DatabaseConnection.createStatement().executeQuery("SELECT idplot FROM plots WHERE idcity = '" + cityID + "' AND iddifficulty = '" + (difficulty.ordinal() + 1) + "' AND status = '" + status.name() + "'"));
    }

    public static double getMultiplierByDifficulty(Difficulty difficulty) throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT multiplier FROM difficulties where name = '" + difficulty.name() + "'");
        rs.next();

        return rs.getDouble(1);
    }

    public static int getScoreRequirementByDifficulty(Difficulty difficulty) throws SQLException {
        ResultSet rs = DatabaseConnection.createStatement().executeQuery("SELECT scoreRequirement FROM difficulties WHERE iddifficulty = '" + (difficulty.ordinal() + 1) + "'");
        rs.next();

        return rs.getInt(1);
    }

    private static List<Plot> listPlots(ResultSet rs) throws SQLException {
        List<Plot> plots = new ArrayList<>();

        // Get plot
        while (rs.next()) {
           plots.add(new Plot(rs.getInt("idplot")));
        }

        return plots;
    }

    @SuppressWarnings("deprecation")
    public static void checkPlotsForLastActivity() {
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(BTEPlotSystem.getPlugin(), () -> {
            try {
                List<Plot> plots = getPlots(Status.unfinished);
                long millisIn30Days = 30L * 24 * 60 * 60 * 1000;

                for(Plot plot : plots) {
                    if(plot.getLastActivity().getTime() < (new Date().getTime() - millisIn30Days)) {
                        Bukkit.getScheduler().runTask(BTEPlotSystem.getPlugin(), () -> {
                            try {
                                //PlotHandler.abandonPlot(plot);
                                Bukkit.getLogger().log(Level.INFO, "Called event to abandon plot #" + plot.getID() + " after 30 days of inactivity!");
                            } catch (Exception ex) {
                                Bukkit.getLogger().log(Level.SEVERE, "A unknown error occurred!", ex);
                            }
                        });
                    }
                }
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        }, 0L, 20 * 60 * 60); // 1 Hour
    }

    public static Plot getPlotByWorld(World plotWorld) throws SQLException {
        return new Plot(Integer.parseInt(plotWorld.getName().substring(2)));
    }

    public static boolean plotExists(int ID) {
        String worldName = "P-" + ID;
        return (BTEPlotSystem.getMultiverseCore().getMVWorldManager().getMVWorld(worldName) != null) || BTEPlotSystem.getMultiverseCore().getMVWorldManager().getUnloadedWorlds().contains(worldName);
    }

    public static Difficulty getPlotDifficultyForBuilder(int cityID, Builder builder) throws SQLException {
        int playerScore = builder.getScore();
        int easyScore = PlotManager.getScoreRequirementByDifficulty(Difficulty.EASY), mediumScore = PlotManager.getScoreRequirementByDifficulty(Difficulty.MEDIUM), hardScore = PlotManager.getScoreRequirementByDifficulty(Difficulty.HARD);
        boolean easyHasPlots = false, mediumHasPlots = false, hardHasPlots = false;

        if(PlotManager.getPlots(cityID, Difficulty.EASY, Status.unclaimed).size() != 0) {
            easyHasPlots = true;
        }

        if(PlotManager.getPlots(cityID, Difficulty.MEDIUM, Status.unclaimed).size() != 0) {
            mediumHasPlots = true;
        }

        if(PlotManager.getPlots(cityID, Difficulty.HARD, Status.unclaimed).size() != 0) {
            hardHasPlots = true;
        }

        if(playerScore >= easyScore && playerScore < mediumScore && easyHasPlots) {
            return Difficulty.EASY;
        } else {
            if(playerScore >= mediumScore && playerScore < hardScore && mediumHasPlots) {
                return Difficulty.MEDIUM;
            } else if(!mediumHasPlots && easyHasPlots && playerScore >= mediumScore && playerScore < hardScore) {
                return Difficulty.EASY;
            } else {
                if(playerScore >= hardScore && hardHasPlots) {
                    return Difficulty.HARD;
                } else if(mediumHasPlots && playerScore >= hardScore) {
                    return Difficulty.MEDIUM;
                } else if(easyHasPlots && playerScore >= hardScore) {
                    return Difficulty.EASY;
                } else {
                    return null;
                }
            }
        }
    }

    public static boolean isPlotWorld(World world) {
        return BTEPlotSystem.getMultiverseCore().getMVWorldManager().isMVWorld(world) && world.getName().startsWith("P-");
    }

    public static int getPlotSize() {
        return 150;
    }

    public static String getSchematicPath() {
        return BTEPlotSystem.getPlugin().getConfig().getString("schematic-path");
    }
}
