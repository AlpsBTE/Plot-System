package com.alpsbte.plotsystem.utils.conversion.projection;

import com.alpsbte.plotsystem.utils.conversion.projection.airocean.Airocean;
import com.alpsbte.plotsystem.utils.conversion.projection.airocean.ConformalEstimate;
import com.alpsbte.plotsystem.utils.conversion.projection.airocean.ModifiedAirocean;

import java.util.HashMap;
import java.util.Map;

/**
 * Support for various projection types.
 * <p>
 * The geographic space is the surface of the earth, parameterized by the usual spherical coordinates system of latitude and longitude.
 * The projected space is a plane on to which the geographic space is being projected, and is parameterized by a 2D Cartesian coordinate system (x and y).
 * <p>
 * A projection as defined here is something that projects a point in the geographic space to a point of the projected space (and vice versa).
 * <p>
 * All geographic coordinates are in degrees.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Equirectangular_projection">Wikipedia's article on the equirectangular projection</a>
 */
public abstract class GeographicProjection {

    /**
     * Contains the various projections implemented in Terra121,
     * identified by a String key.
     */
    public static final Map<String, GeographicProjection> projections;

    static {
        projections = new HashMap<>();
        projections.put("airocean", new Airocean());
        projections.put("conformal", new ConformalEstimate());
        projections.put("bteairocean", new ModifiedAirocean());
    }

    /**
     * Orients a projection
     *
     * @param base        - the projection to orient
     * @param orientation - the orientation to use
     * @return a projection that warps the base projection but applies the transformation described by the given orientation
     */
    public static GeographicProjection orientProjection(GeographicProjection base, Orientation orientation) {
        if (base.upright()) {
            if (orientation == Orientation.upright) {
                return base;
            }
            base = new UprightOrientationProjectionTransform(base);
        }

        if (orientation == Orientation.swapped) {
            return null;
        } else if (orientation == Orientation.upright) {
            base = new UprightOrientationProjectionTransform(base);
        }

        return base;
    }


    /**
     * Converts map coordinates to geographic coordinates
     *
     * @param x - x map coordinate
     * @param y - y map coordinate
     * @return {longitude, latitude} in degrees
     * @throws OutOfProjectionBoundsException if the specified point on the projected space cannot be mapped to a point of the geographic space
     */
    public abstract double[] toGeo(double x, double y) throws OutOfProjectionBoundsException;

    /**
     * Converts geographic coordinates to map coordinates
     *
     * @param longitude - longitude, in degrees
     * @param latitude  - latitude, in degrees
     * @return {x, y} map coordinates
     * @throws OutOfProjectionBoundsException if the specified point on the geographic space cannot be mapped to a point of the projected space
     */
    public abstract double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException;

    /**
     * Gives an estimation of the scale of this projection.
     * This is just an estimation, as distortion is inevitable when projecting a sphere onto a flat surface,
     * so this value varies in reality.
     *
     * @return an estimation of the scale of this projection
     */
    public abstract double metersPerUnit();

    /**
     * Indicates the minimum and maximum X and Y coordinates on the projected space.
     *
     * @return {minimum X, minimum Y, maximum X, maximum Y}
     */
    public double[] bounds() {

        try {
            //get max in by using extreme coordinates
            double[] bounds = {
                    this.fromGeo(-180, 0)[0],
                    this.fromGeo(0, -90)[1],
                    this.fromGeo(180, 0)[0],
                    this.fromGeo(0, 90)[1]
            };

            if (bounds[0] > bounds[2]) {
                double t = bounds[0];
                bounds[0] = bounds[2];
                bounds[2] = t;
            }

            if (bounds[1] > bounds[3]) {
                double t = bounds[1];
                bounds[1] = bounds[3];
                bounds[3] = t;
            }

            return bounds;
        } catch (OutOfProjectionBoundsException e) {
            return new double[]{0, 0, 1, 1};
        }
    }

    /**
     * Indicates whether the North Pole is projected to the north of the South Pole on the projected space,
     * assuming Minecraft's coordinate system cardinal directions for the projected space (north is negative Z).
     *
     * @return North Pole Z <= South Pole Z
     */
    public boolean upright() {
        try {
            return this.fromGeo(0, 90)[1] <= this.fromGeo(0, -90)[1];
        } catch (OutOfProjectionBoundsException e) {
            return false;
        }
    }

    public enum Orientation {
        none, upright, swapped
    }
}
