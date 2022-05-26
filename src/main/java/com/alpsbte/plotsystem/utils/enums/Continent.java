package com.alpsbte.plotsystem.utils.enums;

import com.alpsbte.plotsystem.utils.io.language.LangPaths;

public enum Continent {
    EUROPE("europe", LangPaths.Continent.EUROPE),
    ASIA("asia", LangPaths.Continent.ASIA),
    AFRICA("africa", LangPaths.Continent.AFRICA),
    OCEANIA("oceania", LangPaths.Continent.OCEANIA),
    SOUTH_AMERICA("south america", LangPaths.Continent.SOUTH_AMERICA),
    NORTH_AMERICA("north america", LangPaths.Continent.NORTH_AMERICA);

    public final String databaseEnum;
    public final String langPath;
    Continent(String databaseEnum, String langPath) {
        this.databaseEnum = databaseEnum;
        // although LangPath.Continent keys match the enum name, you cannot get the value without reflection
        this.langPath = langPath;
    }
}
