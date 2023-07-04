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
import com.alpsbte.plotsystem.core.system.tutorial.tasks.events.TeleportPointEventTask;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BeginnerTutorial extends AbstractTutorial {

    @Override
    protected List<Class<? extends AbstractStage>> setStages() {
        return Arrays.asList(
                Stage1.class,
                Stage2.class,
                Stage3.class
        );
    }

    public BeginnerTutorial(Builder builder) {
        super(builder);
    }

    private static class Stage1 extends AbstractStage {
        public Stage1(Player player) {
            super(player);
        }

        @Override
        protected List<String> setMessages() {
            return Arrays.asList(
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_TITLE),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_DESC),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_1),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_2),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_3),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_4),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_5)
            );
        }

        @Override
        public StageTimeline setTasks() {
            return new StageTimeline(player)
                    .teleportPlayer(Utils.getSpawnLocation()).delay(DEFAULT_STAGE_DELAY)
                    .sendMessage(getMessages().get(2), Sound.ENTITY_VILLAGER_AMBIENT).delay(5)
                    .sendMessage(getMessages().get(3), Sound.ENTITY_VILLAGER_AMBIENT).delay(5)
                    .sendMessage(getMessages().get(4), Sound.ENTITY_VILLAGER_AMBIENT).delay(6)
                    .sendMessage(getMessages().get(5), Sound.ENTITY_VILLAGER_AMBIENT).delay(5)
                    .sendMessage(getMessages().get(6), Sound.ENTITY_VILLAGER_AMBIENT).delay(5);
        }
    }

    private static class Stage2 extends AbstractStage {
        public Stage2(Player player) {
            super(player);
        }

        @Override
        protected List<String> setMessages() {
            return Arrays.asList(
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_TITLE),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_DESC),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_1),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_2),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_3, "§b" + "https://goo.gl/maps/FY5zbFCoUSm1iWgd7" + "§7"),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_4),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_5, "§b" + "https://earth.google.com/web/search/48.209560382869,+16.50040542606851" + "§7"),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_6, "§6" + "/plot links" + "§7")
            );
        }

        @Override
        protected StageTimeline setTasks() {
            StageTimeline timeline = new StageTimeline(player);
            timeline.delay(DEFAULT_STAGE_DELAY);
            for (int i = 2; i < getMessages().size(); i++) timeline.sendMessage(getMessages().get(i), Sound.ENTITY_VILLAGER_AMBIENT).delay(5);
            return timeline;
        }
    }

    private static class Stage3 extends AbstractStage {
        public Stage3(Player player) {
            super(player);
        }

        @Override
        protected List<String> setMessages() {
            return null;
        }

        @Override
        protected StageTimeline setTasks() {
            FileConfiguration config = PlotSystem.getPlugin().getConfig();
            ConfigurationSection teleportPointsSection = config.getConfigurationSection(ConfigPaths.TUTORIAL_BEGINNER_TELEPORT_POINTS);
            List<double[]> teleportPoints = new ArrayList<>();
            teleportPointsSection.getKeys(false).forEach(t -> teleportPoints.add(new double[]{
                    config.getDouble(String.join("", ConfigPaths.TUTORIAL_BEGINNER_TELEPORT_POINTS, ".", t, ".x")),
                    config.getDouble(String.join("", ConfigPaths.TUTORIAL_BEGINNER_TELEPORT_POINTS, ".", t, ".z"))
            }));

            StageTimeline timeline = new StageTimeline(player)
                    .sendMessage("Teleport to the given points", Sound.ENTITY_VILLAGER_AMBIENT).delay(2)
                    .addTask(new TeleportPointEventTask(player, teleportPoints, 1)).delay(1)
                    .sendMessage("Done", Utils.SoundUtils.FINISH_PLOT_SOUND);
            return timeline;
        }
    }
}
