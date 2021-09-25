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

package com.alpsbte.plotsystem;

import com.alpsbte.plotsystem.commands.*;
import com.alpsbte.plotsystem.core.config.ConfigManager;
import com.alpsbte.plotsystem.core.holograms.HolographicDisplay;
import com.alpsbte.plotsystem.core.holograms.PlotsLeaderboard;
import com.alpsbte.plotsystem.core.holograms.ScoreLeaderboard;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.alpsbte.plotsystem.core.config.ConfigPaths;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.EventListener;
import com.alpsbte.plotsystem.core.config.ConfigNotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.ipvp.canvas.MenuFunctionListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.logging.Level;

public class PlotSystem extends JavaPlugin {

    private static final String VERSION = "2.0";

    private static PlotSystem plugin;
    private ConfigManager configManager;
    private CommandManager commandManager;

    private boolean pluginEnabled = false;

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

        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "------------------ Plot-System V" + VERSION + " ------------------");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "Starting plugin...");
        Bukkit.getConsoleSender().sendMessage("");

        // Check for required dependencies, if it returns false disable plugin
        if (!DependencyManager.checkForRequiredDependencies()) {
            Bukkit.getConsoleSender().sendMessage(errorPrefix + "Could not load required dependencies.");
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Missing Dependencies:");
            DependencyManager.missingDependencies.forEach(dependency -> Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + " - " + dependency));

            this.getServer().getPluginManager().disablePlugin(this);
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

            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.configManager.reloadConfigs();

        // Initialize database connection
        try {
            DatabaseConnection.InitializeDatabase();
            Bukkit.getConsoleSender().sendMessage(successPrefix + "Successfully initialized database connection.");
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage(errorPrefix + "Could not initialize database connection.");
            Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);

            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register event listeners
        try {
            this.getServer().getPluginManager().registerEvents(new EventListener(), this);
            this.getServer().getPluginManager().registerEvents(new MenuFunctionListener(), this);
            Bukkit.getConsoleSender().sendMessage(successPrefix + "Successfully registered event listeners.");
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage(errorPrefix + "Could not register event listeners.");
            Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);

            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register commands
        try {
            commandManager = new CommandManager();
            commandManager.init();
            Bukkit.getConsoleSender().sendMessage(successPrefix + "Successfully registered commands.");
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage(errorPrefix + "Could not register commands.");
            Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);

            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Check for extensions
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "Extensions:");

        if (DependencyManager.isHolographicDisplaysEnabled()) {
            reloadHolograms();
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "- HolographicDisplays (Leaderboards)");
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "No extensions enabled.");
        }

        // Check for updates
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "Update-Checker:");

        UpdateChecker.getVersion(version -> {
            if (version.equalsIgnoreCase(VERSION)) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "You are using the latest stable version.");
            } else {
                UpdateChecker.isUpdateAvailable = true;
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "You are using a outdated version!");
                Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "Latest version: " + ChatColor.GREEN + version + ChatColor.GRAY + " | Your version: " + ChatColor.RED + VERSION);
                Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "Update here: " + ChatColor.AQUA + "https://github.com/AlpsBTE/Plot-System/releases");
            }
        });

        PlotManager.checkPlotsForLastActivity();
        PlotManager.syncPlotSchematicFiles();

        pluginEnabled = true;
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GREEN + "Enabled Plot-System plugin.");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "------------------------------------------------------");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GRAY + "> " + ChatColor.GRAY + "Made by " + ChatColor.RED + "Alps BTE (AT/CH/LI)");
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GRAY + "> " + ChatColor.GRAY + "GitHub: " + ChatColor.WHITE + "https://github.com/AlpsBTE/Plot-System");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "------------------------------------------------------");
    }

    @Override
    public void onDisable() {
        if (!pluginEnabled) {
            Bukkit.getConsoleSender().sendMessage("");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Disabling plugin...");
            Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "------------------------------------------------------");
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GRAY + "> " + ChatColor.GRAY + "Made by " + ChatColor.RED + "Alps BTE (AT/CH/LI)");
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GRAY + "> " + ChatColor.GRAY + "GitHub: " + ChatColor.WHITE + "https://github.com/AlpsBTE/Plot-System");
            Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "------------------------------------------------------");
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override @Deprecated
    public FileConfiguration getConfig() {
        return this.configManager.getConfig();
    }

    @Override @Deprecated
    public void reloadConfig() {
        this.configManager.reloadConfigs();
    }

    @Override @Deprecated
    public void saveConfig() {
        this.configManager.saveConfigs();
    }

    public static void reloadHolograms() {
        if (DependencyManager.isHolographicDisplaysEnabled()) {
            for (HolographicDisplay hologram : holograms) {
                if(plugin.getConfigManager().getConfig().getBoolean(hologram.getDefaultPath() + ConfigPaths.HOLOGRAMS_ENABLED)) {
                    hologram.show();
                } else {
                    hologram.hide();
                }
            }
        }
    }

    public static PlotSystem getPlugin() {
        return plugin;
    }

    public CommandManager getCommandManager() {
        return commandManager;
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

    public static class UpdateChecker {
        private final static int RESOURCE_ID = 95757;
        private static boolean isUpdateAvailable = false;

        /**
         * Get latest plugin version from SpigotMC
         * @param version Returns latest stable version
         */
        public static void getVersion(final Consumer<String> version) {
            try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + RESOURCE_ID).openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    version.accept(scanner.next());
                }
            } catch (IOException ex) {
                Bukkit.getLogger().log(Level.WARNING, "Cannot look for new updates: " + ex.getMessage());
            }
        }

        /**
         * @return True if an update is available
         */
        public static boolean updateAvailable() {
            return isUpdateAvailable;
        }
    }
}
