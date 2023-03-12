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

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.items.builder.LoreBuilder;
import com.alpsbte.plotsystem.utils.io.language.LangPaths;
import com.alpsbte.plotsystem.utils.io.language.LangUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public abstract class AbstractTutorial {
    public static List<AbstractTutorial> activeTutorials = new ArrayList<>();

    private final List<Class<? extends AbstractStage>> stages;
    protected Builder builder;
    protected Player player;
    protected BukkitTask tutorialTask;
    protected AbstractStage activeStage;
    private int activeStageIndex = 0;

    protected abstract List<Class<? extends AbstractStage>> setStages();

    protected AbstractTutorial(Builder builder) {
        this(builder, 0);
    }

    protected AbstractTutorial(Builder builder, int stageIndex) {
        this.builder = builder;
        this.player = builder.getPlayer();
        activeTutorials.add(this);

        stages = setStages();
        SetStage(stageIndex);
        tutorialTask = Bukkit.getScheduler().runTaskTimerAsynchronously(PlotSystem.getPlugin(), () -> {
            if (!player.isOnline()) StopTutorial();
            if (activeStage.taskTimeline.currentTaskId >= activeStage.taskTimeline.tasks.size() - 1) NextStage();
        }, 0, 20 / 2); // every half tick
    }

    private void SetStage(int stageIndex) {
        activeStageIndex = stageIndex - 1;
        NextStage();
    }

    private void NextStage() {
        if (activeStageIndex + 1 >= stages.size()) {
            // TODO: complete tutorial
            StopTutorial();
        } else {
            try {
                // Switch to next stage
                activeStage = stages.get(activeStageIndex + 1).getDeclaredConstructor(Player.class).newInstance(builder.getPlayer());
                activeStageIndex++;
                ChatHandler.printInfo(player, ChatHandler.getStageUnlockedInfo(activeStage.setMessages().get(0), activeStage.setMessages().get(1)));
                player.playSound(player.getLocation(), Utils.CreatePlotSound, 1f, 1f);
                activeStage.taskTimeline.StartTimeline();
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to initialize tutorial stage.", ex);
                player.sendMessage(Utils.getErrorMessageFormat(LangUtil.get(player, LangPaths.Message.Error.ERROR_OCCURRED)));
            } catch (InterruptedException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to initialize tutorial task.", ex);
                player.sendMessage(Utils.getErrorMessageFormat(LangUtil.get(player, LangPaths.Message.Error.ERROR_OCCURRED)));
            }
        }
    }

    private void StopTutorial() {
        if (tutorialTask != null) tutorialTask.cancel();
        activeTutorials.remove(this);
    }

    public static class ChatHandler {
        public static void printInfo(Player player, String[] messages) {
            Arrays.stream(messages).forEach(player::sendMessage);
        }

        public static String[] getTaskMessage(String message) {
            return new LoreBuilder().setDefaultColor(ChatColor.GRAY).addLine("").addLine("§8§l> §7" + message).addLine("").build().toArray(new String[0]);
        }

        public static String[] getStageUnlockedInfo(String title, String description) {
            LoreBuilder builder = new LoreBuilder()
                .addLines("", " §6§l" + "NEW STAGE UNLOCKED", "  §8◆ §b" + title, ""); // TODO: set player lang
            String[] descriptionLines = description.split("\n");
            Arrays.stream(descriptionLines).forEach(desc -> builder.addLine("    §7▪ §f" + desc));
            builder.addLine("");
            return builder.build().toArray(new String[0]);
        }
    }
}
