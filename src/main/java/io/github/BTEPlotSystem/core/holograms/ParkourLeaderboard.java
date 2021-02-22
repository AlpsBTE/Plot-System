package github.BTEPlotSystem.core.holograms;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.utils.Builder;
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
                parkourScores.add(new Builder(UUID.fromString(uuid)).getName() + "," + score);
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
