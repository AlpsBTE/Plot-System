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

package com.alpsbte.plotsystem.core.system.tutorial;

import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.holograms.TutorialHologram;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import com.alpsbte.plotsystem.core.system.plot.generator.TutorialPlotGenerator;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.io.TutorialPaths;
import com.sk89q.worldedit.Vector2D;
import me.filoghost.holographicdisplays.api.Position;
import me.filoghost.holographicdisplays.api.hologram.PlaceholderSetting;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public abstract class AbstractTutorial {
    public static List<AbstractTutorial> activeTutorials = new ArrayList<>();

    private final List<Class<? extends AbstractStage>> stages;
    private final Player player;
    private final TutorialPlot plot;
    private final TutorialHologram hologram = new TutorialHologram("tutorial-hologram");
    private TutorialPlotGenerator plotGenerator;
    private BukkitTask tutorialTask;
    private AbstractStage activeStage;
    private int activeStageIndex = 0;

    protected abstract List<Class<? extends AbstractStage>> setStages();

    protected AbstractTutorial(Builder builder, int tutorialId) throws SQLException {
        this.player = builder.getPlayer();
        stages = setStages();

        // Get tutorial plot
        if (TutorialPlot.getPlot(builder.getUUID().toString(), tutorialId) == null) {
            plot = TutorialPlot.addTutorialPlot(builder.getUUID().toString(), tutorialId);
        } else plot = TutorialPlot.getPlot(builder.getUUID().toString(), tutorialId);

        // Check if tutorial plot is null
        if (plot == null) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not load tutorial. Plot is null.");
            return;
        }

        // Generate tutorial world
        plotGenerator = new TutorialPlotGenerator(plot, builder);

        // Create tutorial hologram
        createHologram();

        // Set stage and add tutorial to active tutorials
        setStage(plot.getStage());
        activeTutorials.add(this);

        // Start tutorial
        final String worldName = plot.getWorld().getWorldName();
        tutorialTask = Bukkit.getScheduler().runTaskTimerAsynchronously(PlotSystem.getPlugin(), () -> {
            try {
                if (activeStage == null || !player.isOnline() || !player.getWorld().getName().equals(worldName)) StopTutorial(false);
                if (activeStage.getTaskTimeline().lastTaskId >= activeStage.getTaskTimeline().tasks.size() - 1) nextStage();
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        }, 0, 20);
    }

    private void setStage(int stageIndex) throws SQLException {
        activeStageIndex = stageIndex - 1;
        nextStage();
    }

    private void nextStage() throws SQLException {
        if (activeStageIndex + 1 >= stages.size()) {
            // TODO: Complete tutorial

            player.sendMessage("Tutorial Completed");
            StopTutorial(true);
        } else {
            try {
                // Switch to next stage
                activeStage = stages.get(activeStageIndex + 1).getDeclaredConstructor(TutorialPlot.class, TutorialHologram.class).newInstance(plot, hologram);
                activeStageIndex++;

                // Generate plot schematic for stage
                plotGenerator.generateOutlines(activeStage.initSchematicId);

                hologram.updateHeader(Material.valueOf(plot.getTutorialConfig().getString(TutorialPaths.TUTORIAL_ITEM_NAME)),
                        "§b§lStage " + (activeStageIndex + 1) + " §f§l◆ §6§l" + activeStage.getMessages().get(0));
                hologram.setFooterVisibility(false);

                ChatHandler.printInfo(player, ChatHandler.getStageUnlockedInfo(activeStage.getMessages().get(0), activeStage.getMessages().get(1)));
                player.playSound(player.getLocation(), activeStageIndex == 0 ? Utils.SoundUtils.TELEPORT_SOUND : Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                activeStage.getTaskTimeline().StartTimeline();

            } catch (Exception ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to initialize tutorial stage.", ex);
                player.sendMessage(Utils.ChatUtils.getErrorMessageFormat(LangUtil.getInstance().get(player, LangPaths.Message.Error.ERROR_OCCURRED)));
            }
        }
    }

    private void StopTutorial(boolean isCompleted) {
        if (tutorialTask != null) tutorialTask.cancel();
        activeTutorials.remove(this);
        if (activeStage != null) activeStage.getTaskTimeline().StopTimeline();
        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
            try {
                hologram.remove();
                plot.getWorld().unloadWorld(true);
                if (isCompleted) player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        });

        // TODO: save stage to database
    }

    private void createHologram() throws SQLException {
        // Load position from config and convert to vector
        String[] hologramPosition = plot.getTutorialConfig().getString(TutorialPaths.HOLOGRAM_COORDINATES).split(",");
        Vector2D hologramVector = new Vector2D(Double.parseDouble(hologramPosition[0]), Double.parseDouble(hologramPosition[1]));

        // Get the highest block at hologram position and add 2 to get the y coordinate
        int hologramY = plot.getWorld().getBukkitWorld().getHighestBlockYAt(hologramVector.getBlockX(), hologramVector.getBlockZ()) + 2;

        // Create hologram
        hologram.create(Position.of(new Location(plot.getWorld().getBukkitWorld(), hologramVector.getX(), hologramY , hologramVector.getZ())));
        hologram.getHologram().setPlaceholderSetting(PlaceholderSetting.ENABLE_ALL);
        hologram.setDefaultHologramHeight(hologramY);
    }



    // TODO: MOVE SOMEWHERE ELSE
    public static class ChatHandler {
        public static void printInfo(Player player, TextComponent[] messages) {
            player.sendMessage("");
            for (TextComponent message : messages) player.spigot().sendMessage(message);
            player.sendMessage("");
        }

        public static void printInfo(Player player, String[] messages) {
            player.sendMessage("");
            for (String message : messages) player.sendMessage(message);
            player.sendMessage("");
        }

        public static void printInfo(Player player, String message, String hoverText, String link) {
            TextComponent tc = new TextComponent();
            tc.setText(message);
            tc.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
            printInfo(player, new TextComponent[]{tc});
        }

        public static String[] getTaskMessage(String message, ChatColor color) {
            return new LoreBuilder().setDefaultColor(color).addLine("§8§l> §" + color.getChar() + message).build().toArray(new String[0]);
        }

        public static String[] getStageUnlockedInfo(String title, String description) {
            LoreBuilder builder = new LoreBuilder()
                .addLines("", " §b§l" + "NEW STAGE UNLOCKED", "  §f§l◆ §6§l" + title, ""); // TODO: set player lang
            String[] descriptionLines = description.split("%newline%");
            Arrays.stream(descriptionLines).forEach(desc -> builder.addLine("    §7§l▪ §f" + desc));
            return builder.build().toArray(new String[0]);
        }
    }
}
