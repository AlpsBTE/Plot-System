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
import com.alpsbte.plotsystem.core.system.tutorial.tasks.events.worldedit.LineCmdEventTask;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.events.worldedit.WandCmdEventTask;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.io.TutorialPaths;
import com.sk89q.worldedit.Vector;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class BeginnerTutorial extends AbstractTutorial {

    @Override
    protected List<Class<? extends AbstractStage>> setStages() {
        return Arrays.asList(
                Stage1.class,
                Stage2.class,
                Stage3.class,
                Stage4.class,
                Stage5.class,
                Stage6.class
        );
    }

    public BeginnerTutorial(Builder builder) throws SQLException {
        super(builder, TutorialCategory.BEGINNER.getId());
    }

    private static class Stage1 extends AbstractStage {
        public Stage1(TutorialPlot plot, TutorialHologram hologram) throws SQLException, IOException {
            super(plot, hologram, 0);
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
        protected StageTimeline setTasks() {
            return new StageTimeline(player, hologram)
                    .updateHologramContent(Arrays.asList(
                            getMessages().get(2),
                            getMessages().get(3),
                            getEmptyLine(),
                            getMessages().get(4),
                            getMessages().get(5)
                    ), null)
                    .delay(15);
        }
    }

    private static class Stage2 extends AbstractStage {
        public Stage2(TutorialPlot plot, TutorialHologram hologram) throws SQLException, IOException {
            super(plot, hologram, 0);
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
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_6, "Google Maps"),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_6, "Google Earth"),
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
                            getEmptyLine(),
                            getMessages().get(4),
                            getMessages().get(5),
                            getEmptyLine(),
                            getMessages().get(6)
                    ), Sound.UI_BUTTON_CLICK)
                    .delay(10)
                    .waitForConfirmation()
                    .updateHologramContent(Arrays.asList(
                            "§a§lGoogle Maps",
                            getMessages().get(7),
                            getEmptyLine(),
                            getMessages().get(9),
                            getMessages().get(10),
                            getMessages().get(11)
                    ), Sound.UI_BUTTON_CLICK)
                    .delay(2)
                    .sendClickableChatMessage(LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_OPEN_LINK, "Google Maps"),
                            Sound.ENTITY_EXPERIENCE_ORB_PICKUP, "§aGoogle Maps", plot.getGoogleMapsLink())
                    .delay(8)
                    .waitForConfirmation()
                    .updateHologramContent(Arrays.asList(
                            "§a§lGoogle Earth",
                            getMessages().get(8),
                            getEmptyLine(),
                            getMessages().get(12),
                            getMessages().get(13),
                            getMessages().get(14)
                    ), Sound.UI_BUTTON_CLICK)
                    .delay(2)
                    .sendClickableChatMessage(LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_OPEN_LINK, "Google Earth"),
                            Sound.ENTITY_EXPERIENCE_ORB_PICKUP, "§aGoogle Earth", plot.getGoogleEarthLink())
                    .delay(8);
        }
    }

    private static class Stage3 extends AbstractStage {
        public Stage3(TutorialPlot plot, TutorialHologram hologram) throws SQLException, IOException {
            super(plot, hologram, 0);
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
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE3_5, "§a" + "4" + "§r§7"),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE3_6)
            );
        }

        @Override
        protected StageTimeline setTasks() {
            return new StageTimeline(player, hologram)
                    .updateHologramContent(Arrays.asList(
                            getMessages().get(2),
                            getMessages().get(3),
                            getEmptyLine(),
                            getMessages().get(4),
                            getMessages().get(5)
                    ), Sound.UI_BUTTON_CLICK)
                    .delay(5)
                    .sendChatMessage(setMessages().get(6), Sound.ENTITY_EXPERIENCE_ORB_PICKUP)
                    .addTeleportEvent(player, getBuildingPoints(player, plot), 1, (teleportPoint) -> {
                        Utils.TutorialUtils.setBlockAt(player.getWorld(), teleportPoint, Material.CONCRETE_POWDER, (byte) 5);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 1f);
                    })
                    .delay(1)
                    .sendChatMessage(getMessages().get(7), Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        }
    }

    private static class Stage4 extends AbstractStage {
        public Stage4(TutorialPlot plot, TutorialHologram hologram) throws SQLException, IOException {
            super(plot, hologram, 0);
        }

        @Override
        protected List<String> setMessages() {
            return Arrays.asList(
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE4_TITLE),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE4_DESC),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE4_1),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE4_2),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE4_3, "§a//wand§r§7")
            );
        }

        @Override
        protected StageTimeline setTasks() {
            return new StageTimeline(player, hologram)
                .updateHologramContent(Arrays.asList(
                        getMessages().get(2),
                        getMessages().get(3)
                ), Sound.UI_BUTTON_CLICK)
                .delay(5)
                .sendChatMessage(getMessages().get(4), Sound.ENTITY_EXPERIENCE_ORB_PICKUP)
                .addTask(new WandCmdEventTask(player))
                .playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        }
    }

    private static class Stage5 extends AbstractStage {
        public Stage5(TutorialPlot plot, TutorialHologram hologram) throws SQLException, IOException {
            super(plot, hologram, 0);
        }

        @Override
        protected List<String> setMessages() {
            return Arrays.asList(
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE5_TITLE),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE5_DESC),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE5_1),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE5_2, "§8§l//line <pattern>§r§f"),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE5_3,
                            "§a" + LangUtil.getInstance().get(player, LangPaths.Note.Action.RIGHT_CLICK) + "§f",
                                    "§a" + LangUtil.getInstance().get(player, LangPaths.Note.Action.LEFT_CLICK) + "§f"),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE5_4, "§a//line wool§r§7"),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE5_5)
            );
        }

        @Override
        protected StageTimeline setTasks() {
            List<Vector> buildingPoints = getBuildingPoints(player, plot);

            // Map building points to lines
            Map<Vector, Vector> buildingLinePoints = new HashMap<>();
            buildingLinePoints.put(buildingPoints.get(0), buildingPoints.get(1));
            buildingLinePoints.put(buildingPoints.get(1), buildingPoints.get(2));
            buildingLinePoints.put(buildingPoints.get(2), buildingPoints.get(3));
            buildingLinePoints.put(buildingPoints.get(3), buildingPoints.get(0));

            return new StageTimeline(player, hologram)
                    .updateHologramContent(Arrays.asList(
                            getMessages().get(2),
                            getEmptyLine(),
                            getMessages().get(3),
                            getMessages().get(4)
                    ), Sound.UI_BUTTON_CLICK)
                    .delay(5)
                    .sendChatMessage(getMessages().get(5), Sound.ENTITY_EXPERIENCE_ORB_PICKUP)
                    .addTask(new LineCmdEventTask(player, plot.getTutorialConfig().getString(TutorialPaths.Beginner.BASE_BLOCK),
                            plot.getTutorialConfig().getInt(TutorialPaths.Beginner.BASE_BLOCK_ID), buildingLinePoints, ((minPoint, maxPoint) -> {
                        buildingLinePoints.remove(minPoint);
                        Utils.TutorialUtils.setBlockAt(player.getWorld(), minPoint, Material.CONCRETE_POWDER, (byte) 5);
                        Utils.TutorialUtils.setBlockAt(player.getWorld(), maxPoint, Material.CONCRETE_POWDER, (byte) 5);
                    })))
                    .delay(1)
                    .sendChatMessage(getMessages().get(6), Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        }
    }

    private static class Stage6 extends AbstractStage {
        public Stage6(TutorialPlot plot, TutorialHologram hologram) throws SQLException, IOException {
            super(plot, hologram, 1);
        }

        @Override
        protected List<String> setMessages() {
            return Arrays.asList(
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE6_TITLE),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE6_DESC),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE6_1),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE6_2),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE6_3),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE6_4, "§a" + plot.getTutorialConfig().getInt(TutorialPaths.Beginner.HEIGHT) + "§r§7"),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE6_5, "§a" + plot.getTutorialConfig().getInt(TutorialPaths.Beginner.HEIGHT) + "§r§7"),
                    LangUtil.getInstance().get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE6_6)
            );
        }

        @Override
        protected StageTimeline setTasks() {
            int height = plot.getTutorialConfig().getInt(TutorialPaths.Beginner.HEIGHT);
            int offset = plot.getTutorialConfig().getInt(TutorialPaths.Beginner.HEIGHT_OFFSET);

            StageTimeline stage = new StageTimeline(player, hologram);
            stage.updateHologramContent(Arrays.asList(
                    getMessages().get(2),
                    getEmptyLine(),
                    getMessages().get(3)
            ), Sound.UI_BUTTON_CLICK)
            .delay(5)
            .sendChatMessage(setMessages().get(4), Sound.ENTITY_EXPERIENCE_ORB_PICKUP)
            .addPlayerChatEvent(player, height, offset, 3, (isCorrect, attemptsLeft) -> {
                if (!isCorrect && attemptsLeft > 0) {
                    AbstractTutorial.ChatHandler.printInfo(player, AbstractTutorial.ChatHandler.getTaskMessage(getMessages().get(7), ChatColor.GRAY));
                } else {
                    stage.delay(1);
                    stage.sendChatMessage(isCorrect ? getMessages().get(5) : getMessages().get(6), Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
                }
            });
            return stage;
        }
    }

    /**
     * Get the building points from the config and convert them to Vector
     * @param player player who is doing the tutorial
     * @param plot plot where the tutorial is taking place
     * @return list of building points as Vector
     */
    private static List<Vector> getBuildingPoints(Player player, TutorialPlot plot) {
        // Read coordinates from config
        List<String> buildingPointsAsString = Arrays.asList(
                plot.getTutorialConfig().getString(TutorialPaths.Beginner.POINT_1),
                plot.getTutorialConfig().getString(TutorialPaths.Beginner.POINT_2),
                plot.getTutorialConfig().getString(TutorialPaths.Beginner.POINT_3),
                plot.getTutorialConfig().getString(TutorialPaths.Beginner.POINT_4)
        );

        // Convert coordinates to Vector
        List<Vector> buildingPoints = new ArrayList<>();
        buildingPointsAsString.forEach(c -> {
            String[] pointsSplit = c.trim().split(",");
            double x = Double.parseDouble(pointsSplit[0]);
            double z = Double.parseDouble(pointsSplit[1]);
            double y = player.getWorld().getHighestBlockYAt((int) x, (int) z);
            buildingPoints.add(new Vector(x, y, z));
        });

        return buildingPoints;
    }
}
