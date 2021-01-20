package github.BTEPlotSystem.utils.conversion;

import org.bukkit.Bukkit;

import java.util.logging.Level;

public class ConversionProcessor {

    private static final double ROOT3 = Math.sqrt(3);
    private static final double ARC = 2 * Math.asin(Math.sqrt(5 - Math.sqrt(5)) / Math.sqrt(10));

    protected static double[] convertCoordinates(double x, double y) {
        double t = x;
        x = -y;
        y = t;

        x += ARC;
        y += 0.75 * ARC * ROOT3;

        int face = findTriangleGrid(x, y);

        Bukkit.getLogger().log(Level.INFO, "Face: " + face);

        return new double[] {x, y};
    }

    private static final int[] FACE_ON_GRID = new int[] {
            -1, -1,  0,  1,  2, -1, -1,  3, -1,  4, -1,
            -1,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
            20, 19, 15, 21, 16, -1, 17, 18, -1, -1, -1,
    };

    private static int findTriangleGrid(double x, double y) {

        //cast equiladeral triangles to 45 degree right triangles (side length of root2)
        double xp = x / ARC;
        double yp = y / (ARC * ROOT3);

        Bukkit.getLogger().log(Level.INFO, "Test 1");

        int row;
        if(yp > -0.25) {
            if(yp < 0.25) { //middle
                row = 1;
            }
            else if(yp <= 0.75){ //top
                row = 0;
                yp = 0.5 -yp; //translate to middle and flip
            }
            else return -1;
        } else if (yp >= -0.75) { //bottom
            row = 2;
            yp = -yp - 0.5; //translate to middle and flip
        } else return -1;

        yp += 0.25; //change origin to vertex 4, to allow grids to allign

        //rotate coords 45 degrees so left and right sides of the triangle become the x/y axies (also side lengths are now 1)
        double xr = xp - yp;
        double yr = xp + yp;

        //assign a order to what grid along the y=x line it is
        int gx = (int)Math.floor(xr);
        int gy = (int)Math.floor(yr);

        int col = 2*gx + (gy != gx ? 1 : 0) + 6;

        //out of bounds
        if(col < 0 || col >= 11)
          return -1;

        return FACE_ON_GRID[row * 11 + col]; //get face at this position
    }
}
