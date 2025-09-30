package com.alpsbte.plotsystem.core.system.plot.utils;

public enum PlotType {

    FOCUS_MODE(0),
    LOCAL_INSPIRATION_MODE(1),
    CITY_INSPIRATION_MODE(2),
    TUTORIAL(3);

    final int id;

    PlotType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    // Returns true, if the plot type only contains environment around the plot.
    public boolean hasEnvironment() {
        return id == 1 || id == 2 || id == 3;
    }

    // Returns true, if the plot type only contains one plot per world.
    public boolean hasOnePlotPerWorld() {
        return id == 0 || id == 1 || id == 3;
    }

    public static PlotType byId(int id) {
        for (PlotType plotType : values())
            if (plotType.getId() == id)
                return plotType;

        return null;
    }
}
