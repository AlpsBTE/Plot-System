package com.alpsbte.plotsystem.core.database;

import com.alpsbte.plotsystem.core.database.providers.BuilderProvider;

public class DataProvider {
    public static BuilderProvider BUILDER = new BuilderProvider();
    public static BuildTeamProviderSql BUILD_TEAM = new BuildTeamProviderSql();
}
