package com.alpsbte.plotsystem.core.database;

import com.alpsbte.plotsystem.core.data.BuilderProvider;
import com.alpsbte.plotsystem.core.data.DataProvider;

public class DataProviderSql implements DataProvider {
    private final BuilderProvider builderProvider;

    public DataProviderSql() {
        builderProvider = new BuilderProviderSql();
    }
    @Override
    public BuilderProvider getBuilderProvider() {
        return builderProvider;
    }
}
