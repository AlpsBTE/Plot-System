/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.core.system;

import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.entity.Player;

public class Difficulty {
    private final String ID;
    private final PlotDifficulty difficulty;

    private double multiplier;
    private int scoreRequirement;

    public Difficulty(PlotDifficulty difficulty, String id, double multiplier, int scoreRequirement) {
        this.difficulty = difficulty;
        this.ID = id;
        this.multiplier = multiplier;
        this.scoreRequirement = scoreRequirement;
    }

    public String getID() {
        return ID;
    }

    public String getName(Player player) {
        return LangUtil.getInstance().get(player, LangPaths.Database.DIFFICULTY + "." + ID + ".name");
    }

    public PlotDifficulty getDifficulty() {
        return difficulty;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public int getScoreRequirement() {
        return scoreRequirement;
    }

    public boolean setMultiplier(double multiplier) {
        if (DataProvider.DIFFICULTY.setMultiplier(ID, multiplier)) {
            this.multiplier = multiplier;
            return true;
        }
        return false;
    }

    public boolean setScoreRequirement(int scoreRequirement) {
        if (DataProvider.DIFFICULTY.setScoreRequirement(ID, scoreRequirement)) {
            this.scoreRequirement = scoreRequirement;
            return true;
        }
        return false;
    }
}
