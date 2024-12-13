package com.alpsbte.plotsystem.core.database;

import com.alpsbte.plotsystem.core.data.BuildTeamProvider;
import com.alpsbte.plotsystem.core.data.BuilderProvider;
import com.alpsbte.plotsystem.core.data.DataProvider;

public class DataProviderSql implements DataProvider {
    private final BuilderProvider builderProvider;
    private final BuildTeamProvider buildTeamProvider;

    public DataProviderSql() {
        builderProvider = new BuilderProviderSql();
        buildTeamProvider = new BuildTeamProviderSql();
    }
    @Override
    public BuilderProvider getBuilderProvider() {
        return builderProvider;
    }

    @Override
    public BuildTeamProvider getBuildTeamProvider() {
        return buildTeamProvider;
    }
}
