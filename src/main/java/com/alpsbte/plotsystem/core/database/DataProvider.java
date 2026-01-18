package com.alpsbte.plotsystem.core.database;

import com.alpsbte.plotsystem.core.database.providers.BuildTeamProvider;
import com.alpsbte.plotsystem.core.database.providers.BuilderProvider;
import com.alpsbte.plotsystem.core.database.providers.CityProjectProvider;
import com.alpsbte.plotsystem.core.database.providers.CountryProvider;
import com.alpsbte.plotsystem.core.database.providers.DifficultyProvider;
import com.alpsbte.plotsystem.core.database.providers.PlotProvider;
import com.alpsbte.plotsystem.core.database.providers.ReviewProvider;
import com.alpsbte.plotsystem.core.database.providers.ServerProvider;
import com.alpsbte.plotsystem.core.database.providers.TutorialPlotProvider;

public final class DataProvider {
    public static final BuilderProvider BUILDER = new BuilderProvider();
    public static final PlotProvider PLOT = new PlotProvider();
    public static final DifficultyProvider DIFFICULTY = new DifficultyProvider();
    public static final CityProjectProvider CITY_PROJECT = new CityProjectProvider();
    public static final CountryProvider COUNTRY = new CountryProvider();
    public static final ServerProvider SERVER = new ServerProvider();
    public static final TutorialPlotProvider TUTORIAL_PLOT = new TutorialPlotProvider();
    public static final ReviewProvider REVIEW = new ReviewProvider();
    public static final BuildTeamProvider BUILD_TEAM = new BuildTeamProvider(BUILDER, CITY_PROJECT); // has to be initialized after builder and city project providers

    private DataProvider() {}
}
