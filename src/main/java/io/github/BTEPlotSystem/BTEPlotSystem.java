package github.BTEPlotSystem;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.onarandombox.MultiverseCore.MultiverseCore;
import github.BTEPlotSystem.commands.*;
import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.core.EventListener;
import github.BTEPlotSystem.utils.Builder;
import github.BTEPlotSystem.utils.Leaderboard;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.ipvp.canvas.MenuFunctionListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.logging.Level;

public class BTEPlotSystem extends JavaPlugin {
    private static BTEPlotSystem plugin;

    private static MultiverseCore multiverseCore;

    private FileConfiguration leaderboardConfig;

    private FileConfiguration config;
    private File configFile;

    private Leaderboard scoreLeaderboard;
    private Leaderboard parkourLeaderboard;

    @Override
    public void onEnable() {
        plugin = this;
        multiverseCore = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");

        reloadConfig();

        // Connect to Database
        DatabaseConnection.ConnectToDatabase();

        // Add Listener
        this.getServer().getPluginManager().registerEvents(new EventListener(), plugin);
        this.getServer().getPluginManager().registerEvents(new MenuFunctionListener(), plugin);

        // Add Commands
        this.getCommand("plot").setExecutor(new CMDPlot());
        this.getCommand("generateplot").setExecutor(new CMDGeneratePlot());
        this.getCommand("finish").setExecutor(new CMDFinish());
        this.getCommand("abandon").setExecutor(new CMDAbandon());

        this.getCommand("companion").setExecutor(new CMDCompanion());
        this.getCommand("review").setExecutor(new CMDReview());

        this.getCommand("hub").setExecutor(new CMDHub());
        this.getCommand("spawn").setExecutor(new CMDSpawn());

        this.getCommand("setleaderboardposition").setExecutor(new CMDSetLeaderboardPosition());
        this.getCommand("reloadleaderboard").setExecutor(new CMDReloadLeaderboard());


        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // Set Leaderboards
        try {
            scoreLeaderboard = new Leaderboard("SCORE LEADERBOARD", Material.NETHER_STAR, Builder.getBuildersByScore(10),"leaderboardScore",false);
            parkourLeaderboard = new Leaderboard("PARKOUR LEADERBOARD", Material.FEATHER,getParkourList(),"leaderboardParkour",true);
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while placing leaderboards!", ex);
        }

        getLogger().log(Level.INFO, "Successfully enabled BTEPlotSystem plugin.");
    }

    public static BTEPlotSystem getPlugin() {
        return plugin;
    }
    public static MultiverseCore getMultiverseCore() { return multiverseCore; }

    public Leaderboard getScoreLeaderboard() {
        return scoreLeaderboard;
    }

    @Override
    public void reloadConfig() {
        try{
            leaderboardConfig = YamlConfiguration.loadConfiguration(new File(Bukkit.getPluginManager().getPlugin("LeakParkour").getDataFolder(), "history.yml"));
        } catch (Exception ex){
            ex.printStackTrace();
        }

        configFile = new File(getDataFolder(), "config.yml");
        if (configFile.exists()) {
            config = YamlConfiguration.loadConfiguration(configFile);
        } else {
            // Look for default configuration file
            Reader defConfigStream = new InputStreamReader(this.getResource("defaultConfig.yml"), StandardCharsets.UTF_8);

            config = YamlConfiguration.loadConfiguration(defConfigStream);
        }
        saveConfig();
    }

    @Override
    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    public FileConfiguration getLeaderboardConfig() {
        try{
            leaderboardConfig = YamlConfiguration.loadConfiguration(new File(Bukkit.getPluginManager().getPlugin("LeakParkour").getDataFolder(), "history.yml"));
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return leaderboardConfig;
    }

    @Override
    public void saveConfig() {
        if (config == null || configFile == null) {
            return;
        }

        try {
            getConfig().save(configFile);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
        }
    }

    public void connectPlayer(Player player, String server) {
        try{
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("ConnectOther");
            out.writeUTF(player.getName());
            out.writeUTF(server);
            player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
        } catch (Exception ex){
            getLogger().log(Level.WARNING, "Could not connect player [" + player + "] to " + server, ex);
        }
    }

    public List<String> getParkourList() throws SQLException {
        List<String> parkourScores = new ArrayList<>();

        FileConfiguration parkourConfig = BTEPlotSystem.getPlugin().getLeaderboardConfig();

        for (String uuid: parkourConfig.getConfigurationSection("History").getKeys(false)) {
            int score = 0;
            for (String item : parkourConfig.getConfigurationSection("History." + uuid + ".SpeedJumpAndRun").getKeys(false)) {
                score += parkourConfig.getInt("History."+uuid+".SpeedJumpAndRun."+item);
            }
            parkourScores.add(new Builder(UUID.fromString(uuid)).getName() + ","+score);
        }

        HashMap<String,Integer> hashMap = new HashMap<>();

        for (String item : parkourScores) {
            hashMap.put(item.split(",")[0],Integer.parseInt(item.split(",")[1]));
        }

        //LinkedHashMap preserve the ordering of elements in which they are inserted
        LinkedHashMap<String, Integer> reverseSortedMap = new LinkedHashMap<>();

        //Use Comparator.reverseOrder() for reverse ordering
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
}
