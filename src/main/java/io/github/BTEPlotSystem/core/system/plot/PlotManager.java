/*
 * The MIT License (MIT)
 *
 *  Copyright © 2021, Alps BTE <bte.atchli@gmail.com>
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

package github.BTEPlotSystem.core.system.plot;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
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
import github.BTEPlotSystem.utils.FTPManager;
import github.BTEPlotSystem.utils.Utils;
import github.BTEPlotSystem.utils.enums.PlotDifficulty;
import github.BTEPlotSystem.utils.enums.Status;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

public class PlotManager {

    public static List<Plot> getPlots() throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            return listPlots(con.prepareStatement("SELECT idplot FROM plots").executeQuery());
        }
    }

    public static List<Plot> getPlots(Status... statuses) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            return listPlots(con.createStatement().executeQuery(getStatusQuery("idplot", "", statuses)));
        }
    }

    public static List<Plot> getPlots(Builder builder) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            List<Plot> plots = listPlots(con.createStatement().executeQuery("SELECT idplot FROM plots WHERE uuidplayer = '" + builder.getUUID() + "' ORDER BY CAST(status AS CHAR)"));
            plots.addAll(listPlots(con.createStatement().executeQuery("SELECT idplot FROM plots WHERE INSTR(uuidMembers, '" + builder.getUUID() + "') > 0 ORDER BY CAST(status AS CHAR)")));
            return plots;
        }
    }

    public static List<Plot> getPlots(Builder builder, Status... statuses) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            //TODO: Query uuidMember as well
            List<Plot> plots = listPlots(con.createStatement().executeQuery(getStatusQuery("idplot", "' AND uuidplayer = '" + builder.getUUID(), statuses)));
            return plots;
        }
    }

    public static List<Plot> getPlots(int cityID, Status... statuses) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            return listPlots(con.createStatement().executeQuery(getStatusQuery("idplot", "' AND idcity = '" + cityID, statuses)));
        }
    }

    public static List<Plot> getPlots(int cityID, PlotDifficulty plotDifficulty, Status status) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT idplot FROM plots WHERE idcity = ? AND iddifficulty = ? AND status = ?");
            ps.setInt(1, cityID);
            ps.setInt(2, plotDifficulty.ordinal() + 1);
            ps.setString(3, status.name());
            return listPlots(ps.executeQuery());
        }
    }

    private static String getStatusQuery(String columns, String additionalQuery, Status... statuses) {
        StringBuilder query = new StringBuilder("SELECT " + columns + " FROM plots WHERE status = ");

        for(int i = 0; i < statuses.length; i++) {
            query.append("'").append(statuses[i].name()).append(additionalQuery).append("'");
            query.append((i != statuses.length - 1) ? " OR status = " : ""); // TODO: Try to change OR to AND
        }
        return query.toString();
    }

    public static double getMultiplierByDifficulty(PlotDifficulty plotDifficulty) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT multiplier FROM difficulties WHERE iddifficulty = ?");
            ps.setInt(1, plotDifficulty.ordinal() + 1);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getDouble(1);
        }
    }

    public static int getScoreRequirementByDifficulty(PlotDifficulty plotDifficulty) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT scoreRequirement FROM difficulties WHERE iddifficulty = ?");
            ps.setInt(1, plotDifficulty.ordinal() + 1);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    private static List<Plot> listPlots(ResultSet rs) throws SQLException {
        List<Plot> plots = new ArrayList<>();

        while (rs.next()) {
           plots.add(new Plot(rs.getInt("idplot")));
        }

        return plots;
    }

    public static void savePlotAsSchematic(Plot plot) throws IOException, SQLException, WorldEditException {
        // TODO: MOVE CONVERSION TO SEPERATE METHODS

        Vector terraOrigin, schematicOrigin, plotOrigin;
        Vector schematicMinPoint, schematicMaxPoint;
        Vector plotCenter;

        // Load plot outlines schematic as clipboard
        Clipboard outlinesClipboard = ClipboardFormat.SCHEMATIC.getReader(new FileInputStream(plot.getOutlinesSchematic())).read(null);

        // Get player origin coordinates on terra
        terraOrigin = plot.getMinecraftCoordinates();


        // Get plot center
        plotCenter = PlotManager.getPlotCenter(plot);


        // Calculate min and max points of schematic
        int outlinesClipboardCenterX = (int) Math.floor(outlinesClipboard.getRegion().getWidth() / 2d);
        int outlinesClipboardCenterZ = (int) Math.floor(outlinesClipboard.getRegion().getLength() / 2d);

        schematicMinPoint = Vector.toBlockPoint(
                plotCenter.getX() - outlinesClipboardCenterX + ((outlinesClipboard.getRegion().getWidth() % 2 == 0 ? 1 : 0)),
                0,
                plotCenter.getZ() - outlinesClipboardCenterZ + ((outlinesClipboard.getRegion().getLength() % 2 == 0 ? 1 : 0))
        );

        schematicMaxPoint = Vector.toBlockPoint(
                plotCenter.getX() + outlinesClipboardCenterX,
                256,
                plotCenter.getZ() + outlinesClipboardCenterZ
        );


        // Convert terra schematic coordinates into relative plot schematic coordinates
        schematicOrigin = Vector.toBlockPoint(
                Math.floor(terraOrigin.getX()) - Math.floor(outlinesClipboard.getMinimumPoint().getX()),
                Math.floor(terraOrigin.getY()) - Math.floor(outlinesClipboard.getMinimumPoint().getY()),
                Math.floor(terraOrigin.getZ()) - Math.floor(outlinesClipboard.getMinimumPoint().getZ())
        );


        // Add additional plot sizes to relative plot schematic coordinates
        plotOrigin = Vector.toBlockPoint(
                schematicOrigin.getX() + schematicMinPoint.getX(),
                schematicOrigin.getY() + 15 - Math.floor(outlinesClipboard.getRegion().getHeight() / 2f) + (outlinesClipboard.getRegion().getHeight() % 2 == 0 ? 1 : 0),
                schematicOrigin.getZ() + schematicMinPoint.getZ()
        );


        // Load finished plot region as cuboid region
        PlotHandler.loadPlot(plot);
        CuboidRegion region = new CuboidRegion(new BukkitWorld(Bukkit.getWorld("P-" + plot.getID())), schematicMinPoint, schematicMaxPoint);


        // Copy finished plot region to clipboard
        Clipboard cb = new BlockArrayClipboard(region);
        cb.setOrigin(plotOrigin);
        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(region.getWorld(), -1);
        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(editSession, region, cb, region.getMinimumPoint());
        Operations.complete(forwardExtentCopy);

        if(!BTEPlotSystem.getPlugin().getConfig().getBoolean("ftp-enabled")) { // Save Local
            // Write finished plot clipboard to schematic file
            try(ClipboardWriter writer = ClipboardFormat.SCHEMATIC.getWriter(new FileOutputStream(plot.getFinishedSchematic(true), false))) {
                writer.write(cb, region.getWorld().getWorldData());
            }
        } else { // Save FTP/SFTP
            Utils.Server server = plot.getCity().getServer();

            if(server.ftpConfiguration != null) {
                // Send schematic to terra server
                FTPManager.sendFileFTP(FTPManager.getFTPUrl(
                        server, plot),
                        plot.getFinishedSchematic(false));
            } else {
                Bukkit.getLogger().log(Level.SEVERE, "FTP configuration is null!");
            }
        }
    }

    public static double[] convertTerraToPlotXZ(Plot plot, double[] terraCoords) throws IOException {

        // Load plot outlines schematic as clipboard
        Clipboard outlinesClipboard = ClipboardFormat.SCHEMATIC.getReader(new FileInputStream(plot.getOutlinesSchematic())).read(null);

        // Calculate min and max points of schematic
        int outlinesClipboardCenterX = (int) Math.floor(outlinesClipboard.getRegion().getWidth() / 2d);
        int outlinesClipboardCenterZ = (int) Math.floor(outlinesClipboard.getRegion().getLength() / 2d);

        Vector schematicMinPoint = Vector.toBlockPoint(
                PlotManager.getPlotCenter(plot).getX() - outlinesClipboardCenterX + ((outlinesClipboard.getRegion().getWidth() % 2 == 0 ? 1 : 0)),
                0,
                PlotManager.getPlotCenter(plot).getZ() - outlinesClipboardCenterZ + ((outlinesClipboard.getRegion().getLength() % 2 == 0 ? 1 : 0))
        );

        Vector schematicMaxPoint = Vector.toBlockPoint(
                PlotManager.getPlotCenter(plot).getX() + outlinesClipboardCenterX,
                256,
                PlotManager.getPlotCenter(plot).getZ() + outlinesClipboardCenterZ
        );

        // Convert terra schematic coordinates into relative plot schematic coordinates
        double[] schematicCoords = {
                terraCoords[0] - outlinesClipboard.getMinimumPoint().getX(),
                terraCoords[1] - outlinesClipboard.getMinimumPoint().getZ()
        };

        // Add additional plot sizes to relative plot schematic coordinates
        double[] plotCoords = {
                schematicCoords[0] + schematicMinPoint.getX(),
                schematicCoords[1] + schematicMinPoint.getZ()
        };

        // Return coordinates if they are in the schematic plot region
        if(new CuboidRegion(schematicMinPoint, schematicMaxPoint).contains((int)plotCoords[0], (int)plotCoords[1])) {
            return plotCoords;
        }

       return null;
    }

    public static void checkPlotsForLastActivity() {
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(BTEPlotSystem.getPlugin(), () -> {
            try {
                List<Plot> plots = getPlots(Status.unfinished);
                long millisIn14Days = 14L * 24 * 60 * 60 * 1000; // Remove all plots which have no activity for the last 14 days

                for(Plot plot : plots) {
                    if(plot.getLastActivity() != null && plot.getLastActivity().getTime() < (new Date().getTime() - millisIn14Days)) {
                        Bukkit.getScheduler().runTask(BTEPlotSystem.getPlugin(), () -> {
                            try {
                                PlotHandler.abandonPlot(plot);
                                Bukkit.getLogger().log(Level.INFO, "Abandoned plot #" + plot.getID() + " due to inactivity!");
                            } catch (Exception ex) {
                                Bukkit.getLogger().log(Level.SEVERE, "A unknown error occurred!", ex);
                            }
                        });
                    }
                }
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        }, 0L, 20 * 60 * 60); // Check every hour
    }

    public static Plot getPlotByWorld(World plotWorld) throws SQLException {
        return new Plot(Integer.parseInt(plotWorld.getName().substring(2)));
    }

    public static boolean plotExists(int ID) {
        String worldName = "P-" + ID;
        return (BTEPlotSystem.getMultiverseCore().getMVWorldManager().getMVWorld(worldName) != null) || BTEPlotSystem.getMultiverseCore().getMVWorldManager().getUnloadedWorlds().contains(worldName);
    }

    // TODO: Make this function more efficient :eyes:
    public static PlotDifficulty getPlotDifficultyForBuilder(int cityID, Builder builder) throws SQLException {
        int playerScore = builder.getScore();
        int easyScore = PlotManager.getScoreRequirementByDifficulty(PlotDifficulty.EASY), mediumScore = PlotManager.getScoreRequirementByDifficulty(PlotDifficulty.MEDIUM), hardScore = PlotManager.getScoreRequirementByDifficulty(PlotDifficulty.HARD);
        boolean easyHasPlots = false, mediumHasPlots = false, hardHasPlots = false;

        if(PlotManager.getPlots(cityID, PlotDifficulty.EASY, Status.unclaimed).size() != 0) {
            easyHasPlots = true;
        }

        if(PlotManager.getPlots(cityID, PlotDifficulty.MEDIUM, Status.unclaimed).size() != 0) {
            mediumHasPlots = true;
        }

        if(PlotManager.getPlots(cityID, PlotDifficulty.HARD, Status.unclaimed).size() != 0) {
            hardHasPlots = true;
        }

        if(playerScore >= easyScore && playerScore < mediumScore && easyHasPlots) {
            return PlotDifficulty.EASY;
        } else if(playerScore >= mediumScore && playerScore < hardScore && mediumHasPlots) {
            return PlotDifficulty.MEDIUM;
        } else if(playerScore >= hardScore && hardHasPlots) {
            return PlotDifficulty.HARD;
        } else if(easyHasPlots && playerScore >= mediumScore && playerScore < hardScore ) {
            return PlotDifficulty.EASY;
        } else if(mediumHasPlots && playerScore >= easyScore && playerScore < mediumScore) {
            return PlotDifficulty.MEDIUM;
        } else if(hardHasPlots) {
            return PlotDifficulty.HARD;
        } else if(mediumHasPlots) {
            return PlotDifficulty.MEDIUM;
        } else {
            return PlotDifficulty.EASY;
        }
    }

    public static boolean isPlotWorld(World world) {
        return BTEPlotSystem.getMultiverseCore().getMVWorldManager().isMVWorld(world) && world.getName().startsWith("P-");
    }

    public static int getPlotSize(Plot plot) {
       if(plot.getPlotRegion() != null) {
           if(plot.getPlotRegion().contains(150, 15, 150)) {
               return 150;
           } else {
               return 100;
           }
       } else {
        return 150;
       }
    }

    public static Vector getPlotCenter(Plot plot) {
        return Vector.toBlockPoint(
                getPlotSize(plot) / 2d + 0.5,
                15, // TODO: Change Y value to the bottom of the schematic
                getPlotSize(plot) / 2d + 0.5
        );
    }

    public static String getWorldGuardConfigPath(String worldName) {
        return Bukkit.getPluginManager().getPlugin("WorldGuard").getDataFolder() + "/worlds/" + worldName;
    }

    public static String getMultiverseInventoriesConfigPath(String worldName) {
        return Bukkit.getPluginManager().getPlugin("Multiverse-Inventories").getDataFolder() + "/worlds/" + worldName;
    }

    public static File getPlotOutlinesSchematic(Plot plot) {
       return null;
    }

    public static File getFinishedPlotSchematic(Plot plot) {
        return null;
    }

    public static String getPlotSchematicsPath() {
        return Paths.get(BTEPlotSystem.getPlugin().getDataFolder().getAbsolutePath(), "plots") + File.separator;
    }
}