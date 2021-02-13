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
import java.sql.SQLException;
import java.util.logging.Level;

public class BTEPlotSystem extends JavaPlugin {
    private static BTEPlotSystem plugin;

    private static MultiverseCore multiverseCore;

    private FileConfiguration config;
    private File configFile;

    private Leaderboard scoreLeaderboard;

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
        this.getCommand("review").setExecutor(new CMDReview());
        this.getCommand("companion").setExecutor(new CMDCompanion());
        this.getCommand("generateplot").setExecutor(new CMDGeneratePlot());
        this.getCommand("hub").setExecutor(new CMDHub());
        this.getCommand("setleaderboardposition").setExecutor(new CMDSetLeaderboardPosition());
        this.getCommand("reloadleaderboard").setExecutor(new CMDReloadLeaderboard());
        this.getCommand("spawn").setExecutor(new CMDSpawn());

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        getLogger().log(Level.INFO, "Successfully enabled BTEPlotSystem plugin.");

        /*try {
            getLogger().log(Level.INFO, "MC Cords: 3386345 -4686798 IRL Cords: " + Arrays.toString(CoordinateConversion.convertToGeo(3386345, -4686798)));
        } catch (OutOfProjectionBoundsException e) {
            e.printStackTrace();
        }*/

        try {
            scoreLeaderboard = new Leaderboard("SCORE LEADERBOARD", Material.NETHER_STAR, Builder.getBuildersByScore(10));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static BTEPlotSystem getPlugin() {
        return plugin;
    }

    public static MultiverseCore getMultiverseCore() { return multiverseCore; }

    @Override
    public void reloadConfig() {
        configFile = new File(getDataFolder(), "config.yml");
        if (configFile.exists()) {
            config = YamlConfiguration.loadConfiguration(configFile);
        } else {
            // Look for default configuration file
            try {
                Reader defConfigStream = new InputStreamReader(this.getResource("defaultConfig.yml"), "UTF8");

                config = YamlConfiguration.loadConfiguration(defConfigStream);
            } catch (IOException ex) {
                getLogger().log(Level.SEVERE, "Could not load default configuration file", ex);
            }
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

    public Leaderboard getScoreLeaderboard() {
        return scoreLeaderboard;
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
}
