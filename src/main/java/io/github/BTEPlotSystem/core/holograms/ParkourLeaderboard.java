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
import github.BTEPlotSystem.core.system.Builder;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

public class ParkourLeaderboard extends HolographicDisplay {

    public ParkourLeaderboard() {
        super("ParkourLeaderboard");
    }

    @Override
    protected String getTitle() {
        return "§b§lPARKOUR LEADERBOARD";
    }

    @Override
    protected List<String> getDataLines() {
        FileConfiguration parkourConfig = BTEPlotSystem.getPlugin().getLeaderboardConfig();
        List<String> parkourScores = new ArrayList<>();

        for (String uuid : parkourConfig.getConfigurationSection("History").getKeys(false)) {
            int score = 0;
            for (String item : parkourConfig.getConfigurationSection("History." + uuid + ".SpeedJumpAndRun").getKeys(false)) {
                score += parkourConfig.getInt("History."+uuid+".SpeedJumpAndRun."+item);
            }

            try {
                String playerName = new Builder(UUID.fromString(uuid)).getName();
                parkourScores.add((playerName == null ? "Player" : playerName) + "," + score);
            } catch (SQLException ex) {
                BTEPlotSystem.getPlugin().getLogger().log(Level.SEVERE, "Could not convert parkour player to builder!", ex);
            }
        }

        HashMap<String,Integer> hashMap = new HashMap<>();

        for (String item : parkourScores) {
            hashMap.put(item.split(",")[0],Integer.parseInt(item.split(",")[1]));
        }

        LinkedHashMap<String, Integer> reverseSortedMap = new LinkedHashMap<>();

        hashMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));

        List<String> returnList = new ArrayList<>();

        for(Map.Entry<String, Integer> entry : reverseSortedMap.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();

            Date date = new Date(value);
            DateFormat formatter = new SimpleDateFormat("mm:ss:SSS");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            String dateFormatted = formatter.format(date);

            returnList.add(key+","+dateFormatted);
        }

        Collections.reverse(returnList);
        if (returnList.size()>10){
            returnList.subList(10,returnList.size()).clear();
        }

        return returnList;
    }

    @Override
    protected ItemStack getItem() {
        return new ItemStack(Material.FEATHER);
    }
}
