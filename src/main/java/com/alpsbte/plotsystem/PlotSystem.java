/*
 * The MIT License (MIT)
 *
 *  Copyright © 2025, Alps BTE <bte.atchli@gmail.com>
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

import com.alpsbte.alpslib.hologram.DecentHologramDisplay;
import com.alpsbte.alpslib.io.YamlFileFactory;
import com.alpsbte.alpslib.io.config.ConfigNotImplementedException;
import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.alpslib.utils.head.AlpsHeadEventListener;
import com.alpsbte.plotsystem.commands.CommandManager;
import com.alpsbte.plotsystem.core.EventListener;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.holograms.HologramRegister;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.BeginnerTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.Tutorial;
import com.alpsbte.plotsystem.core.system.tutorial.TutorialEventListener;
import com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialNPCTurnTracker;
import com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialUtils;
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
import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import net.kyori.adventure.text.Component;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bukkit.Bukkit;
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

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class PlotSystem extends JavaPlugin {
    private static final String VERSION = "4.1.3";

    private static PlotSystem plugin;
    private CommandManager commandManager;

    private boolean pluginEnabled = false;

    @Override
    public void onEnable() {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog"); // Disable Logging
        YamlFileFactory.registerPlugin(this);
        li.cinnazeyy.langlibs.core.file.YamlFileFactory.registerPlugin(this);
        plugin = this;
        Component successPrefix = text("[", DARK_GRAY).append(text("✔", DARK_GREEN)).append(text("]", DARK_GRAY)).append(text(" ", GRAY));
        Component errorPrefix = text("[", DARK_GRAY).append(text("X", RED)).append(text("]", DARK_GRAY)).append(text(" ", GRAY));

        Bukkit.getConsoleSender().sendMessage(text("------------------ Plot-System V" + VERSION + " ------------------", GOLD));
        Bukkit.getConsoleSender().sendMessage(text("Starting plugin...", DARK_GREEN));
        Bukkit.getConsoleSender().sendMessage(empty());

        // Check for required dependencies, if it returns false disable plugin
        if (!DependencyManager.checkForRequiredDependencies()) {
            Bukkit.getConsoleSender().sendMessage(errorPrefix.append(text("Could not load required dependencies.")));
            Bukkit.getConsoleSender().sendMessage(text("Missing Dependencies:", YELLOW));
            DependencyManager.missingDependencies.forEach(dependency -> Bukkit.getConsoleSender().sendMessage(text(" - " + dependency, YELLOW)));

            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Bukkit.getConsoleSender().sendMessage(successPrefix.append(text("Successfully loaded required dependencies.")));

        // Load config, if it throws an exception disable plugin
        try {
            ConfigUtil.init();
            Bukkit.getConsoleSender().sendMessage(successPrefix.append(text("Successfully loaded configuration files.")));
        } catch (ConfigNotImplementedException ex) {
            Bukkit.getConsoleSender().sendMessage(errorPrefix.append(text("Could not load configuration file.")));
            Bukkit.getConsoleSender().sendMessage(text("The config file must be configured!", YELLOW));

            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        reloadConfig();

        // Load language files
        try {
            LangUtil.init();
            Bukkit.getConsoleSender().sendMessage(successPrefix.append(text("Successfully loaded language files.")));
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage(errorPrefix.append(text("Could not load language file.")));
            PlotSystem.getPlugin().getComponentLogger().error(text(ex.getMessage()), ex);

            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize database connection
        try {
            DatabaseConnection.InitializeDatabase();
            Bukkit.getConsoleSender().sendMessage(successPrefix.append(text("Successfully initialized database connection.")));
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage(errorPrefix.append(text("Could not initialize database connection.")));
            PlotSystem.getPlugin().getComponentLogger().error(text(ex.getMessage()), ex);

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
            Bukkit.getConsoleSender().sendMessage(successPrefix.append(text("Successfully registered event listeners.")));
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage(errorPrefix.append(text("Could not register event listeners.")));
            PlotSystem.getPlugin().getComponentLogger().error(text(ex.getMessage()), ex);

            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register commands
        try {
            commandManager = new CommandManager();
            commandManager.init();
            Bukkit.getConsoleSender().sendMessage(successPrefix.append(text("Successfully registered commands.")));
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage(errorPrefix.append(text("Could not register commands.")));
            PlotSystem.getPlugin().getComponentLogger().error(text(ex.getMessage()), ex);

            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!DependencyManager.isWorldGuardExtraFlagsEnabled()) PlotSystem.getPlugin().getComponentLogger().warn(text("WorldGuardExtraFlags is not enabled!"));

        // Check for updates
        Bukkit.getConsoleSender().sendMessage(empty());
        Bukkit.getConsoleSender().sendMessage(text("Update-Checker:", GOLD));

        UpdateChecker.getVersion(version -> {
            if (new ComparableVersion(VERSION).compareTo(new ComparableVersion(version)) >= 0) {
                Bukkit.getConsoleSender().sendMessage(text("You are using the latest stable version.", YELLOW));
            } else {
                UpdateChecker.isUpdateAvailable = true;
                Bukkit.getConsoleSender().sendMessage(text("You are using a outdated version!", RED));
                Bukkit.getConsoleSender().sendMessage(text("Latest version: ", GRAY).append(text(version, GREEN)).append(text(" | Your version: ", GRAY)).append(text(VERSION, RED)));
                Bukkit.getConsoleSender().sendMessage(text("Update here: ", GRAY).append(text("https://github.com/AlpsBTE/Plot-System/releases", AQUA)));
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
            Bukkit.getConsoleSender().sendMessage(empty());
            Bukkit.getConsoleSender().sendMessage(text("Could not find Protocol-Lib! Consider installing it to avoid issues.", RED));
        }

        // Cache and register custom heads
        Bukkit.getScheduler().runTaskAsynchronously(this, Utils::registerCustomHeads);

        // Register tutorials
        if (getConfig().getBoolean(ConfigPaths.TUTORIAL_ENABLE)) {
            AbstractTutorial.registerTutorials(Collections.singletonList(BeginnerTutorial.class));
            Bukkit.getScheduler().runTaskTimerAsynchronously(FancyNpcsPlugin.get().getPlugin(), new TutorialNPCTurnTracker(), 0, 1L);
        }

        pluginEnabled = true;
        Bukkit.getConsoleSender().sendMessage(empty());
        Bukkit.getConsoleSender().sendMessage(text("Enabled Plot-System plugin.", DARK_GREEN));
        Bukkit.getConsoleSender().sendMessage(text("------------------------------------------------------", GOLD));
        Bukkit.getConsoleSender().sendMessage(text("> ", DARK_GRAY).append(text("Made by ", GRAY)).append(text("Alps BTE (AT/CH/LI)", RED)));
        Bukkit.getConsoleSender().sendMessage(text("> ", DARK_GRAY).append(text("GitHub: ", GRAY)).append(text("https://github.com/AlpsBTE/Plot-System", WHITE)));
        Bukkit.getConsoleSender().sendMessage(text("------------------------------------------------------", GOLD));
    }

    @Override
    public void onDisable() {
        if (!pluginEnabled) {
            Bukkit.getConsoleSender().sendMessage(empty());
            Bukkit.getConsoleSender().sendMessage(text("Disabling plugin...", RED));
            Bukkit.getConsoleSender().sendMessage(text("------------------------------------------------------", GOLD));
            Bukkit.getConsoleSender().sendMessage(text("> ", DARK_GRAY).append(text("Made by ", GRAY)).append(text("Alps BTE (AT/CH/LI)", RED)));
            Bukkit.getConsoleSender().sendMessage(text("> ", DARK_GRAY).append(text("GitHub: ", GRAY)).append(text("https://github.com/AlpsBTE/Plot-System", WHITE)));
            Bukkit.getConsoleSender().sendMessage(text("------------------------------------------------------", GOLD));

            DecentHologramDisplay.activeDisplays.forEach(DecentHologramDisplay::delete);
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
        private static final List<String> missingDependencies = new ArrayList<>();

        /**
         * Check for all required dependencies and inform in console about missing dependencies
         *
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
                missingDependencies.add("FancyNpcs");
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
        public static ProtocolManager getProtocolManager() {return ProtocolLibrary.getProtocolManager();}
    }

    public static class UpdateChecker {
        private static final int RESOURCE_ID = 95757;
        private static boolean isUpdateAvailable = false;

        /**
         * Get the latest plugin version from SpigotMC
         *
         * @param version Returns latest stable version
         */
        public static void getVersion(final Consumer<String> version) {
            try (InputStream inputStream = new URI("https://api.spigotmc.org/legacy/update.php?resource=" + RESOURCE_ID).toURL().openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    version.accept(scanner.next());
                }
            } catch (IOException | URISyntaxException ex) {
                PlotSystem.getPlugin().getComponentLogger().warn(text("Cannot look for new updates:" + ex.getMessage()));
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