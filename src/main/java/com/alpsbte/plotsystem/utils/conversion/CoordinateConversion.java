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

package com.alpsbte.plotsystem.utils.conversion;

import com.alpsbte.plotsystem.utils.conversion.projection.GeographicProjection;
import com.alpsbte.plotsystem.utils.conversion.projection.ScaleProjectionTransform;
import com.alpsbte.plotsystem.utils.conversion.projection.OffsetProjectionTransform;
import com.alpsbte.plotsystem.utils.conversion.projection.OutOfProjectionBoundsException;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CoordinateConversion {

    private static GeographicProjection projection;

    private static final DecimalFormat decFormat1 = new DecimalFormat();

    static {
        decFormat1.setMaximumFractionDigits(1);
        DecimalFormatSymbols usSymbols = new DecimalFormatSymbols(Locale.US);
        decFormat1.setDecimalFormatSymbols(usSymbols);

        projection = GeographicProjection.projections.get("bteairocean");
        projection = GeographicProjection.orientProjection(projection, GeographicProjection.Orientation.upright);
        projection = new ScaleProjectionTransform(projection, 7318261.522857145, 7318261.522857145);
        projection = new OffsetProjectionTransform(projection, 0, 0);
    }

    /**
     * Converts Minecraft coordinates to geographic coordinates
     *
     * @param xCords - Minecraft player x-axis coordinates
     * @param yCords - Minecraft player y-axis coordinates
     * @return - WG84 EPSG:4979 coordinates as double array {lon,lat} in degrees
     */
    public static double[] convertToGeo(double xCords, double yCords) throws OutOfProjectionBoundsException {
        return projection.toGeo(xCords, yCords);
    }

    /**
     * Gets in-game coordinates from geographical location
     *
     * @param lon Geographical Longitude
     * @param lat Geographic Latitude
     * @return The in-game coordinates (x, z)
     */
    public static double[] convertFromGeo(double lon, double lat) throws OutOfProjectionBoundsException {
        return projection.fromGeo(lon, lat);
    }

    /**
     * Get formatted numeric geographic coordinates
     *
     * @param coordinates - WG84 EPSG:4979 coordinates as double array
     * @return - Formatted numeric coordinates as String
     */
    public static String formatGeoCoordinatesNumeric(double[] coordinates) {
        return coordinates[1] + "," + coordinates[0];
    }

    /**
     * Get formatted NSEW geographic coordinates
     *
     * @param coordinates - WG84 EPSG:4979 coordinates as double array
     * @return - Formatted NSEW coordinates as String
     */
    public static String formatGeoCoordinatesNSEW(double[] coordinates) {
        double fixedLon = coordinates[0];
        double fixedLat = coordinates[1];
        String eo = fixedLon < 0 ? "W" : "E";
        String ns = fixedLat < 0 ? "S" : "N";
        double absLon = Math.abs(fixedLon);
        double absLat = Math.abs(fixedLat);
        int longitudeDegrees = (int) absLon;
        int latitudeDegrees = (int) absLat;
        double minLon = absLon * 60 - longitudeDegrees * 60;
        double minLat = absLat * 60 - latitudeDegrees * 60;
        int longitudeMinutes = (int) minLon;
        int latitudeMinutes = (int) minLat;
        double secLon = minLon * 60 - longitudeMinutes * 60;
        double secLat = minLat * 60 - latitudeMinutes * 60;
        String formattedLongitude = longitudeDegrees + "°" + longitudeMinutes + "'" + decFormat1.format(secLon) + "\"" + eo;
        String formattedLatitude = latitudeDegrees + "°" + latitudeMinutes + "'" + decFormat1.format(secLat) + "\"" + ns;
        return formattedLatitude + " " + formattedLongitude;
    }
}
