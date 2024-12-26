package com.alpsbte.plotsystem.utils.conversion.projection.airocean;

import com.alpsbte.plotsystem.utils.conversion.MathUtils;
import com.alpsbte.plotsystem.utils.conversion.projection.InvertableVectorField;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Scanner;

/**
 * Implementation of the Dynmaxion like conformal projection.
 * Slightly modifies the Dynmaxion projection to make it conformal.
 *
 * @see Airocean
 */
public class ConformalEstimate extends Airocean {

    private final InvertableVectorField inverse;

    private final double VECTOR_SCALE_FACTOR = 1 / 1.1473979730192934;

    public ConformalEstimate() {

        int sideLength = 256;

        double[][] xs = new double[sideLength + 1][];
        double[][] ys = new double[xs.length][];

        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("conformal.txt"); Scanner sc = new Scanner(Objects.requireNonNull(is))) {

            for (int u = 0; u < xs.length; u++) {
                double[] px = new double[xs.length - u];
                double[] py = new double[xs.length - u];
                xs[u] = px;
                ys[u] = py;
            }

            for (int v = 0; v < xs.length; v++) {
                for (int u = 0; u < xs.length - v; u++) {
                    String line = sc.nextLine();
                    line = line.substring(1, line.length() - 3);
                    String[] split = line.split(", ");
                    xs[u][v] = Double.parseDouble(split[0]) * this.VECTOR_SCALE_FACTOR;
                    ys[u][v] = Double.parseDouble(split[1]) * this.VECTOR_SCALE_FACTOR;
                }
            }
        } catch (IOException e) {
            System.err.println("Can't load conformal: " + e);
        }

        this.inverse = new InvertableVectorField(xs, ys);
    }

    @Override
    protected double[] triangleTransform(double[] vec) {
        double[] c = super.triangleTransform(vec);

        double x = c[0];
        double y = c[1];

        c[0] /= ARC;
        c[1] /= ARC;

        c[0] += 0.5;
        c[1] += MathUtils.ROOT3 / 6;

        //use another interpolated vector to have a good guess before using Newton's method
        //Note: forward was removed for now, will need to be added back if this improvement is ever re-implemented
        //c = forward.getInterpolatedVector(c[0], c[1]);
        //c = inverse.applyNewtonsMethod(x, y, c[0]/ARC + 0.5, c[1]/ARC + ROOT3/6, 1);

        //just use newtons method: slower
        c = this.inverse.applyNewtonsMethod(x, y, c[0], c[1], 5);//c[0]/ARC + 0.5, c[1]/ARC + ROOT3/6

        c[0] -= 0.5;
        c[1] -= MathUtils.ROOT3 / 6;

        c[0] *= ARC;
        c[1] *= ARC;

        return c;
    }

    @Override
    protected double[] inverseTriangleTransform(double x, double y) {

        x /= ARC;
        y /= ARC;

        x += 0.5;
        y += MathUtils.ROOT3 / 6;

        double[] c = this.inverse.getInterpolatedVector(x, y);
        return super.inverseTriangleTransform(c[0], c[1]);
    }

    @Override
    public double metersPerUnit() {
        return (40075017 / (2 * Math.PI)) / this.VECTOR_SCALE_FACTOR;
    }
}
