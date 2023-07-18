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

import com.alpsbte.plotsystem.core.holograms.TutorialHologram;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.events.TeleportPointEventTask;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.conversion.CoordinateConversion;
import com.alpsbte.plotsystem.utils.conversion.projection.OutOfProjectionBoundsException;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.io.TutorialPaths;
import org.bukkit.Bukkit;
import org.bukkit.Sound;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class BeginnerTutorial extends AbstractTutorial {

    @Override
    protected List<Class<? extends AbstractStage>> setStages() {
        return Arrays.asList(
                Stage1.class,
                Stage2.class,
                Stage3.class
        );
    }

    public BeginnerTutorial(Builder builder) throws SQLException {
        super(builder, TutorialCategory.BEGINNER.getId());
    }

    private static class Stage1 extends AbstractStage {
        public Stage1(TutorialPlot plot, TutorialHologram hologram) throws SQLException {
            super(plot, hologram);
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
            try {
                return new StageTimeline(player, hologram)
                        .teleportPlayer(plot.getWorld().getSpawnPoint(null))
                        .delay(5)
                        .nextHologramPage(Sound.ENTITY_VILLAGER_AMBIENT, 8)
                        .nextHologramPage(Sound.ENTITY_VILLAGER_AMBIENT, 8)
                        .nextHologramPage(Sound.ENTITY_BLAZE_SHOOT, 11)
                        .nextHologramPage(Sound.ENTITY_VILLAGER_AMBIENT, 8)
                        .nextHologramPage(Sound.ENTITY_ZOMBIE_VILLAGER_AMBIENT, 8);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class Stage2 extends AbstractStage {
        public Stage2(TutorialPlot plot, TutorialHologram hologram) throws SQLException {
            super(plot, hologram);
        }

        @Override
        protected List<String> setMessages() {
            try {
                return Arrays.asList(
                        LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_TITLE),
                        LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_DESC),
                        LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_1),
                        LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_2),
                        LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_3, "§b" + plot.getGoogleMapsLink() + "§7"),
                        LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_4),
                        LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_5, "§b" + plot.getGoogleEarthLink() + "§7"),
                        LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_6, "§6" + "/plot links" + "§7")
                );
            } catch (IOException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "An error occurred while loading the tutorial messages!", ex);
            }
            return null;
        }

        @Override
        protected StageTimeline setTasks() {
            StageTimeline timeline = new StageTimeline(player, hologram);
            timeline.delay(DEFAULT_STAGE_DELAY);
            for (int i = 2; i < getMessages().size(); i++) timeline.sendChatMessage(getMessages().get(i), Sound.ENTITY_VILLAGER_AMBIENT).delay(2);
            return timeline;
        }
    }

    private static class Stage3 extends AbstractStage {
        public Stage3(TutorialPlot plot, TutorialHologram hologram) throws SQLException {
            super(plot, hologram);
        }

        @Override
        protected List<String> setMessages() {
            return Arrays.asList(
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE3_TITLE),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE3_DESC),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE3_1),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE3_2),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE3_3),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE3_4)
            );
        }

        @Override
        protected StageTimeline setTasks() {
            List<double[]> teleportPoints = new ArrayList<>();
            List<String> teleportCoordinates = Arrays.asList(
                    plot.getTutorialConfig().getString(TutorialPaths.Beginner.POINT_1),
                    plot.getTutorialConfig().getString(TutorialPaths.Beginner.POINT_2),
                    plot.getTutorialConfig().getString(TutorialPaths.Beginner.POINT_3),
                    plot.getTutorialConfig().getString(TutorialPaths.Beginner.POINT_4)
            );
            teleportCoordinates.forEach(c -> {
                double[] points = new double[2];
                String[] pointsSplit = c.trim().split(",");
                points[0] = Double.parseDouble(pointsSplit[0]);
                points[1] = Double.parseDouble(pointsSplit[1]);
                try {
                    teleportPoints.add(CoordinateConversion.convertFromGeo(points[0], points[1]));
                } catch (OutOfProjectionBoundsException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "An error occurred while converting coordinates", ex);
                }
            });

            return new StageTimeline(player, hologram)
                    .sendChatMessage("Teleport to the given points", Sound.ENTITY_VILLAGER_AMBIENT).delay(2)
                    .addTask(new TeleportPointEventTask(player, teleportPoints, 1, (double[] teleportPoint) -> {
                        player.sendMessage("§7Teleporting to §b" + teleportPoint[0] + "§7, §b" + teleportPoint[1] + "§7...");
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    }))
                    .delay(1)
                    .sendChatMessage("Done", Utils.SoundUtils.FINISH_PLOT_SOUND);
        }
    }
}
