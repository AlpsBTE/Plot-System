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

import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.Review;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Slot;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.sk89q.worldedit.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public interface IPlot {
    /**
     * @return plot id
     */
    int getID();

    /**
     * @return city project
     * @throws SQLException SQL database exception
     */
    CityProject getCity() throws SQLException;

    /**
     * @return plot difficulty
     * @throws SQLException SQL database exception
     */
    PlotDifficulty getDifficulty() throws SQLException;

    /**
     * @return builder who has claimed the plot
     * @throws SQLException SQL database exception
     */
    Builder getPlotOwner() throws SQLException;

    /**
     * Sets the given builder to the plot owner
     * @param UUID UUID of the plot owner, if null remove plot owner
     * @throws SQLException SQL database exception
     */
    void setPlotOwner(@Nullable String UUID) throws SQLException;

    /**
     * @return builders who have been added by the owner as plot members
     * @throws SQLException SQL database exception
     */
    List<Builder> getPlotMembers() throws SQLException;

    /**
     * Sets the given builders to plot members on the plot
     * @param plotMembers plot members, if empty no plot members will get assigned
     * @throws SQLException SQL database exception
     */
    void setPlotMembers(@NotNull List<Builder> plotMembers) throws SQLException;

    /**
     * @return plot review of the completed plot
     * @throws SQLException SQL database exception
     */
    Review getReview() throws SQLException;

    /**
     * @return plot world, can be null if it has not yet been generated
     */
    PlotWorld getWorld();

    /**
     * @return plot permission manager to add or remove build rights
     */
    PlotPermissions getPermissions();

    /**
     * @return total points given for the plot
     * @throws SQLException SQL database exception
     */
    int getTotalScore() throws SQLException;

    /**
     * When building with plot members, the total score is shared
     * @return shared points
     * @throws SQLException SQL database exception
     */
    int getSharedScore() throws SQLException;

    /**
     * Sets the given total score of the plot
     * @param score given points after the review, if -1 set score to null
     * @throws SQLException SQL database exception
     */
    void setTotalScore(int score) throws SQLException;

    /**
     * @return current status of the plot
     * @throws SQLException SQL database exception
     */
    Status getStatus() throws SQLException;

    /**
     * Sets the given status of the plot
     * @param status current status
     * @throws SQLException SQL database exception
     */
    void setStatus(@NotNull Status status) throws SQLException;

    /**
     * @return last date on which the plot owner teleported to the plot
     * @throws SQLException SQL database exception
     */
    Date getLastActivity() throws SQLException;

    /**
     * Sets the last activity to the current date and time
     * @param setNull if true, set last activity to null
     * @throws SQLException SQL database exception
     */
    void setLastActivity(boolean setNull) throws SQLException;

    /**
     * @return date when the plot was created on the Terra121 server
     * @throws SQLException SQL database exception
     */
    Date getCreateDate() throws SQLException;

    /**
     * @return builder who created the plot on the Terra121 server
     * @throws SQLException SQL database exception
     */
    Builder getPlotCreator() throws SQLException;

    /**
     * @return get the plot slot of the plot owner
     * @throws SQLException SQL database exception
     */
    Slot getSlot() throws SQLException;

    /**
     * @return schematic file with outlines only
     */
    File getOutlinesSchematic();

    /**
     * @return schematic file of the completed plot
     */
    File getFinishedSchematic();

    /**
     * Returns geographic coordinates in numeric format
     * @return WG84 EPSG:4979 coordinates as double array {lon,lat} in degrees
     * @see com.alpsbte.plotsystem.utils.conversion.CoordinateConversion#convertToGeo(double, double)
     * @throws SQLException SQL database exception
     */
    String getGeoCoordinates() throws SQLException;

    /**
     * Returns in-game Minecraft coordinates on a Terra121 world
     * @return the in-game coordinates (x, z)
     * @see com.alpsbte.plotsystem.utils.conversion.CoordinateConversion#convertFromGeo(double, double)
     * @throws SQLException SQL database exception
     */
    Vector getMinecraftCoordinates() throws SQLException;

    /**
     * @param pasted if true, plot has been pasted on the Terra121 server
     * @throws SQLException SQL database exception
     */
    void setPasted(boolean pasted) throws SQLException;

    /**
     * @return if {@link #getReview()} is null, it will return false
     * @throws SQLException SQL database exception
     */
    boolean isReviewed() throws SQLException;
}
