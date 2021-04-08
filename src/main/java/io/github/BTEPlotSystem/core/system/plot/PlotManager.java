package github.BTEPlotSystem.core.system.plot;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.CuboidRegion;
import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.core.system.Builder;
import github.BTEPlotSystem.utils.enums.Difficulty;
import github.BTEPlotSystem.utils.enums.Status;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

    public static void savePlotAsSchematic(Plot plot) throws IOException, SQLException, WorldEditException {

        Vector terraOrigin = plot.getMinecraftCoordinates();
        Bukkit.getLogger().log(Level.INFO, "Terra Origin: " + terraOrigin.toString());

        double schematicOriginX, schematicOriginY, schematicOriginZ;
        double plotOriginX, plotOriginY, plotOriginZ;

        // Load plot outlines schematic as clipboard
        Clipboard outlinesClipboard = ClipboardFormat.SCHEMATIC.getReader(new FileInputStream(plot.getOutlinesSchematic())).read(null);

        Vector plotCenter = Vector.toBlockPoint(
                PlotManager.getPlotSize() / 2f,
                15,
                PlotManager.getPlotSize() / 2f
        );
        Bukkit.getLogger().log(Level.INFO, "Plot Center: " + plotCenter);

        // Calculate min and max plot region vectors
        int outlinesClipboardCenterX = (int) Math.floor(outlinesClipboard.getRegion().getWidth() / 2f);
        int outlinesClipboardCenterZ = (int) Math.floor(outlinesClipboard.getRegion().getLength() / 2f);

        Vector minPoint = new Vector(
                plotCenter.getX() - outlinesClipboardCenterX + ((outlinesClipboard.getRegion().getWidth() % 2 == 0 ? 1 : 0)),
                0,
                plotCenter.getZ() - outlinesClipboardCenterZ + ((outlinesClipboard.getRegion().getLength() % 2 == 0 ? 1 : 0))
        );
        Bukkit.getLogger().log(Level.INFO, "Finished Plot Min Point: " + minPoint.toString());


        Vector maxPoint = new Vector(
                plotCenter.getX() + Math.ceil(outlinesClipboardCenterX),
                256,
                plotCenter.getZ() + Math.ceil(outlinesClipboardCenterZ));
        Bukkit.getLogger().log(Level.INFO, "Finished Plot Max Point: " + maxPoint.toString());


        // Load finished plot region as cuboid region
        PlotHandler.loadPlot(plot);
        CuboidRegion region = new CuboidRegion(new BukkitWorld(Bukkit.getWorld("P-" + plot.getID())), minPoint, maxPoint);

        Bukkit.getLogger().log(Level.INFO, "Schematic Info: Length: " + outlinesClipboard.getRegion().getLength() + " | Width: " + outlinesClipboard.getRegion().getWidth() + " | Height: " + outlinesClipboard.getRegion().getHeight());

        // Convert origin terra coords into relative schematic coords
        schematicOriginX = Math.floor(terraOrigin.getX()) - Math.floor(outlinesClipboard.getMinimumPoint().getX());
        schematicOriginY = Math.floor(terraOrigin.getY()) - Math.floor(outlinesClipboard.getMinimumPoint().getY()); // Removed .getRegion()
        schematicOriginZ = Math.floor(terraOrigin.getZ()) - Math.floor(outlinesClipboard.getMinimumPoint().getZ());
        Bukkit.getLogger().log(Level.INFO, "Outlines Clipboard Min Point: " + outlinesClipboard.getMinimumPoint().toString());
        Bukkit.getLogger().log(Level.INFO, "Schematic Origin Point: " + new Vector(schematicOriginX, schematicOriginY, schematicOriginZ).toString());


        // Convert schematic coords into relative plot coords
        plotOriginX = schematicOriginX + minPoint.getX();
        plotOriginY = schematicOriginY + (15 - Math.floor(outlinesClipboard.getRegion().getHeight() / 2f)) + (outlinesClipboard.getRegion().getHeight() % 2 == 0 ? 1 : 0);
        plotOriginZ = schematicOriginZ + minPoint.getZ();

        Vector plotOrigin = new Vector(plotOriginX, plotOriginY, plotOriginZ);
        Bukkit.getLogger().log(Level.INFO, "Plot Origin Point: " + plotOrigin.toString());


        // Copy finished plot region to clipboard
        Clipboard cb = new BlockArrayClipboard(region);
        cb.setOrigin(plotOrigin);
        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(region.getWorld(), -1);
        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(editSession, region, cb, region.getMinimumPoint());
        Operations.complete(forwardExtentCopy);

        // Write finished plot clipboard to schematic file
        try(ClipboardWriter writer = ClipboardFormat.SCHEMATIC.getWriter(new FileOutputStream(plot.getFinishedSchematic(), false))) {
            writer.write(cb, region.getWorld().getWorldData());
        }
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
        } else if(playerScore >= mediumScore && playerScore < hardScore && mediumHasPlots) {
            return Difficulty.MEDIUM;
        } else if(playerScore >= hardScore && hardHasPlots) {
            return Difficulty.HARD;
        } else if(easyHasPlots && playerScore >= mediumScore && playerScore < hardScore ) {
            return Difficulty.EASY;
        } else if(mediumHasPlots && playerScore >= easyScore && playerScore < mediumScore) {
            return Difficulty.MEDIUM;
        } else if(hardHasPlots) {
            return Difficulty.HARD;
        } else if(mediumHasPlots) {
            return Difficulty.MEDIUM;
        } else if(easyHasPlots) {
            return Difficulty.EASY;
        } else {
            return null;
        }
    }

    public static boolean isPlotWorld(World world) {
        return BTEPlotSystem.getMultiverseCore().getMVWorldManager().isMVWorld(world) && world.getName().startsWith("P-");
    }

    public static int getPlotSize() {
        return 150;
    }

    public static String getOutlinesSchematicPath() {
        return BTEPlotSystem.getPlugin().getConfig().getString("outlines-schematic-path");
    }

    public static String getFinishedSchematicPath() {
        return BTEPlotSystem.getPlugin().getConfig().getString("finished-schematic-path");
    }
}
