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
import com.alpsbte.plotsystem.core.system.tutorial.tasks.CustomTask;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.io.TutorialPaths;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BeginnerTutorial extends AbstractTutorial {

    @Override
    protected List<Class<? extends AbstractStage>> setStages() {
        return Arrays.asList(
                Stage1.class,
                Stage2.class,
                Stage3.class,
                Stage4.class
        );
    }

    public BeginnerTutorial(Builder builder) throws SQLException {
        super(builder, TutorialCategory.BEGINNER.getId());
    }

    private static class Stage1 extends AbstractStage {
        public Stage1(TutorialPlot plot, TutorialHologram hologram) throws SQLException, IOException {
            super(plot, hologram);
        }

        @Override
        protected List<String> setMessages() {
            return Arrays.asList(
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_TITLE),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_DESC),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_1),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_2, "§8§l►§r§f"),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_3),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_4)
            );
        }

        @Override
        public StageTimeline setTasks() {
            return new StageTimeline(player, hologram)
                    .updateHologramContent(Arrays.asList(
                            getMessages().get(2),
                            getMessages().get(3),
                            "{empty}",
                            getMessages().get(4),
                            getMessages().get(5)
                    ), null)
                    .delay(15);
        }
    }

    private static class Stage2 extends AbstractStage {
        public Stage2(TutorialPlot plot, TutorialHologram hologram) throws SQLException, IOException {
            super(plot, hologram);
        }

        @Override
        protected List<String> setMessages() {
            return Arrays.asList(
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_TITLE),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_DESC),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_1),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_2),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_3, "§a§lGoogle Maps §8§l►§r§f"),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_4, "§a§lGoogle Earth §8§l►§r§f"),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_5, "§8§l/plot links§r§f"),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_6, "§8Google Maps§r§f"),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_6, "§8Google Earth§r§f"),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_7),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_8),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_9),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_10),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_11),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_12)
            );
        }

        @Override
        protected StageTimeline setTasks() throws IOException {
            return new StageTimeline(player, hologram)
                    .updateHologramContent(Arrays.asList(
                            getMessages().get(2),
                            getMessages().get(3),
                            "{empty}",
                            getMessages().get(4),
                            getMessages().get(5),
                            "{empty}",
                            getMessages().get(6)
                    ), null)
                    .delay(10)
                    .waitForConfirmation()
                    .updateHologramContent(Arrays.asList(
                            "§a§lGoogle Maps",
                            getMessages().get(7),
                            "{empty}",
                            getMessages().get(9),
                            getMessages().get(10),
                            getMessages().get(11)
                    ), Sound.UI_BUTTON_CLICK)
                    .delay(2)
                    .sendClickableChatMessage(LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_OPEN_LINK, "Google Maps"),
                            Sound.ENTITY_EXPERIENCE_ORB_PICKUP, "§a§lGoogle Maps", plot.getGoogleMapsLink())
                    .delay(8)
                    .waitForConfirmation()
                    .updateHologramContent(Arrays.asList(
                            "§a§lGoogle Earth",
                            getMessages().get(8),
                            "{empty}",
                            getMessages().get(12),
                            getMessages().get(13),
                            getMessages().get(14)
                    ), Sound.UI_BUTTON_CLICK)
                    .delay(2)
                    .sendClickableChatMessage(LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_OPEN_LINK, "Google Earth"),
                            Sound.ENTITY_EXPERIENCE_ORB_PICKUP, "§a§lGoogle Earth", plot.getGoogleEarthLink())
                    .delay(8);
        }
    }

    private static class Stage3 extends AbstractStage {
        public Stage3(TutorialPlot plot, TutorialHologram hologram) throws SQLException, IOException {
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
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE3_4, "§8§l" + "/tpll <lon> <lat>" + "§r§f"),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE3_5, "§a§l" + "4" + "§r§7"),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE3_6)
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
                String[] pointsSplit = c.trim().split(",");
                teleportPoints.add(new double[] {Double.parseDouble(pointsSplit[0]), Double.parseDouble(pointsSplit[1])});
            });

            return new StageTimeline(player, hologram)
                    .updateHologramContent(Arrays.asList(
                            getMessages().get(2),
                            getMessages().get(3),
                            "{empty}",
                            getMessages().get(4),
                            getMessages().get(5)
                    ), null)
                    .delay(5)
                    .sendChatMessage(setMessages().get(6), Sound.ENTITY_VILLAGER_AMBIENT)
                    .addTask(new CustomTask(player, () -> {
                        hologram.updateFooter(0, teleportPoints.size());
                        hologram.updateFooter(true);
                    }))
                    .addTeleportEvent(player, teleportPoints, 1, (double[] teleportPoint, int pointsRemaining) -> {
                        player.getWorld().getBlockAt((int) teleportPoint[0],
                                player.getWorld().getHighestBlockYAt(plot.getCenter().getBlockX(), plot.getCenter().getBlockZ()), (int) teleportPoint[1])
                                .setTypeIdAndData(Material.CONCRETE_POWDER.getId(), (byte) 5, false);
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                        hologram.updateFooter(teleportCoordinates.size() - pointsRemaining, teleportCoordinates.size());
                    })
                    .delay(1)
                    .sendChatMessage(getMessages().get(7), Utils.SoundUtils.FINISH_PLOT_SOUND);
        }
    }

    private static class Stage4 extends AbstractStage {

        public Stage4(TutorialPlot plot, TutorialHologram hologram) throws SQLException, IOException {
            super(plot, hologram);
        }

        @Override
        protected List<String> setMessages() {
            return Arrays.asList(
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE4_TITLE),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE4_DESC),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE4_1),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE4_2),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE4_3),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE4_4, "§a" + plot.getTutorialConfig().getInt(TutorialPaths.Beginner.HEIGHT) + "§r§7"),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE4_5, "§a" + plot.getTutorialConfig().getInt(TutorialPaths.Beginner.HEIGHT) + "§r§7"),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE4_6)
            );
        }

        @Override
        protected StageTimeline setTasks() {
            int height = plot.getTutorialConfig().getInt(TutorialPaths.Beginner.HEIGHT);
            int offset = plot.getTutorialConfig().getInt(TutorialPaths.Beginner.HEIGHT_OFFSET);

            StageTimeline stage = new StageTimeline(player, hologram);
            stage.updateHologramContent(Arrays.asList(
                    getMessages().get(2),
                    "{empty}",
                    getMessages().get(3)
            ), Sound.UI_BUTTON_CLICK)
            .delay(5)
            .sendChatMessage(setMessages().get(4), Sound.ENTITY_EXPERIENCE_ORB_PICKUP)
            .addTask(new CustomTask(player, () -> {
                hologram.updateFooter(0, 1);
                hologram.updateFooter(true);
            }))
            .addPlayerChatEvent(player, height, offset, 3, (isCorrect, attemptsLeft) -> {
                if (!isCorrect && attemptsLeft > 0) {
                    AbstractTutorial.ChatHandler.printInfo(player, AbstractTutorial.ChatHandler.getTaskMessage(getMessages().get(7), ChatColor.GRAY));
                } else {
                    hologram.updateFooter(1, 1);
                    stage.delay(1);
                    stage.sendChatMessage(isCorrect ? getMessages().get(5) : getMessages().get(6), Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
                }
            });
            return stage;
        }
    }
}
