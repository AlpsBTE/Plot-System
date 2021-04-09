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

package github.BTEPlotSystem.core.holograms;

import github.BTEPlotSystem.BTEPlotSystem;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class EventHologram extends HolographicDisplay {

    public EventHologram() {
        super("EventHologram");
    }

    @Override
    protected String getTitle() {
        return ("§6§lEVENT-SERVER");
    }

    @Override
    protected void insertLines() {
        getHologram().insertTextLine(0, getTitle());
        getHologram().insertTextLine(1, "§r");

        if(BTEPlotSystem.getPlugin().getNavigatorConfig().getBoolean("servers.event.visible")) {
            List<String> data = getDataLines();
            for(int i = 2; i < data.size() + 2; i++) {
                getHologram().insertTextLine(i, data.get(i - 2));
            }
        } else {
            getHologram().insertTextLine(2, "§2There is currently no event...");
        }
    }

    @Override
    public void updateLeaderboard() {
        if(isPlaced() && getHologram() != null) {
            getHologram().clearLines();
            insertLines();
        }
    }

    @Override
    protected List<String> getDataLines() {
        return Arrays.asList(BTEPlotSystem.getPlugin().getNavigatorConfig().getString("servers.event.type.description").split("/"));
    }

    @Override
    protected ItemStack getItem() {
        return null;
    }
}
