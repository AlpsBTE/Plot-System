/*
 * The MIT License (MIT)
 *
 *  Copyright © 2021, Alps BTE <bte.atchli@gmail.com>
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

package alpsbte.plotsystem.core.holograms;

import alpsbte.plotsystem.PlotSystem;
import alpsbte.plotsystem.core.system.Builder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ScoreLeaderboard extends HolographicDisplay {

    public ScoreLeaderboard() {
        super("score-leaderboard");
    }

    @Override
    protected String getTitle() {
        return "§b§lTOP SCORE";
    }

    @Override
    protected List<String> getDataLines() {
        try {
            return Builder.getBuildersByScore(10);
        } catch (SQLException ex) {
            PlotSystem.getPlugin().getLogger().log(Level.SEVERE, "Could not read data lines.", ex);
        }
        return new ArrayList<>();
    }

    @Override
    protected ItemStack getItem() {
        return new ItemStack(Material.NETHER_STAR);
    }

    @Override
    public void updateHologram() {
        if(isPlaced()) {
            getHologram().clearLines();
            insertLines();
        }
    }
}
