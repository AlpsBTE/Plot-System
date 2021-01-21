package github.BTEPlotSystem.utils.conversion;

import github.BTEPlotSystem.utils.conversion.projection.GeographicProjection;
import github.BTEPlotSystem.utils.conversion.projection.OffsetProjectionTransform;
import github.BTEPlotSystem.utils.conversion.projection.OutOfProjectionBoundsException;
import github.BTEPlotSystem.utils.conversion.projection.ScaleProjectionTransform;

public class CoordinateConversion {

    private static GeographicProjection projection;

    static {
        projection  = GeographicProjection.projections.get("bteairocean");
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
     * Get formatted geographic coordinates
     *
     * @param coordinates - WG84 EPSG:4979 coordinates as double array
     * @return - Formatted coordinates as String
     */
    public static String formatGeoCoordinates(double[] coordinates) {
        return coordinates[1] + ", " + coordinates[0];
    }
}
