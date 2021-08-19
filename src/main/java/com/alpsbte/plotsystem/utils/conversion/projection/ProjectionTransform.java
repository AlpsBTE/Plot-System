package com.alpsbte.plotsystem.utils.conversion.projection;

public abstract class ProjectionTransform extends GeographicProjection {
    protected final GeographicProjection input;

    /**
     * @param input - projection to transform
     */
    public ProjectionTransform(GeographicProjection input) {
        this.input = input;
    }

    @Override
    public boolean upright() {
        return this.input.upright();
    }

    @Override
    public double[] bounds() {
        return this.input.bounds();
    }

    @Override
    public double metersPerUnit() {
        return this.input.metersPerUnit();
    }
}
