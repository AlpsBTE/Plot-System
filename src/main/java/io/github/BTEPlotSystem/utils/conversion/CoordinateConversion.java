package github.BTEPlotSystem.utils.conversion;

import github.BTEPlotSystem.utils.conversion.projection.GeographicProjection;
import github.BTEPlotSystem.utils.conversion.projection.OffsetProjectionTransform;
import github.BTEPlotSystem.utils.conversion.projection.OutOfProjectionBoundsException;
import github.BTEPlotSystem.utils.conversion.projection.ScaleProjectionTransform;

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
        String eo = fixedLon < 0 ? "W": "E";
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
        String formattedLongitude = "" + longitudeDegrees + "°" + longitudeMinutes + "'" + decFormat1.format(secLon) + "\"" + eo;
        String formattedLatitude = "" + latitudeDegrees + "°" + latitudeMinutes + "'" + decFormat1.format(secLat) + "\"" + ns;
        return formattedLatitude + " " + formattedLongitude;
    }
}
