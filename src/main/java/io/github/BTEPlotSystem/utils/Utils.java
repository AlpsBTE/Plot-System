/*
package github.BTEPlotSystem.utils;

import com.sk89q.worldedit.math.MathUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.NotActiveException;
import java.rmi.server.ExportException;

public class Utils {
    final static double THETA = Math.toRadians(-150);
    final static double COS_THETA = Math.cos(THETA);
    final static double SIN_THETA = Math.sin(THETA);
    final static double ARC = 2 * Math.asin(Math.sqrt(5 - Math.sqrt(5)) / Math.sqrt(10));

    protected static final double EL = Math.sqrt(8) / Math.sqrt(5 + Math.sqrt(5));
    protected static final double EL6 = EL / 6;
    protected static final double DVE = Math.sqrt(3 + Math.sqrt(5)) / Math.sqrt(5 + Math.sqrt(5));
    protected static final double R = -3 * EL6 / DVE;

    protected static final double Z = Math.sqrt(5 + 2 * Math.sqrt(5)) / Math.sqrt(15);

    protected static final double[][][] INVERSE_ROTATION_MATRICES = new double[22][3][3];

    protected static final double BERING_X = -0.3420420960118339;//-0.3282152608138795;
    protected static final double BERING_Y = -0.322211064085279;//-0.3281491467713469;
    protected static final double ARCTIC_Y = -0.2;//-0.3281491467713469;
    protected static final double ARCTIC_M = (ARCTIC_Y - Math.sqrt(3) * ARC / 4) / (BERING_X - -0.5 * ARC);
    protected static final double ARCTIC_B = ARCTIC_Y - ARCTIC_M * BERING_X;
    protected static final double ALEUTIAN_Y = -0.5000446805492526;//-0.5127463765943157;
    protected static final double ALEUTIAN_XL = -0.5149231279757507;//-0.4957832938238718;
    protected static final double ALEUTIAN_XR = -0.45;
    protected static final double ALEUTIAN_M = (BERING_Y - ALEUTIAN_Y) / (BERING_X - ALEUTIAN_XR);
    protected static final double ALEUTIAN_B = BERING_Y - ALEUTIAN_M * BERING_X;

    protected static final int[] FACE_ON_GRID = {
            -1, -1, 0, 1, 2, -1, -1, 3, -1, 4, -1,
            -1, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
            20, 19, 15, 21, 16, -1, 17, 18, -1, -1, -1,
    };

    protected static final double[][] CENTER_MAP = {
            {-3, 7},
            {-2, 5},
            {-1, 7},
            {2, 5},
            {4, 5},
            {-4, 1},
            {-3, -1},
            {-2, 1},
            {-1, -1},
            {0, 1},
            {1, -1},
            {2, 1},
            {3, -1},
            {4, 1},
            {5, -1}, //14, left side, right to be cut
            {-3, -5},
            {-1, -5},
            {1, -5},
            {2, -7},
            {-4, -7},
            {-5, -5}, //20, pseudo triangle, child of 14
            {-2, -7} //21 , pseudo triangle, child of 15
    };

    protected static final boolean[] FLIP_TRIANGLE = new boolean[] {
            true, false, true, false, false,
            true, false, true, false, true, false, true, false, true, false,
            true, true, true, false, false,
            true, false
    };

    public static double[] toGeo(double x, double y){
        //Step 1
        boolean easia;
        if (y < 0){
            easia = x > 0;
        } else if (y > ARC / 2){
            easia = x > -Math.sqrt(3)*ARC/2;
        } else {
            easia = y*-Math.sqrt(3)<x;
        }

        double t = x;
        x = -y;
        y = t;

        if (easia){
            t = x;
            x = COS_THETA * x + SIN_THETA * y;
            y = COS_THETA * y - SIN_THETA * t;
            x -= ARC;
        } else {
            x += ARC;
        }

        y+= 0.75*ARC*Math.sqrt(3);

        if (easia != isEurasianPart(x,y)) throw new NotImplementedException();

        //Step 2
        int face = findTriangleGrid(x,y);

        if (face == -1) throw new NotImplementedException();

        x -= CENTER_MAP[face][0];
        y -= CENTER_MAP[face][1];

        if(FLIP_TRIANGLE[face]){
            x = -x;
            y = -y;
        }

        double[] c = inverseTriangleTransformNewton(x,y);
        x = c[0];
        y = c[1];
        double z = c[2];

        double[] vec = new double[] {x,y,z};

        double[] vecp = matVecProdD(INVERSE_ROTATION_MATRICES[face], vec);


        return spherical2Geo(cartesian2Spherical(vecp));
    }

    protected static int findTriangleGrid(double x, double y) {

        //cast equilateral triangles to 45 degrees right triangles (side length of root2)
        double xp = x / ARC;
        double yp = y / (ARC * Math.sqrt(3));

        int row;
        if (yp > -0.25) {
            if (yp < 0.25) { //middle
                row = 1;
            } else if (yp <= 0.75) { //top
                row = 0;
                yp = 0.5 - yp; //translate to middle and flip
            } else {
                return -1;
            }
        } else if (yp >= -0.75) { //bottom
            row = 2;
            yp = -yp - 0.5; //translate to middle and flip
        } else {
            return -1;
        }

        yp += 0.25; //change origin to vertex 4, to allow grids to align

        //rotate coords 45 degrees so left and right sides of the triangle become the x/y axies (also side lengths are now 1)
        double xr = xp - yp;
        double yr = xp + yp;

        //assign a order to what grid along the y=x line it is
        int gx = (int) Math.floor(xr);
        int gy = (int) Math.floor(yr);

        int col = 2 * gx + (gy != gx ? 1 : 0) + 6;

        //out of bounds
        if (col < 0 || col >= 11) {
            return -1;
        }

        return FACE_ON_GRID[row * 11 + col]; //get face at this position
    }

    protected static double[] inverseTriangleTransformNewton(double xpp, double ypp) {

        //a & b are linearly related to c, so using the tan of sum formula we know: tan(c+off) = (tanc + tanoff)/(1-tanc*tanoff)
        double tanaoff = Math.tan(Math.sqrt(3) * ypp + xpp); // a = c + root3*y'' + x''
        double tanboff = Math.tan(2 * xpp); // b = c + 2x''

        double anumer = tanaoff * tanaoff + 1;
        double bnumer = tanboff * tanboff + 1;

        //we will be solving for tanc, starting at t=0, tan(0) = 0
        double tana = tanaoff;
        double tanb = tanboff;
        double tanc = 0;

        double adenom = 1;
        double bdenom = 1;

        //double fp = anumer + bnumer + 1; //derivative relative to tanc

        for (int i = 0; i < 5; i++) {
            double f = tana + tanb + tanc - R; //R = tana + tanb + tanc
            double fp = anumer * adenom * adenom + bnumer * bdenom * bdenom + 1; //derivative relative to tanc

            tanc -= f / fp;

            adenom = 1 / (1 - tanc * tanaoff);
            bdenom = 1 / (1 - tanc * tanboff);

            tana = (tanc + tanaoff) * adenom;
            tanb = (tanc + tanboff) * bdenom;
        }

        //simple reversal algebra based on tan values
        double yp = Math.sqrt(3) * (DVE * tana + EL6) / 2;
        double xp = DVE * tanb + yp / Math.sqrt(3) + EL6;

        //x = z*xp/Z, y = z*yp/Z, x^2 + y^2 + z^2 = 1
        double xpoZ = xp / Z;
        double ypoZ = yp / Z;

        double z = 1 / Math.sqrt(1 + xpoZ * xpoZ + ypoZ * ypoZ);

        return new double[]{ z * xpoZ, z * ypoZ, z };
    }

    protected static double[] matVecProdD(double[][] matrix, double[] vector) {
        double[] result = new double[vector.length];
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                result[i] += matrix[i][j] * vector[j];
            }
        }
        return result;
    }

    protected static double[] spherical2Geo(double[] spherical) {
        double lon = Math.toDegrees(spherical[0]);
        double lat = 90 - Math.toDegrees(spherical[1]);
        return new double[]{ lon, lat };
    }

    protected static double[] cartesian2Spherical(double[] cartesian) {
        double lambda = Math.atan2(cartesian[1], cartesian[0]);
        double phi = Math.atan2(Math.sqrt(cartesian[0] * cartesian[0] + cartesian[1] * cartesian[1]), cartesian[2]);
        return new double[]{ lambda, phi };
    }

    protected static boolean isEurasianPart(double x, double y) {

        //catch vast majority of cases in not near boundary
        if (x > 0) {
            return false;
        }
        if (x < -0.5 * ARC) {
            return true;
        }

        if (y > Math.sqrt(3) * ARC / 4) //above arctic ocean
        {
            return x < 0;
        }

        if (y < ALEUTIAN_Y) //below bering sea
        {
            return y < (ALEUTIAN_Y + ALEUTIAN_XL) - x;
        }

        if (y > BERING_Y) { //boundary across arctic ocean

            if (y < ARCTIC_Y) {
                return x < BERING_X; //in strait
            }

            return y < ARCTIC_M * x + ARCTIC_B; //above strait
        }

        return y > ALEUTIAN_M * x + ALEUTIAN_B;
    }
}
*/
