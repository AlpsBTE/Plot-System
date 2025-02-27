/*
 * The MIT License (MIT)
 *
 *  Copyright © 2025, Alps BTE <bte.atchli@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.alpsbte.plotsystem.core.database;

import com.alpsbte.plotsystem.core.database.providers.*;

public class DataProvider {
    public static final BuilderProvider BUILDER = new BuilderProvider();
    public static final PlotProvider PLOT = new PlotProvider();
    public static final DifficultyProvider DIFFICULTY = new DifficultyProvider();
    public static final CityProjectProvider CITY_PROJECT = new CityProjectProvider();
    public static final CountryProvider COUNTRY = new CountryProvider();
    public static final ServerProvider SERVER = new ServerProvider();
    public static final TutorialPlotProvider TUTORIAL_PLOT = new TutorialPlotProvider();
    public static final BuildTeamProvider BUILD_TEAM = new BuildTeamProvider(BUILDER, CITY_PROJECT); // has to be initialized after builder and city project providers
}
