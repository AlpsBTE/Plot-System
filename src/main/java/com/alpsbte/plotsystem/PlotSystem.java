/*
 *  The MIT License (MIT)
 *
 *  Copyright © 2021-2025, Alps BTE <bte.atchli@gmail.com>
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
import com.alpsbte.alpslib.io.database.DatabaseConfigPaths;
import com.alpsbte.alpslib.io.database.DatabaseConnection;
import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.alpslib.utils.head.AlpsHeadEventListener;
import com.alpsbte.plotsystem.commands.CommandManager;
import com.alpsbte.plotsystem.core.EventListener;
import com.alpsbte.plotsystem.core.holograms.HologramRegister;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotHandler;
import com.alpsbte.plotsystem.core.system.plot.generator.world.SkeletonWorldGenerator;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.BeginnerTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.Tutorial;
import com.alpsbte.plotsystem.core.system.tutorial.TutorialEventListener;
import com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialNPCTurnTracker;
import com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialUtils;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.google.common.io.CharStreams;
import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.ipvp.canvas.MenuFunctionListener;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class PlotSystem extends JavaPlugin {
    private static PlotSystem plugin;
    private boolean pluginEnabled = false;

    @Override
    public void onEnable() {
        YamlFileFactory.registerPlugin(this);
        plugin = this;
        Component successPrefix = text("[", DARK_GRAY).append(text("✔", DARK_GREEN)).append(text("]", DARK_GRAY)).append(text(" ", GRAY));
        Component errorPrefix = text("[", DARK_GRAY).append(text("X", RED)).append(text("]", DARK_GRAY)).append(text(" ", GRAY));

        // Load config, if it throws an exception disable plugin
        try {
            ConfigUtil.init();
            getComponentLogger().info(successPrefix.append(text("Successfully loaded configuration files.")));
        } catch (ConfigNotImplementedException ex) {
            getComponentLogger().error(errorPrefix.append(text("Could not load configuration file.")));
            getComponentLogger().info(text("The config file must be configured!", YELLOW));
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        reloadConfig();

        // Load language files
        try {
            LangUtil.init();
            getComponentLogger().info(successPrefix.append(text("Successfully loaded language files.")));
        } catch (Exception ex) {
            getComponentLogger().error(errorPrefix.append(text("Could not load language file.")), ex);
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize database connection
        try {
            initDatabase();
            getComponentLogger().info(successPrefix.append(text("Successfully initialized database connection.")));
        } catch (Exception ex) {
            getComponentLogger().error(errorPrefix.append(text("Could not initialize database connection.")), ex);
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
            getComponentLogger().info(successPrefix.append(text("Successfully registered event listeners.")));
        } catch (Exception ex) {
            getComponentLogger().error(errorPrefix.append(text("Could not register event listeners.")), ex);
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register commands
        try {
            CommandManager commandManager = new CommandManager();
            commandManager.init();
            getComponentLogger().info(successPrefix.append(text("Successfully registered commands.")));
        } catch (Exception ex) {
            getComponentLogger().error(errorPrefix.append(text("Could not register commands.")), ex);
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        DecentHologramDisplay.registerPlugin(this);
        HologramRegister.init();
        Utils.ChatUtils.checkForChatInputExpiry();
        PlotUtils.Effects.startTimer();

        // Start task that checks for and abandons inactive plots every hour
        Bukkit.getScheduler().runTaskTimerAsynchronously(PlotSystem.getPlugin(), PlotHandler::abandonInactivePlots, 0L, 20 * 60 * 60L);

        // Register tutorials
        if (getConfig().getBoolean(ConfigPaths.TUTORIAL_ENABLE)) {
            AbstractTutorial.registerTutorials(Collections.singletonList(BeginnerTutorial.class));
            Bukkit.getScheduler().runTaskTimerAsynchronously(FancyNpcsPlugin.get().getPlugin(), new TutorialNPCTurnTracker(), 0, 1L);
        }

        // Generate Skeleton World
        if (Bukkit.getWorld("Skeleton") == null) {
            getComponentLogger().info("No skeleton world found!");
            getComponentLogger().info("Generating skeleton world...");
            new SkeletonWorldGenerator();
        }

        pluginEnabled = true;
        getComponentLogger().info(text("Enabled Plot-System plugin.", DARK_GREEN));
        getComponentLogger().info(text("------------------------------------------------------", GOLD));
        getComponentLogger().info(text("> ", DARK_GRAY).append(text("Made by ", GRAY)).append(text("Alps BTE (AT/CH/LI)", RED)));
        getComponentLogger().info(text("> ", DARK_GRAY).append(text("GitHub: ", GRAY)).append(text("https://github.com/AlpsBTE/Plot-System", WHITE)));
        getComponentLogger().info(text("------------------------------------------------------", GOLD));
    }

    @Override
    public void onDisable() {
        if (!pluginEnabled) {
            getComponentLogger().info(text("Disabling plugin...", RED));
            getComponentLogger().info(text("------------------------------------------------------", GOLD));
            getComponentLogger().info(text("> ", DARK_GRAY).append(text("Made by ", GRAY)).append(text("Alps BTE (AT/CH/LI)", RED)));
            getComponentLogger().info(text("> ", DARK_GRAY).append(text("GitHub: ", GRAY)).append(text("https://github.com/AlpsBTE/Plot-System", WHITE)));
            getComponentLogger().info(text("------------------------------------------------------", GOLD));

            DecentHologramDisplay.activeDisplays.forEach(DecentHologramDisplay::delete);
        } else {
            // Close database connection
            DatabaseConnection.shutdown();

            // Unload plots
            for (UUID player : PlotUtils.Cache.getCachedInProgressPlots().keySet()) {
                Builder builder = Builder.byUUID(player);
                for (Plot plot : PlotUtils.Cache.getCachedInProgressPlots(builder)) {
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

    public void initDatabase() throws IOException, SQLException, ClassNotFoundException {
        DatabaseConnection.initializeDatabase(DatabaseConfigPaths.getConfig(getConfig()), true);
        var initScript = CharStreams.toString(Objects.requireNonNull(getTextResource("DATABASE.sql")));
        try (var con = DatabaseConnection.getConnection(); var s = con.createStatement()) {
            s.execute(initScript);
        }
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, String id) {
        return new SkeletonWorldGenerator.EmptyChunkGenerator();
    }
}