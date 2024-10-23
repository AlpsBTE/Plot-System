/*
 * The MIT License (MIT)
 *
 *  Copyright © 2021-2022, Alps BTE <bte.atchli@gmail.com>
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
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotPermissions;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.core.system.plot.world.OnePlotWorld;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.conversion.CoordinateConversion;
import com.alpsbte.plotsystem.utils.conversion.projection.OutOfProjectionBoundsException;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static net.kyori.adventure.text.Component.text;

public abstract class AbstractPlot {
    public static final double PLOT_VERSION = 3;


    protected final int ID;
    protected Builder plotOwner;
    protected OnePlotWorld onePlotWorld;
    protected PlotPermissions plotPermissions;
    protected PlotType plotType;
    protected double plotVersion = -1;

    protected List<BlockVector2> outline;
    protected List<BlockVector2> blockOutline;

    public AbstractPlot(int id) {
        this.ID = id;
    }

    /**
     * @return plot id
     */
    public int getID() {
        return ID;
    }

    /**
     * @return builder who has claimed the plot
     * @throws SQLException SQL database exception
     */
    public abstract Builder getPlotOwner() throws SQLException;

    /**
     * @return plot world, can be one or city plot world
     */
    public abstract <T extends PlotWorld> T getWorld() throws SQLException;

    /**
     * @return the outline of the plot which contains all corner points of the polygon
     */
    public abstract List<BlockVector2> getOutline() throws SQLException, IOException;

    /**
     * @return last date on which the plot owner teleported to the plot
     * @throws SQLException SQL database exception
     */
    public abstract Date getLastActivity() throws SQLException;

    /**
     * Sets the last activity to the current date and time
     *
     * @param setNull if true, set last activity to null
     * @throws SQLException SQL database exception
     */
    public abstract void setLastActivity(boolean setNull) throws SQLException;

    public abstract Status getStatus() throws SQLException;

    public abstract void setStatus(@NotNull Status status) throws SQLException;

    /**
     * Returns the plot type the player has selected when creating the plot
     *
     * @return the plot type
     * @throws SQLException SQL database exception
     */
    public abstract PlotType getPlotType() throws SQLException;

    public abstract double getVersion();

    protected abstract File getSchematicFile(String fileName);

    /**
     * @return schematic file with outlines only
     */
    public abstract File getOutlinesSchematic();

    /**
     * @return schematic file with environment only
     */
    public abstract File getEnvironmentSchematic();


    /**
     * Returns geographic coordinates in numeric format
     *
     * @return WG84 EPSG:4979 coordinates as double array {lon,lat} in degrees
     * @throws IOException fails to load schematic file
     * @see com.alpsbte.plotsystem.utils.conversion.CoordinateConversion#convertToGeo(double, double)
     */
    public String getGeoCoordinates() throws IOException {
        // Convert MC coordinates to geo coordinates
        BlockVector3 mcCoordinates = getCoordinates();
        try {
            return CoordinateConversion.formatGeoCoordinatesNumeric(CoordinateConversion.convertToGeo(mcCoordinates.x(), mcCoordinates.z()));
        } catch (OutOfProjectionBoundsException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("Could not convert MC coordinates to geo coordinates!"), ex);
        }
        return null;
    }

    /**
     * Returns in-game Minecraft coordinates on a Terra121 world
     *
     * @return the in-game coordinates (x, z)
     * @throws IOException fails to load schematic file
     * @see com.alpsbte.plotsystem.utils.conversion.CoordinateConversion#convertFromGeo(double, double)
     */
    public BlockVector3 getCoordinates() throws IOException {
        Clipboard clipboard = FaweAPI.load(getOutlinesSchematic());
        if (clipboard != null) return clipboard.getOrigin();
        return null;
    }

    public BlockVector3 getCenter() {
        try {
            Clipboard clipboard = FaweAPI.load(getOutlinesSchematic());
            if (clipboard != null) {
                Vector3 clipboardCenter = clipboard.getRegion().getCenter();
                return BlockVector3.at(clipboardCenter.x(), this.getWorld().getPlotHeightCentered(), clipboardCenter.z());
            }
        } catch (IOException | SQLException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("Failed to load schematic file to clipboard!"), ex);
        }
        return null;
    }

    /**
     * @return plot permission manager to add or remove build rights
     */
    public PlotPermissions getPermissions() throws SQLException {
        if (plotPermissions == null) plotPermissions = new PlotPermissions(getWorld());
        return plotPermissions;
    }

    public String getOSMMapsLink() throws IOException {
        return "https://www.openstreetmap.org/#map=19/" + getGeoCoordinates().replace(",", "/");
    }

    public String getGoogleMapsLink() throws IOException {
        return "https://www.google.com/maps/place/" + getGeoCoordinates();
    }

    public String getGoogleEarthLink() throws IOException {
        return "https://earth.google.com/web/@" + getGeoCoordinates() + ",0a,1000d,20y,-0h,0t,0r";
    }

    protected List<BlockVector2> getOutlinePoints(String outlinePoints) throws SQLException, IOException {
        List<BlockVector2> locations = new ArrayList<>();
        if (outlinePoints == null) {
            CuboidRegion plotRegion = PlotUtils.getPlotAsRegion(this);
            if (plotRegion != null) locations.addAll(plotRegion.polygonize(4));
        } else {
            String[] list = outlinePoints.split("\\|");

            for (String s : list) {
                String[] locs = s.split(",");
                locations.add(BlockVector2.at(Double.parseDouble(locs[0]), Double.parseDouble(locs[1])));
            }
        }
        this.outline = locations;
        return locations;
    }

    /**
     * @return the outline of the polygon with one point per Block
     */
    public final List<BlockVector2> getBlockOutline() throws SQLException, IOException {
        if (this.blockOutline != null)
            return this.blockOutline;

        List<BlockVector2> points = new ArrayList<>();
        List<BlockVector2> outline = getOutline();

        for (int i = 0; i < outline.size() - 1; i++) {
            BlockVector2 b1 = outline.get(i);
            BlockVector2 b2 = outline.get(i + 1);
            int distance = (int) b1.distance(b2);

            points.addAll(Utils.getLineBetweenPoints(b1, b2, distance));
        }

        BlockVector2 first = outline.get(0);
        BlockVector2 last = outline.get(outline.size() - 1);
        points.addAll(Utils.getLineBetweenPoints(last, first, (int) first.distance(last)));

        this.blockOutline = points;
        return points;
    }
}
