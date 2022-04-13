package com.alpsbte.plotsystem.core.system.plot;

public enum PlotType {

    FOCUS_MODE(0),
    LOCAL_INSPIRATION_MODE(1),
    CITY_INSPIRATION_MODE(2);

    int id;

    PlotType(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /** Returns true, if the plot type only contains environment around the plot.*/
    public boolean hasEnvironment(){
        if(id == 1 || id == 2)
            return true;
        else
            return false;
    }

    /** Returns true, if the plot type only contains one plot per world.*/
    public boolean hasOnePlotPerWorld(){
        if(id == 0 || id == 1)
            return true;
        else
            return false;
    }

    public static PlotType byId(int id){
        for(PlotType plotType : values())
            if(plotType.getId() == id)
                return plotType;

        return null;
    }
}
