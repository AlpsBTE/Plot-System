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

package github.BTEPlotSystem;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.onarandombox.MultiverseCore.MultiverseCore;
import github.BTEPlotSystem.commands.*;
import github.BTEPlotSystem.commands.admin.CMD_DeletePlot;
import github.BTEPlotSystem.commands.admin.CMD_PReload;
import github.BTEPlotSystem.commands.admin.CMD_SetHologram;
import github.BTEPlotSystem.commands.plot.*;
import github.BTEPlotSystem.commands.review.*;
import github.BTEPlotSystem.core.DatabaseConnection;
import github.BTEPlotSystem.core.EventListener;
import github.BTEPlotSystem.core.holograms.*;
import github.BTEPlotSystem.core.system.plot.PlotManager;
import org.bukkit.Bukkit;
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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class BTEPlotSystem extends JavaPlugin {
    private static BTEPlotSystem plugin;

    private static MultiverseCore multiverseCore;

    private FileConfiguration leaderboardConfig;
    private FileConfiguration navigatorConfig;
    private FileConfiguration config;
    private File configFile;

    private final static List<HolographicDisplay> holograms = new ArrayList<>();

    @Override
    public void onEnable() {
        plugin = this;
        multiverseCore = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");

        reloadConfig();

        // Initialize database
        DatabaseConnection.InitializeDatabase();

        // Add listeners
        this.getServer().getPluginManager().registerEvents(new EventListener(), plugin);
        this.getServer().getPluginManager().registerEvents(new MenuFunctionListener(), plugin);

        // Add default commands [No Permissions]
        this.getCommand("spawn").setExecutor(new CMD_Spawn());
        this.getCommand("hub").setExecutor(new CMD_Spawn());
        this.getCommand("tpp").setExecutor(new CMD_Tpp());
        this.getCommand("tpll").setExecutor(new CMD_Tpll());

        // Add plot commands [alpsbte.plot Permission]
        this.getCommand("companion").setExecutor(new CMD_Companion());
        this.getCommand("link").setExecutor(new CMD_Links());
        this.getCommand("submit").setExecutor(new CMD_Submit());
        this.getCommand("abandon").setExecutor(new CMD_Abandon());
        this.getCommand("undosubmit").setExecutor(new CMD_UndoSubmit());
        this.getCommand("feedback").setExecutor(new CMD_Feedback());
        this.getCommand("plots").setExecutor(new CMD_Plots());
        this.getCommand("tpll").setExecutor(new CMD_Tpll());
        this.getCommand("invite").setExecutor(new CMD_Invite());

        // Add reviewer commands [alpsbte.review Permission]
        this.getCommand("plot").setExecutor(new CMD_Plot());
        this.getCommand("review").setExecutor(new CMD_Review());
        this.getCommand("undoreview").setExecutor(new CMD_UndoReview());
        this.getCommand("sendfeedback").setExecutor(new CMD_SendFeedback());
        this.getCommand("editplot").setExecutor(new CMD_EditPlot());

        // Add admin commands [alpsbte.admin Permission]
        this.getCommand("deleteplot").setExecutor(new CMD_DeletePlot());
        this.getCommand("generateplot").setExecutor(new CMD_GeneratePlot());
        this.getCommand("sethologram").setExecutor(new CMD_SetHologram());
        this.getCommand("preload").setExecutor(new CMD_PReload());


        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // Set holograms
        if(config.getBoolean("holograms.ScoreLeaderboard.enabled")) holograms.add(new ScoreLeaderboard());
        if(config.getBoolean("holograms.BuildsLeaderboard.enabled")) holograms.add(new CompletedBuildsLeaderboard());
        holograms.forEach(Thread::start);

        PlotManager.checkPlotsForLastActivity();

        getLogger().log(Level.INFO, "Successfully enabled AlpsBTE-PlotSystem plugin.");
    }

    public static BTEPlotSystem getPlugin() {
        return plugin;
    }
    public static MultiverseCore getMultiverseCore() { return multiverseCore; }

    public static List<HolographicDisplay> getHolograms() {
        return holograms;
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
}
