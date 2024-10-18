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

package com.alpsbte.plotsystem;

//import com.alpsbte.alpslib.hologram.HolographicDisplay;
import com.alpsbte.plotsystem.core.holograms.connector.DecentHologramDisplay;
import com.alpsbte.plotsystem.core.holograms.HologramRegister;
import com.alpsbte.alpslib.io.YamlFileFactory;
import com.alpsbte.alpslib.io.config.ConfigNotImplementedException;
import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.alpslib.utils.head.AlpsHeadEventListener;
import com.alpsbte.plotsystem.commands.*;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.core.system.tutorial.*;
import com.alpsbte.plotsystem.utils.PacketListener;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.EventListener;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.ipvp.canvas.MenuFunctionListener;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;

public class PlotSystem extends JavaPlugin {
    private static final String VERSION = "4.0";

    private static PlotSystem plugin;
    private CommandManager commandManager;

    private boolean pluginEnabled = false;

    @Override
    public void onEnable() {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog"); // Disable Logging
        YamlFileFactory.registerPlugin(this);
        li.cinnazeyy.langlibs.core.file.YamlFileFactory.registerPlugin(this);
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
            ConfigUtil.init();
            Bukkit.getConsoleSender().sendMessage(successPrefix + "Successfully loaded configuration files.");
        } catch (ConfigNotImplementedException ex) {
            Bukkit.getConsoleSender().sendMessage(errorPrefix + "Could not load configuration file.");
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "The config file must be configured!");

            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        reloadConfig();


        // Load language files
        try {
            LangUtil.init();
            Bukkit.getConsoleSender().sendMessage(successPrefix + "Successfully loaded language files.");
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage(errorPrefix + "Could not load language file.");
            Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);

            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

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
            this.getServer().getPluginManager().registerEvents(new AlpsHeadEventListener(), this);
            if (getConfig().getBoolean(ConfigPaths.TUTORIAL_ENABLE))
                this.getServer().getPluginManager().registerEvents(new TutorialEventListener(), this);
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

        if (!DependencyManager.isWorldGuardExtraFlagsEnabled()) Bukkit.getLogger().log(Level.WARNING, "WorldGuardExtraFlags is not enabled!");

