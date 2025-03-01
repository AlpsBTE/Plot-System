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
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotPermissions;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.core.system.plot.world.OnePlotWorld;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.conversion.CoordinateConversion;
import com.alpsbte.plotsystem.utils.conversion.projection.OutOfProjectionBoundsException;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

public abstract class AbstractPlot {
    public static final double PLOT_VERSION = 3;
    public static final ClipboardFormat CLIPBOARD_FORMAT = BuiltInClipboardFormat.FAST_V2;

    private final int ID;

    protected Builder plotOwner;
    protected OnePlotWorld onePlotWorld;
    protected PlotPermissions plotPermissions;
    protected PlotType plotType;
    protected double plotVersion;

    protected List<BlockVector2> outline;
    protected List<BlockVector2> blockOutline;

    public AbstractPlot(int id, UUID plotOwnerUUID) {
        this.ID = id;
        this.plotOwner = plotOwnerUUID != null ? DataProvider.BUILDER.getBuilderByUUID(plotOwnerUUID) : null;
    }

    /**
     * @return plot id
     */
    public int getID() {
        return ID;
    }

    /**
     * @return builder who has claimed the plot
     */
    public Builder getPlotOwner() {
        return plotOwner;
    }

    /**
     * sets the plot owner of the current plot
     * @param plotOwner uuid of player
     * @return if true, the execution was successful
     */
    public abstract boolean setPlotOwner(@Nullable Builder plotOwner);

    /**
     * @return plot world, can be one or city plot world
     */
    public abstract <T extends PlotWorld> T getWorld();

    /**
     * @return the outline of the plot which contains all corner points of the polygon
     */
    public abstract List<BlockVector2> getOutline();

    /**
     * @return last date on which the plot owner teleported to the plot
     */
    public abstract LocalDate getLastActivity();

    /**
     * Sets the last activity to the current date and time
     *
     * @param setNull if true, set last activity to null
     */
    public abstract boolean setLastActivity(boolean setNull);

    public abstract Status getStatus();

    public abstract boolean setStatus(@NotNull Status status);

    /**
     * Returns the plot type the player has selected when creating the plot
     *
     * @return the plot type
     */
    public abstract PlotType getPlotType();

    public abstract double getVersion();

    /**
     * @return schematic file with outlines only
     */
    public abstract byte[] getInitialSchematicBytes();

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
        ByteArrayInputStream inputStream = new ByteArrayInputStream(getInitialSchematicBytes());
        try (ClipboardReader reader = CLIPBOARD_FORMAT.getReader(inputStream)) {
            Clipboard clipboard = reader.read();
            if (clipboard != null) return clipboard.getOrigin();
        }
        return null;
    }

    public BlockVector3 getCenter() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(getInitialSchematicBytes());
        try (ClipboardReader reader = CLIPBOARD_FORMAT.getReader(inputStream)){
            Clipboard clipboard = reader.read();
            if (clipboard != null) {
                Vector3 clipboardCenter = clipboard.getRegion().getCenter();
                return BlockVector3.at(clipboardCenter.x(), this.getWorld().getPlotHeightCentered(), clipboardCenter.z());
            }
        } catch (IOException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("Failed to load schematic file to clipboard!"), ex);
        }
        return null;
    }

    /**
     * @return plot permission manager to add or remove build rights
     */
    public PlotPermissions getPermissions() {
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

    protected List<BlockVector2> getOutlinePoints(String outlinePoints) {
        List<BlockVector2> locations = new ArrayList<>();
        String[] list = outlinePoints.split("\\|");

        for (String s : list) {
            String[] locs = s.split(",");
            locations.add(BlockVector2.at(Double.parseDouble(locs[0]), Double.parseDouble(locs[1])));
        }
        this.outline = locations;
        return locations;
    }

    /**
     * @return the outline of the polygon with one point per Block
     */
    public final List<BlockVector2> getBlockOutline() {
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

        BlockVector2 first = outline.getFirst();
        BlockVector2 last = outline.getLast();
        points.addAll(Utils.getLineBetweenPoints(last, first, (int) first.distance(last)));

        this.blockOutline = points;
        return points;
    }
}
