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

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import github.BTEPlotSystem.commands.*;
import github.BTEPlotSystem.commands.admin.CMD_DeletePlot;
import github.BTEPlotSystem.commands.admin.CMD_PReload;
import github.BTEPlotSystem.commands.admin.CMD_SetHologram;
import github.BTEPlotSystem.commands.plot.*;
import github.BTEPlotSystem.commands.review.*;
import github.BTEPlotSystem.core.config.ConfigPaths;
import github.BTEPlotSystem.core.database.DatabaseConnection;
import github.BTEPlotSystem.core.EventListener;
import github.BTEPlotSystem.core.config.ConfigManager;
import github.BTEPlotSystem.core.config.ConfigNotImplementedException;
import github.BTEPlotSystem.core.holograms.*;
import github.BTEPlotSystem.core.system.plot.PlotManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.ipvp.canvas.MenuFunctionListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BTEPlotSystem extends JavaPlugin {

    private static BTEPlotSystem plugin;
    private ConfigManager configManager;

    private static final List<HolographicDisplay> holograms = Arrays.asList(
      new ScoreLeaderboard(),
      new PlotsLeaderboard()
    );

    @Override
    public void onEnable() {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog"); // Disable Logging
        plugin = this;

        String successPrefix = ChatColor.DARK_GRAY + "[" + ChatColor.DARK_GREEN + "✔" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;
        String errorPrefix = ChatColor.DARK_GRAY + "[" + ChatColor.RED + "X" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;

        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "------------------ Plot-System V1.2 ------------------");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "Starting plugin...");
        Bukkit.getConsoleSender().sendMessage("");

        // Check for required dependencies, if it returns false disable plugin
        if (!DependencyManager.checkForRequiredDependencies()) {
            Bukkit.getConsoleSender().sendMessage(errorPrefix + "Could not load required dependencies.");
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Missing Dependencies:");
            DependencyManager.missingDependencies.forEach(dependency -> Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + " - " + dependency));

            plugin.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Bukkit.getConsoleSender().sendMessage(successPrefix + "Successfully loaded required dependencies.");

        // Load config, if it throws an exception disable plugin
        try {
            configManager = new ConfigManager();
            Bukkit.getConsoleSender().sendMessage(successPrefix + "Successfully loaded configuration file.");
        } catch (ConfigNotImplementedException ex) {
            Bukkit.getConsoleSender().sendMessage(errorPrefix + "Could not load configuration file.");
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "The config file must be configured!");

            plugin.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        reloadConfig();

        // Initialize database connection
        try {
            DatabaseConnection.InitializeDatabase();
            Bukkit.getConsoleSender().sendMessage(successPrefix + "Successfully initialized database connection.");
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage(errorPrefix + "Could not initialize database connection.");
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + ex.getMessage());

            plugin.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register event listeners
        try {
            this.getServer().getPluginManager().registerEvents(new EventListener(), plugin);
            this.getServer().getPluginManager().registerEvents(new MenuFunctionListener(), plugin);
            Bukkit.getConsoleSender().sendMessage(successPrefix + "Successfully registered event listeners.");
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage(errorPrefix + "Could not register event listeners.");
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + ex.getMessage());

            plugin.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register commands
        try {
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
            Bukkit.getConsoleSender().sendMessage(successPrefix + "Successfully registered commands.");
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage(errorPrefix + "Could not register commands.");
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + ex.getMessage());

            plugin.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Check for extensions
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "Extensions:");

        if (DependencyManager.isHolographicDisplaysEnabled()) {
            reloadHolograms();
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "- Holograms (Leaderboard)");
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "No extensions enabled.");
        }

        PlotManager.checkPlotsForLastActivity();

        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "Enabled Plot-System plugin.");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "------------------------------------------------------");
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Disabling plugin...");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "------------------------------------------------------");
    }

    @Override
    public FileConfiguration getConfig() {
        return this.configManager.getConfig();
    }

    @Override
    public void reloadConfig() {
        this.configManager.reloadConfig();
    }

    @Override
    public void saveConfig() {
        this.configManager.saveConfig();
    }

    public static void reloadHolograms() {
        for (HolographicDisplay hologram : holograms) {
            if(getPlugin().getConfig().getBoolean(hologram.getDefaultPath() + "enabled")) {
                hologram.show();
            } else {
                hologram.hide();
            }
        }
    }

    public static BTEPlotSystem getPlugin() {
        return plugin;
    }

    public static List<HolographicDisplay> getHolograms() { return holograms; }


    public static class DependencyManager {

        // List with all missing dependencies
        private final static List<String> missingDependencies = new ArrayList<>();

        /**
         * Check for all required dependencies and inform in console about missing dependencies
         * @return True if all dependencies are present
         */
        private static boolean checkForRequiredDependencies() {
            PluginManager pluginManager = plugin.getServer().getPluginManager();

            if (!pluginManager.isPluginEnabled("Multiverse-Core")) {
                missingDependencies.add("Multiverse-Core (V2.5.0)");
            }

            if (!pluginManager.isPluginEnabled("WorldEdit")) {
                missingDependencies.add("WorldEdit (V6.1.9)");
            }

            if (!pluginManager.isPluginEnabled("WorldGuard")) {
                missingDependencies.add("WorldGuard (V6.2.2)");
            }

            if (!pluginManager.isPluginEnabled("FastAsyncWorldEdit")) {
                missingDependencies.add("FastAsyncWorldEdit (FAWE)");
            }

            if (!pluginManager.isPluginEnabled("HeadDatabase")) {
                missingDependencies.add("HeadDatabase");
            }

            if (!pluginManager.isPluginEnabled("VoidGen")) {
                missingDependencies.add("VoidGen (V2.0)");
            }

            return missingDependencies.isEmpty();
        }

        /**
         * @return True if HolographicDisplays is present
         */
        public static boolean isHolographicDisplaysEnabled() {
            return plugin.getServer().getPluginManager().isPluginEnabled("HolographicDisplays");
        }

        /**
         * @return Multiverse-Core instance
         */
        public static MultiverseCore getMultiverseCore() {
            return (MultiverseCore) plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
        }

        /**
         * @return World Edit instance
         */
        public static WorldEdit getWorldEdit() {
            return WorldEdit.getInstance();
        }

        /**
         * @return World Guard instance
         */
        public static WorldGuardPlugin getWorldGuard() {
            return WorldGuardPlugin.inst();
        }
    }
}