        // Check for updates
        Bukkit.getConsoleSender().sendMessage("");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "Update-Checker:");

        UpdateChecker.getVersion(version -> {
            if (new ComparableVersion(VERSION).compareTo(new ComparableVersion(version)) >= 0) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "You are using the latest stable version.");
            } else {
                UpdateChecker.isUpdateAvailable = true;
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "You are using a outdated version!");
                Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "Latest version: " + ChatColor.GREEN + version + ChatColor.GRAY + " | Your version: " + ChatColor.RED + VERSION);
                Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + "Update here: " + ChatColor.AQUA + "https://github.com/AlpsBTE/Plot-System/releases");
            }
        });

        DecentHologramDisplay.registerPlugin(this);
        HologramRegister.init();
        PlotUtils.checkPlotsForLastActivity();
        PlotUtils.syncPlotSchematicFiles();
        Utils.ChatUtils.checkForChatInputExpiry();
        PlotUtils.Effects.startTimer();

        try {
            new PacketListener();
        } catch (NoClassDefFoundError ex) {
            Bukkit.getConsoleSender().sendMessage("");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Could not find Protocol-Lib! Consider installing it to avoid issues.");
        }

        // Cache and register custom heads
        Bukkit.getScheduler().runTaskAsynchronously(this, Utils::registerCustomHeads);

        // Register tutorials
        if (getConfig().getBoolean(ConfigPaths.TUTORIAL_ENABLE)) {
            AbstractTutorial.registerTutorials(Collections.singletonList(BeginnerTutorial.class));
        }

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

            HologramRegister.getActiveDisplays().forEach(DecentHologramDisplay::delete);
        } else {
            // Unload plots
            for (UUID player : PlotUtils.Cache.getCachedInProgressPlots().keySet()) {
                for (Plot plot : PlotUtils.Cache.getCachedInProgressPlots(Builder.byUUID(player))) {
                    if (plot != null) plot.getWorld().unloadWorld(true);
                }
            }

            // Unload tutorials
            for (int i = 0; i < AbstractTutorial.getActiveTutorials().size(); i++) {
                Tutorial tutorial = AbstractTutorial.getActiveTutorials().get(i);
                tutorial.onTutorialStop(tutorial.getPlayerUUID());
            }
        }
    }

    @Override
    public @NotNull FileConfiguration getConfig() {
        return ConfigUtil.getInstance().configs[0];
    }

    @Override
    public void reloadConfig() {
        ConfigUtil.getInstance().reloadFiles();
        ConfigUtil.getInstance().saveFiles();
        Utils.ChatUtils.setChatFormat(getConfig().getString(ConfigPaths.CHAT_FORMAT_INFO_PREFIX),
                getConfig().getString(ConfigPaths.CHAT_FORMAT_ALERT_PREFIX));
        String chatPrefix = getConfig().getString(ConfigPaths.TUTORIAL_CHAT_PREFIX);
        if (chatPrefix != null) TutorialUtils.CHAT_TASK_PREFIX_COMPONENT = AlpsUtils.deserialize(chatPrefix);
    }

    @Override
    public void saveConfig() {
        ConfigUtil.getInstance().saveFiles();
    }

    public static PlotSystem getPlugin() {
        return plugin;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }


    public static class DependencyManager {

        // List with all missing dependencies
        private final static List<String> missingDependencies = new ArrayList<>();

        /**
         * Check for all required dependencies and inform in console about missing dependencies
         * @return True if all dependencies are present
         */
        private static boolean checkForRequiredDependencies() {
            PluginManager pluginManager = plugin.getServer().getPluginManager();
            if (!pluginManager.isPluginEnabled("DecentHolograms")) {
                missingDependencies.add("DecentHolograms");
            }

            if (!pluginManager.isPluginEnabled("Multiverse-Core")) {
                missingDependencies.add("Multiverse-Core");
            }

            if (!pluginManager.isPluginEnabled("FastAsyncWorldEdit")) {
                missingDependencies.add("FastAsyncWorldEdit");
            }

            if (!pluginManager.isPluginEnabled("WorldGuard")) {
                missingDependencies.add("WorldGuard");
            }

            if (!pluginManager.isPluginEnabled("HeadDatabase")) {
                missingDependencies.add("HeadDatabase");
            }

            if (!pluginManager.isPluginEnabled("VoidGen")) {
                missingDependencies.add("VoidGen");
            }

            if (!pluginManager.isPluginEnabled("LangLibs")) {
                missingDependencies.add("LangLibs");
            }

            if (!pluginManager.isPluginEnabled("FancyNpcs")) {
                missingDependencies.add("FancyNpcs (https://mvn.alps-bte.com/repository/fancyNpcs/de/oliver/FancyNpcs/2.0.5/FancyNpcs-2.0.5.jar)");
            }

            return missingDependencies.isEmpty();
        }

        /**
         * @return True if ParticleNativeAPI is present
         */
        public static boolean isParticleNativeAPIEnabled() {
            return plugin.getServer().getPluginManager().isPluginEnabled("ParticleNativeAPI");
        }

        public static boolean isMultiverseInventoriesEnabled() {
            return plugin.getServer().getPluginManager().isPluginEnabled("Multiverse-Inventories");
        }

        public static boolean isWorldGuardExtraFlagsEnabled() {
            return plugin.getServer().getPluginManager().isPluginEnabled("WorldGuardExtraFlags");
        }

        /**
         * @param worldName Name of the world
         * @return Config path for the world
         */
        public static String getMultiverseInventoriesConfigPath(String worldName) {
            return PlotSystem.DependencyManager.isMultiverseInventoriesEnabled() ? Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("Multiverse-Inventories")).getDataFolder() + "/worlds/" + worldName : "";
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

        /**
         * @param worldName Name of the world
         * @return Config path for the world
         */
        public static String getWorldGuardConfigPath(String worldName) {
            return Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("WorldGuard")).getDataFolder() + "/worlds/" + worldName;
        }

        /**
         * @return Protocol Lib Instance
         */
        public static ProtocolManager getProtocolManager() { return ProtocolLibrary.getProtocolManager(); }
    }

    public static class UpdateChecker {
        private final static int RESOURCE_ID = 95757;
        private static boolean isUpdateAvailable = false;

        /**
         * Get the latest plugin version from SpigotMC
         * @param version Returns latest stable version
         */
        public static void getVersion(final Consumer<String> version) {
            try (InputStream inputStream = new URI("https://api.spigotmc.org/legacy/update.php?resource=" + RESOURCE_ID).toURL().openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    version.accept(scanner.next());
                }
            } catch (IOException | URISyntaxException ex) {
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