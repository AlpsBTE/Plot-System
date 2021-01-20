package github.BTEPlotSystem.utils.conversion;

public class CoordinateConversion extends ConversionProcessor {

    /**
     * Converts Minecraft coordinates to geographic coordinates
     *
     * @param xCords - Minecraft player x-axis coordinates
     * @param yCords - Minecraft player y-axis coordinates
     * @return - WG84 EPSG:4979 coordinates as double array {lon,lat} in degrees
     */
    public static double[] convertToGeo(double xCords, double yCords) {
        return convertCoordinates(xCords, yCords);
    }

    /**
     * Get formatted geographic coordinates
     *
     * @param coordinates - WG84 EPSG:4979 coordinates as double array
     * @return - Formatted coordinates as String
     */
    public static String formatGeoCoordinates(double[] coordinates) {
        return coordinates[0] + ", " + coordinates[1];
    }
}
