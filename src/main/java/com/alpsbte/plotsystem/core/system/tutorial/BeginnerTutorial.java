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

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import com.alpsbte.plotsystem.core.system.tutorial.stage.AbstractPlotStage;
import com.alpsbte.plotsystem.core.system.tutorial.stage.AbstractStage;
import com.alpsbte.plotsystem.core.system.tutorial.stage.StageTimeline;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.events.commands.LineCmdEventTask;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.events.commands.WandCmdEventTask;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.message.ChatMessageTask;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.io.TutorialPaths;
import com.sk89q.worldedit.Vector;
import net.md_5.bungee.api.chat.ClickEvent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

import static net.md_5.bungee.api.ChatColor.*;

public class BeginnerTutorial extends AbstractPlotTutorial {
    @Override
    protected List<TutorialWorld> setWorlds() {
        try {
            return Arrays.asList(
                    new TutorialWorld(tutorialId, 0),
                    new TutorialWorld(tutorialId, 1, plot.getWorld().getWorldName())
            );
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
        return null;
    }

    @Override
    protected List<Class<? extends AbstractStage>> setStages() {
        return Arrays.asList(
                Stage1.class,
                Stage2.class,
                Stage3.class,
                Stage4.class,
                Stage5.class,
                Stage6.class,
                Stage7.class,
                Stage8.class,
                Stage9.class,
                Stage10.class
        );
    }

    public BeginnerTutorial(Player player) throws SQLException {
        super(player, TutorialCategory.BEGINNER.getId());
    }

    private static class Stage1 extends AbstractPlotStage {
        protected Stage1(Player player, TutorialPlot plot) throws SQLException, IOException {
            super(player, 0, plot, -1);
        }

        @Override
        public List<String> setMessages() {
            return Arrays.asList(
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_TITLE),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_DESC),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_1, GOLD + getPlayer().getName() + GRAY),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_2),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_3),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_4)
            );
        }

        @Override
        public StageTimeline getTimeline() {
            return new StageTimeline(getPlayer())
                    // TODO: Add task to click on the NPC to continue as player task
                    .delay(2)
                    .sendChatMessage(getMessages().get(2), Sound.ENTITY_VILLAGER_AMBIENT, true)
                    .sendChatMessage(getMessages().get(3), Sound.ENTITY_VILLAGER_AMBIENT, true)
                    .sendChatMessage(getMessages().get(4), Sound.ENTITY_VILLAGER_AMBIENT, true)
                    .sendChatMessage(getMessages().get(5), Sound.ENTITY_VILLAGER_AMBIENT, true);
        }
    }

    private static class Stage2 extends AbstractPlotStage {
        public static final String GOOGLE_MAPS = "Google Maps" + GRAY;
        public static final String GOOGLE_EARTH = "Google Earth" + GRAY;

        protected Stage2(Player player, TutorialPlot plot) throws SQLException, IOException {
            super(player, 0, plot, -1);
        }

        @Override
        public List<String> setMessages() {
            return Arrays.asList(
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_TITLE),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_DESC),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_1, GOLD + GOOGLE_MAPS, GOLD + GOOGLE_EARTH),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_2, GOLD + GOOGLE_MAPS),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Note.Action.CLICK_TO_OPEN_LINK, GREEN + GOOGLE_MAPS),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_3, GOLD + GOOGLE_EARTH),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Note.Action.CLICK_TO_OPEN_LINK, GREEN + GOOGLE_EARTH),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_4, GOLD + "/plot links" + GRAY)
            );
        }

        @Override
        public StageTimeline getTimeline() throws IOException {
            return new StageTimeline(getPlayer())
                    .delay(2)
                    .sendChatMessage(getMessages().get(2), Sound.ENTITY_VILLAGER_AMBIENT, true)
                    .sendChatMessage(new Object[] {
                            getMessages().get(3),
                            StringUtils.EMPTY,
                            new ChatMessageTask.ClickableTaskMessage(getMessages().get(4), GRAY + GOOGLE_MAPS, getPlot().getGoogleMapsLink(), ClickEvent.Action.OPEN_URL)
                    }, Sound.ENTITY_VILLAGER_AMBIENT, true)
                    .sendChatMessage(new Object[] {
                            getMessages().get(5),
                            StringUtils.EMPTY,
                            new ChatMessageTask.ClickableTaskMessage(getMessages().get(6), GRAY + GOOGLE_EARTH, getPlot().getGoogleEarthLink(), ClickEvent.Action.OPEN_URL)
                    }, Sound.ENTITY_VILLAGER_AMBIENT, true)
                    .sendChatMessage(getMessages().get(7), Sound.ENTITY_VILLAGER_AMBIENT, true);
        }
    }

    private static class Stage3 extends AbstractPlotStage {
        protected Stage3(Player player, TutorialPlot plot) throws SQLException, IOException {
            super(player, 1, plot, 0);
        }

        @Override
        public List<String> setMessages() {
            return Arrays.asList(
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE3_TITLE),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE3_DESC),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE3_1),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE3_2),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE3_3, Stage2.GOOGLE_MAPS),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE3_4, GOLD + "/tpll <lat> <lon>" + GRAY),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE3_5, GREEN + "4" + GRAY),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE3_6)
            );
        }

        @Override
        public StageTimeline getTimeline() throws SQLException, IOException {
            return new StageTimeline(getPlayer())
                    .delay(3)
                    .sendChatMessage(getMessages().get(2), Sound.ENTITY_VILLAGER_AMBIENT, true)
                    .sendChatMessage(getMessages().get(3), Sound.ENTITY_VILLAGER_AMBIENT, true)
                    .sendChatMessage(new ChatMessageTask.ClickableTaskMessage(getMessages().get(4), GRAY + Stage2.GOOGLE_MAPS,
                            getPlot().getGoogleMapsLink(), ClickEvent.Action.OPEN_URL) ,Sound.ENTITY_VILLAGER_AMBIENT, false)
                    .delay(2)
                    // TODO: add player task information
                    .addTeleportEvent(getBuildingPoints(getPlot()), 1, (teleportPoint) -> {
                        Utils.TutorialUtils.setBlockAt(getPlayer().getWorld(), teleportPoint, Material.CONCRETE_POWDER, (byte) 5);
                        getPlayer().playSound(new Location(getPlayer().getWorld(), teleportPoint.getBlockX(), teleportPoint.getBlockY(), teleportPoint.getBlockZ()),
                                Sound.BLOCK_NOTE_PLING, 1f, 1f);
                    })
                    .delay(1)
                    .sendChatMessage(getMessages().get(7), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, true);
        }
    }

    private static class Stage4 extends AbstractPlotStage {
        protected Stage4(Player player, TutorialPlot plot) throws SQLException, IOException {
            super(player, 1, plot, 0);
        }

        @Override
        public List<String> setMessages() {
            return Arrays.asList(
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE4_TITLE),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE4_DESC),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE4_1),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE4_2),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE4_3, GOLD +"//wand" + GRAY)
            );
        }

        @Override
        public StageTimeline getTimeline() {
            return new StageTimeline(getPlayer())
                    .sendChatMessage(getMessages().get(2), Sound.ENTITY_VILLAGER_AMBIENT, true)
                    .sendChatMessage(getMessages().get(3), Sound.ENTITY_VILLAGER_AMBIENT, true)
                    .sendChatMessage(getMessages().get(4), Sound.ENTITY_VILLAGER_AMBIENT, false)
                    .addTask(new WandCmdEventTask(getPlayer()))
                    .playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        }
    }

    private static class Stage5 extends AbstractPlotStage {
        private final static String BASE_BLOCK = ConfigUtil.getTutorialInstance().getBeginnerTutorial().getString(TutorialPaths.Beginner.BASE_BLOCK);
        private final static int BASE_BLOCK_ID = ConfigUtil.getTutorialInstance().getBeginnerTutorial().getInt(TutorialPaths.Beginner.BASE_BLOCK_ID);

        protected Stage5(Player player, TutorialPlot plot) throws SQLException, IOException {
            super(player, 1, plot, 0);
        }

        @Override
        public List<String> setMessages() {
            return Arrays.asList(
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE5_TITLE),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE5_DESC),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE5_1),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE5_2, "§8§l//line <pattern>§r§f"),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE5_3,
                            "§a" + LangUtil.getInstance().get(getPlayer(), LangPaths.Note.Action.RIGHT_CLICK) + "§f",
                                    "§a" + LangUtil.getInstance().get(getPlayer(), LangPaths.Note.Action.LEFT_CLICK) + "§f"),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE5_4, "§a//line wool§r§7"),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE5_5)
            );
        }

        @Override
        public StageTimeline getTimeline() throws SQLException {
            List<Vector> buildingPoints = getBuildingPoints(getPlot());

            // Map building points to lines
            Map<Vector, Vector> buildingLinePoints = new HashMap<>();
            buildingLinePoints.put(buildingPoints.get(0), buildingPoints.get(1));
            buildingLinePoints.put(buildingPoints.get(1), buildingPoints.get(2));
            buildingLinePoints.put(buildingPoints.get(2), buildingPoints.get(3));
            buildingLinePoints.put(buildingPoints.get(3), buildingPoints.get(0));

            return new StageTimeline(getPlayer())
                    .sendChatMessage(getMessages().get(2), Sound.ENTITY_VILLAGER_AMBIENT, true)
                    .sendChatMessage(getMessages().get(3), Sound.ENTITY_VILLAGER_AMBIENT, true)
                    .delay(5)
                    .sendChatMessage(getMessages().get(5), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, true)
                    .addTask(new LineCmdEventTask(getPlayer(), BASE_BLOCK, BASE_BLOCK_ID, buildingLinePoints, ((minPoint, maxPoint) -> {
                        buildingLinePoints.remove(minPoint);

                        Utils.TutorialUtils.setBlockAt(getPlayer().getWorld(), minPoint, Material.CONCRETE_POWDER, (byte) 5);
                        Utils.TutorialUtils.setBlockAt(getPlayer().getWorld(), maxPoint, Material.CONCRETE_POWDER, (byte) 5);
                        getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 1f);
                    })))
                    .delay(1)
                    .sendChatMessage(getMessages().get(6), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, true);
        }
    }

    private static class Stage6 extends AbstractPlotStage {
        private final static int HEIGHT = ConfigUtil.getTutorialInstance().getBeginnerTutorial().getInt(TutorialPaths.Beginner.HEIGHT);
        private final static int HEIGHT_OFFSET = ConfigUtil.getTutorialInstance().getBeginnerTutorial().getInt(TutorialPaths.Beginner.HEIGHT_OFFSET);

        protected Stage6(Player player, TutorialPlot plot) throws SQLException, IOException {
            super(player, 1, plot, 1);
        }

        @Override
        public List<String> setMessages() {
            return Arrays.asList(
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE6_TITLE),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE6_DESC),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE6_1),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE6_2),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE6_3),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE6_4, "§a" + HEIGHT + "§r§7"),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE6_5, "§a" + HEIGHT_OFFSET + "§r§7"),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE6_6)
            );
        }

        @Override
        public StageTimeline getTimeline() {
            StageTimeline stage = new StageTimeline(getPlayer());
            stage.delay(5)
            .sendChatMessage(getMessages().get(4), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, true)
            .addPlayerChatEvent(HEIGHT, HEIGHT_OFFSET, 3, (isCorrect, attemptsLeft) -> {
                if (!isCorrect && attemptsLeft > 0) {
                   // AbstractTutorial.ChatHandler.printInfo(player, AbstractTutorial.ChatHandler.getTaskMessage(messages.get(7), GRAY));
                } else {
                    stage.delay(1);
                    stage.sendChatMessage(isCorrect ? getMessages().get(5) : getMessages().get(6), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, false);
                }
            });
            return stage;
        }
    }

    private static class Stage7 extends AbstractPlotStage {
        protected Stage7(Player player, TutorialPlot plot) throws SQLException, IOException {
            super(player, 1, plot, 2);
        }

        @Override
        public List<String> setMessages() {
            return Arrays.asList(
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE7_TITLE),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE7_DESC),
                    "TODO"
            );
        }

        @Override
        public StageTimeline getTimeline() {
            return new StageTimeline(getPlayer())
                    .delay(5);
        }
    }

    private static class Stage8 extends AbstractPlotStage {
        protected Stage8(Player player, TutorialPlot plot) throws SQLException, IOException {
            super(player, 1, plot, 4);
        }

        @Override
        public List<String> setMessages() {
            return Arrays.asList(
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE8_TITLE),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE8_DESC),
                    "TODO"
            );
        }

        @Override
        public StageTimeline getTimeline() {
            return new StageTimeline(getPlayer())
                    .delay(5);
        }
    }

    private static class Stage9 extends AbstractPlotStage {
        protected Stage9(Player player, TutorialPlot plot) throws SQLException, IOException {
            super(player, 1, plot, 5);
        }

        @Override
        public List<String> setMessages() {
            return Arrays.asList(
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE9_TITLE),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE9_DESC),
                    "TODO"
            );
        }

        @Override
        public StageTimeline getTimeline() {
            return new StageTimeline(getPlayer())
                    .delay(5);
        }
    }

    private static class Stage10 extends AbstractPlotStage {
        protected Stage10(Player player, TutorialPlot plot) throws SQLException, IOException {
            super(player, 1, plot, 6);
        }

        @Override
        public List<String> setMessages() {
            return Arrays.asList(
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE10_TITLE),
                    LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE10_DESC),
                    "TODO"
            );
        }

        @Override
        public StageTimeline getTimeline() {
            return new StageTimeline(getPlayer())
                    .delay(5);
        }
    }

    /**
     * Get the building points from the config and convert them to Vector
     * @return list of building points as Vector
     */
    private static List<Vector> getBuildingPoints(TutorialPlot plot) throws SQLException {
        // Read coordinates from config
        FileConfiguration config = ConfigUtil.getTutorialInstance().getBeginnerTutorial();
        List<String> buildingPointsAsString = Arrays.asList(
                config.getString(TutorialPaths.Beginner.POINT_1),
                config.getString(TutorialPaths.Beginner.POINT_2),
                config.getString(TutorialPaths.Beginner.POINT_3),
                config.getString(TutorialPaths.Beginner.POINT_4)
        );

        // Convert coordinates to Vector
        List<Vector> buildingPoints = new ArrayList<>();

        World world = plot.getWorld().getBukkitWorld();
        buildingPointsAsString.forEach(c -> {
            String[] pointsSplit = c.trim().split(",");
            double x = Double.parseDouble(pointsSplit[0]);
            double z = Double.parseDouble(pointsSplit[1]);
            double y = AlpsUtils.getHighestBlockYAt(world, (int) x, (int) z);
            buildingPoints.add(new Vector(x, y, z));
        });

        return buildingPoints;
    }
}
