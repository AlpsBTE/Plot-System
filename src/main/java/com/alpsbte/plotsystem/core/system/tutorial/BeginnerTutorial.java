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
import com.alpsbte.plotsystem.core.holograms.PlotTutorialHologram;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import com.alpsbte.plotsystem.core.system.tutorial.stage.AbstractPlotStage;
import com.alpsbte.plotsystem.core.system.tutorial.stage.AbstractStage;
import com.alpsbte.plotsystem.core.system.tutorial.stage.StageTimeline;
import com.alpsbte.plotsystem.core.system.tutorial.stage.TutorialWorld;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.PlotPermissionChangeTask;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.PlotSchematicPasteTask;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.events.BuildEventTask;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.events.commands.LineCmdEventTask;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.events.commands.WandCmdEventTask;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.message.ChatMessageTask;
import com.alpsbte.plotsystem.utils.io.*;
import com.sk89q.worldedit.math.BlockVector3;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

import static com.alpsbte.plotsystem.core.system.tutorial.TutorialUtils.*;
import static com.alpsbte.plotsystem.core.system.tutorial.TutorialUtils.Delay;
import static com.alpsbte.plotsystem.core.system.tutorial.TutorialUtils.Sound;
import static net.md_5.bungee.api.ChatColor.*;

public class BeginnerTutorial extends AbstractPlotTutorial {
    public BeginnerTutorial(Player player, int stageId) throws SQLException {
        super(player, TutorialCategory.BEGINNER.getId(), stageId);
    }

    @Override
    protected List<TutorialWorld> initWorlds() {
        try {
            return Arrays.asList(
                    new TutorialWorld(getId(), 0),
                    new TutorialWorld(getId(), 1, plot.getWorld().getWorldName())
            );
        } catch (SQLException ex) {
            onException(ex);
        }
        return null;
    }

    @Override
    protected List<Class<? extends AbstractStage>> initStages() {
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

    @Override
    public String getName() {
        return LangUtil.getInstance().get(getPlayer(), LangPaths.MenuTitle.TUTORIAL_BEGINNER);
    }

    private static class Stage1 extends AbstractPlotStage {
        protected Stage1(Player player, TutorialPlot plot) {
            super(player, 0, plot, -1);
        }

        @Override
        public String setTitle() {
            return LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_TITLE);
        }

        @Override
        public List<String> setMessages() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_MESSAGES,
                    CHAT_HIGHLIGHT_COLOR + getPlayer().getName() + GRAY,
                    ConfigUtil.getInstance().configs[0].getString(ConfigPaths.TUTORIAL_NPC_NAME));
        }

        @Override
        protected List<String> setTasks() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_TASKS,
                    ConfigUtil.getInstance().configs[0].getString(ConfigPaths.TUTORIAL_NPC_NAME));
        }

        @Override
        protected List<AbstractTutorialHologram> setHolograms() {
            return null;
        }

        @Override
        public StageTimeline getTimeline() {
            return new StageTimeline(getPlayer())
                    .delay(Delay.TIMELINE_START)
                    .interactNPC(getTasks().get(0))
                    .sendChatMessage(getMessages().get(0), Sound.NPC_TALK, true)
                    .sendChatMessage(getMessages().get(1), Sound.NPC_TALK, true)
                    .sendChatMessage(getMessages().get(2), Sound.NPC_TALK, true)
                    .sendChatMessage(getMessages().get(3), Sound.NPC_TALK, true);
        }
    }

    private static class Stage2 extends AbstractPlotStage {
        public static final String GOOGLE_MAPS = "Google Maps" + GRAY;
        public static final String GOOGLE_MAPS_STREET_VIEW = "Street View" + GRAY;
        public static final String GOOGLE_EARTH = "Google Earth" + GRAY;

        protected Stage2(Player player, TutorialPlot plot) {
            super(player, 1, plot, 0);
        }

        @Override
        protected String setTitle() {
            return LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_TITLE);
        }

        @Override
        public List<String> setMessages() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_MESSAGES,
                    CHAT_HIGHLIGHT_COLOR + GOOGLE_MAPS, CHAT_HIGHLIGHT_COLOR + GOOGLE_EARTH,
                    CHAT_HIGHLIGHT_COLOR + GOOGLE_MAPS,
                    CHAT_HIGHLIGHT_COLOR + GOOGLE_MAPS_STREET_VIEW,
                    CHAT_CLICK_HIGHLIGHT + LangUtil.getInstance().get(getPlayer(), LangPaths.Note.Action.CLICK_TO_OPEN_LINK, GOOGLE_MAPS + CHAT_CLICK_HIGHLIGHT),
                    CHAT_HIGHLIGHT_COLOR + GOOGLE_EARTH,
                    CHAT_CLICK_HIGHLIGHT + LangUtil.getInstance().get(getPlayer(), LangPaths.Note.Action.CLICK_TO_OPEN_LINK, GOOGLE_EARTH + CHAT_CLICK_HIGHLIGHT),
                    CHAT_HIGHLIGHT_COLOR + "/plot links" + GRAY);
        }

        @Override
        protected List<String> setTasks() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE2_TASKS);
        }

        @Override
        protected List<AbstractTutorialHologram> setHolograms() {
            return null;
        }

        @Override
        public StageTimeline getTimeline() throws IOException {
            return new StageTimeline(getPlayer())
                    .delay(Delay.TIMELINE_START)
                    .sendChatMessage(getMessages().get(0), Sound.NPC_TALK, true)
                    .sendChatMessage(getMessages().get(1), Sound.NPC_TALK, true)
                    .sendChatMessage(new Object[] {
                            getMessages().get(2),
                            "",
                            new ChatMessageTask.ClickableTaskMessage(getMessages().get(3), GRAY + GOOGLE_MAPS, getPlot().getGoogleMapsLink(), ClickEvent.Action.OPEN_URL)
                    }, Sound.NPC_TALK, true)
                    .sendChatMessage(new Object[] {
                            getMessages().get(4),
                            "",
                            new ChatMessageTask.ClickableTaskMessage(getMessages().get(5), GRAY + GOOGLE_EARTH, getPlot().getGoogleEarthLink(), ClickEvent.Action.OPEN_URL)
                    }, Sound.NPC_TALK, true)
                    .sendChatMessage(getMessages().get(6), Sound.NPC_TALK, true);
        }
    }

    private static class Stage3 extends AbstractPlotStage {
        protected Stage3(Player player, TutorialPlot plot) {
            super(player, 1, plot, 0);
        }

        @Override
        protected String setTitle() {
            return LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE3_TITLE);
        }

        @Override
        public List<String> setMessages() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE3_MESSAGES,
                    CHAT_HIGHLIGHT_COLOR + "/tpll <lat> <lon>" + GRAY,
                    CHAT_HIGHLIGHT_COLOR + LangUtil.getInstance().get(getPlayer(), LangPaths.Note.Action.RIGHT_CLICK) + GRAY,
                    CHAT_CLICK_HIGHLIGHT + LangUtil.getInstance().get(getPlayer(), LangPaths.Note.Action.CLICK_TO_OPEN_LINK, Stage2.GOOGLE_MAPS + CHAT_CLICK_HIGHLIGHT),
                    CHAT_HIGHLIGHT_COLOR + "4" + GRAY);
        }

        @Override
        protected List<String> setTasks() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE3_TASKS,
                    CHAT_HIGHLIGHT_COLOR + "4" + YELLOW,
                    CHAT_HIGHLIGHT_COLOR + "/tpll" + YELLOW);
        }

        @Override
        protected List<AbstractTutorialHologram> setHolograms() {
            return null;
        }

        @Override
        public StageTimeline getTimeline() throws SQLException, IOException {
            return new StageTimeline(getPlayer())
                    .delay(Delay.TIMELINE_START)
                    .sendChatMessage(getMessages().get(0), Sound.NPC_TALK, true)
                    .sendChatMessage(new Object[] {
                            getMessages().get(1),
                            "",
                            new ChatMessageTask.ClickableTaskMessage(getMessages().get(2), GRAY + Stage2.GOOGLE_MAPS, getPlot().getGoogleMapsLink(), ClickEvent.Action.OPEN_URL)
                    }, Sound.NPC_TALK, false)
                    .delay(Delay.TASK_START)
                    .addTeleportEvent(getTasks().get(0), getPlotPoints(getPlot()), 1, (teleportPoint, isCorrect) -> {
                        if (isCorrect) {
                            TutorialUtils.setBlockAt(getPlayer().getWorld(), teleportPoint, Material.LIME_CONCRETE_POWDER);
                            getPlayer().playSound(new Location(getPlayer().getWorld(), teleportPoint.getBlockX(), teleportPoint.getBlockY(), teleportPoint.getBlockZ()),
                                    Sound.ASSIGNMENT_COMPLETED, 1f, 1f);
                        } else {
                            ChatMessageTask.sendTaskMessage(getPlayer(), new Object[] { getMessages().get(3) }, false);
                            getPlayer().playSound(new Location(getPlayer().getWorld(), teleportPoint.getBlockX(), teleportPoint.getBlockY(), teleportPoint.getBlockZ()),
                                    Sound.ASSIGNMENT_WRONG, 1f, 1f);
                        }
                    });
        }
    }

    private static class Stage4 extends AbstractPlotStage {
        protected Stage4(Player player, TutorialPlot plot) {
            super(player, 1, plot, 1);
        }

        @Override
        protected String setTitle() {
            return LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE4_TITLE);
        }

        @Override
        public List<String> setMessages() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE4_MESSAGES,
                    CHAT_HIGHLIGHT_COLOR + LangUtil.getInstance().get(getPlayer(), LangPaths.Note.Action.RIGHT_CLICK) + GRAY,
                    CHAT_HIGHLIGHT_COLOR + LangUtil.getInstance().get(getPlayer(), LangPaths.Note.Action.LEFT_CLICK) + GRAY);
        }

        @Override
        protected List<String> setTasks() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE4_TASKS,
                    CHAT_HIGHLIGHT_COLOR + "//wand" + YELLOW);
        }

        @Override
        protected List<AbstractTutorialHologram> setHolograms() {
            return null;
        }

        @Override
        public StageTimeline getTimeline() {
            return new StageTimeline(getPlayer())
                    .delay(Delay.TIMELINE_START)
                    .sendChatMessage(getMessages().get(0), Sound.NPC_TALK, true)
                    .sendChatMessage(getMessages().get(1), Sound.NPC_TALK, false)
                    .delay(Delay.TASK_START)
                    .addTask(new WandCmdEventTask(getPlayer(), getTasks().get(0)))
                    .delay(Delay.TASK_END)
                    .sendChatMessage(getMessages().get(2), Sound.NPC_TALK, true);
        }
    }

    private static class Stage5 extends AbstractPlotStage {
        private final static String BASE_BLOCK = ConfigUtil.getTutorialInstance().getBeginnerTutorial().getString(TutorialPaths.Beginner.BASE_BLOCK);
        private final static int BASE_BLOCK_ID = ConfigUtil.getTutorialInstance().getBeginnerTutorial().getInt(TutorialPaths.Beginner.BASE_BLOCK_ID);

        protected Stage5(Player player, TutorialPlot plot) {
            super(player, 1, plot, 1);
        }

        @Override
        protected String setTitle() {
            return LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE5_TITLE);
        }

        @Override
        public List<String> setMessages() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE5_MESSAGES,
                    CHAT_HIGHLIGHT_COLOR + "//line <pattern>" + GRAY,
                    CHAT_HIGHLIGHT_COLOR + LangUtil.getInstance().get(getPlayer(), LangPaths.Note.Action.RIGHT_CLICK) + GRAY,
                    CHAT_HIGHLIGHT_COLOR + LangUtil.getInstance().get(getPlayer(), LangPaths.Note.Action.LEFT_CLICK) + GRAY);
        }

        @Override
        protected List<String> setTasks() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE5_TASKS,
                    CHAT_HIGHLIGHT_COLOR + "//line " + BASE_BLOCK.toLowerCase() + YELLOW);
        }

        @Override
        protected List<AbstractTutorialHologram> setHolograms() {
            return null;
        }

        @Override
        public StageTimeline getTimeline() throws SQLException {
            List<BlockVector3> buildingPoints = getPlotPoints(getPlot());

            // Map building points to lines
            Map<BlockVector3, BlockVector3> buildingLinePoints = new HashMap<>();
            buildingLinePoints.put(buildingPoints.get(0), buildingPoints.get(1));
            buildingLinePoints.put(buildingPoints.get(1), buildingPoints.get(2));
            buildingLinePoints.put(buildingPoints.get(2), buildingPoints.get(3));
            buildingLinePoints.put(buildingPoints.get(3), buildingPoints.get(0));

            return new StageTimeline(getPlayer())
                    .delay(Delay.TIMELINE_START)
                    .sendChatMessage(getMessages().get(0), Sound.NPC_TALK, true)
                    .sendChatMessage(new Object[] {
                            getMessages().get(1),
                            "",
                            getMessages().get(2)
                    }, Sound.NPC_TALK, false)
                    .delay(Delay.TASK_START)
                    .addTask(new PlotPermissionChangeTask(getPlayer(), false, true))
                    .addTask(new LineCmdEventTask(getPlayer(), getTasks().get(0), BASE_BLOCK, BASE_BLOCK_ID, buildingLinePoints, ((minPoint, maxPoint) -> {
                        if (minPoint != null && maxPoint != null) {
                            buildingLinePoints.remove(minPoint);

                            TutorialUtils.setBlockAt(getPlayer().getWorld(), minPoint, Material.LIME_CONCRETE_POWDER);
                            TutorialUtils.setBlockAt(getPlayer().getWorld(), maxPoint, Material.LIME_CONCRETE_POWDER);
                            getPlayer().playSound(getPlayer().getLocation(), Sound.ASSIGNMENT_COMPLETED, 1f, 1f);
                        } else {
                            ChatMessageTask.sendTaskMessage(getPlayer(), new Object[] { getMessages().get(3) }, false);
                            getPlayer().playSound(getPlayer().getLocation(), Sound.ASSIGNMENT_WRONG, 1f, 1f);
                        }
                    })))
                    .addTask(new PlotPermissionChangeTask(getPlayer(), false, false));
        }
    }

    private static class Stage6 extends AbstractPlotStage {
        private final static int HEIGHT = ConfigUtil.getTutorialInstance().getBeginnerTutorial().getInt(TutorialPaths.Beginner.HEIGHT);
        private final static int HEIGHT_OFFSET = ConfigUtil.getTutorialInstance().getBeginnerTutorial().getInt(TutorialPaths.Beginner.HEIGHT_OFFSET);

        protected Stage6(Player player, TutorialPlot plot) {
            super(player, 1, plot, 2);
        }

        @Override
        protected String setTitle() {
            return LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE6_TITLE);
        }

        @Override
        public List<String> setMessages() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE6_MESSAGES,
                    CHAT_CLICK_HIGHLIGHT + LangUtil.getInstance().get(getPlayer(), LangPaths.Note.Action.CLICK_TO_OPEN_LINK, Stage2.GOOGLE_EARTH + CHAT_CLICK_HIGHLIGHT),
                    CHAT_HIGHLIGHT_COLOR + HEIGHT + GRAY);
        }

        @Override
        protected List<String> setTasks() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE6_TASKS);
        }

        @Override
        protected List<AbstractTutorialHologram> setHolograms() {
            return null;
        }

        @Override
        public StageTimeline getTimeline() throws IOException {
            StageTimeline stage = new StageTimeline(getPlayer())
                    .delay(Delay.TIMELINE_START)
                    .sendChatMessage(getMessages().get(0), Sound.NPC_TALK, true)
                    .sendChatMessage(getMessages().get(1), Sound.NPC_TALK, true)
                    .sendChatMessage(new Object[] {
                            getMessages().get(2),
                            "",
                            new ChatMessageTask.ClickableTaskMessage(getMessages().get(3), GRAY + Stage2.GOOGLE_EARTH,
                                    getPlot().getGoogleEarthLink(), ClickEvent.Action.OPEN_URL)
                    }, Sound.NPC_TALK, false)
                    .delay(Delay.TASK_START);
            stage.addPlayerChatEvent(getTasks().get(0), HEIGHT, HEIGHT_OFFSET, 3, (isCorrect, attemptsLeft) -> {
                if (!isCorrect && attemptsLeft > 0) {
                    ChatMessageTask.sendTaskMessage(getPlayer(), new Object[] { getMessages().get(6) }, false);
                    getPlayer().playSound(getPlayer().getLocation(), Sound.ASSIGNMENT_WRONG, 1f, 1f);
                } else {
                    ChatMessageTask.sendTaskMessage(getPlayer(), new Object[] { isCorrect ? getMessages().get(4) : getMessages().get(5) }, false);
                    getPlayer().playSound(getPlayer().getLocation(), Sound.ASSIGNMENT_COMPLETED, 1f, 1f);
                    stage.delay(Delay.TASK_END);
                }
            });
            return stage;
        }
    }

    private static class Stage7 extends AbstractPlotStage {
        protected Stage7(Player player, TutorialPlot plot) {
            super(player, 1, plot, 2);
        }

        @Override
        protected String setTitle() {
            return LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE7_TITLE);
        }

        @Override
        public List<String> setMessages() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE7_MESSAGES,
                    CHAT_HIGHLIGHT_COLOR + "//stack" + WHITE,
                    CHAT_HIGHLIGHT_COLOR + "//line" + WHITE,
                    CHAT_HIGHLIGHT_COLOR + "//sel convex" + WHITE);
        }

        @Override
        protected List<String> setTasks() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE7_TASKS);
        }

        @Override
        protected List<AbstractTutorialHologram> setHolograms() {
            return Arrays.asList(
                    new PlotTutorialHologram(getPlayer(), 0, getMessages().get(1)),
                    new PlotTutorialHologram(getPlayer(), 1, getMessages().get(2)),
                    new PlotTutorialHologram(getPlayer(), 2, getMessages().get(4), 2),
                    new PlotTutorialHologram(getPlayer(), 3, getMessages().get(5), 0),
                    new PlotTutorialHologram(getPlayer(), 4, getMessages().get(6))
            );
        }

        @Override
        public StageTimeline getTimeline() {
            return new StageTimeline(getPlayer())
                    .delay(Delay.TIMELINE_START)
                    .addTask(new PlotSchematicPasteTask(getPlayer(), 3)).delay(1)
                    .sendChatMessage(getMessages().get(0), Sound.NPC_TALK, true)
                    .createHolograms(getTasks().get(0), getHolograms().get(0), getHolograms().get(1))
                    .delay(Delay.TASK_END)
                    .deleteHolograms()
                    .delay(2)
                    .addTask(new PlotSchematicPasteTask(getPlayer(), 4)).delay(1)
                    .sendChatMessage(getMessages().get(3), Sound.NPC_TALK, true)
                    .createHolograms(getTasks().get(0), getHolograms().get(2), getHolograms().get(3), getHolograms().get(4))
                    .delay(Delay.TASK_END);
        }
    }

    private static class Stage8 extends AbstractPlotStage {
        protected Stage8(Player player, TutorialPlot plot) {
            super(player, 1, plot, 4);
        }

        @Override
        protected String setTitle() {
            return LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE8_TITLE);
        }

        @Override
        public List<String> setMessages() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE8_MESSAGES);
        }

        @Override
        protected List<String> setTasks() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE8_TASKS);
        }

        @Override
        protected List<AbstractTutorialHologram> setHolograms() {
            return Arrays.asList(
                    new PlotTutorialHologram(getPlayer(), 5, getMessages().get(2)),
                    new PlotTutorialHologram(getPlayer(), 6, getMessages().get(3), 0)
            );
        }

        @Override
        public StageTimeline getTimeline() {
            return new StageTimeline(getPlayer())
                    .delay(Delay.TIMELINE_START)
                    .addTask(new PlotSchematicPasteTask(getPlayer(), 5)).delay(1)
                    .sendChatMessage(getMessages().get(0), Sound.NPC_TALK, true)
                    .sendChatMessage(getMessages().get(1), Sound.NPC_TALK, true)
                    .createHolograms(getHolograms().get(0), getHolograms().get(1))
                    .addTask(new PlotPermissionChangeTask(getPlayer(), true, false))
                    .addTask(new BuildEventTask(getPlayer(), getTasks().get(0), getWindowBuildPoints(), (blockPos, isCorrect) -> {
                        if (isCorrect) {
                            getPlayer().playSound(getPlayer().getLocation(), Sound.ASSIGNMENT_COMPLETED, 1f, 1f);
                        } else {
                            ChatMessageTask.sendTaskMessage(getPlayer(), new Object[] { getMessages().get(4) }, false);
                            getPlayer().playSound(getPlayer().getLocation(), Sound.ASSIGNMENT_WRONG, 1f, 1f);
                        }
                    }))
                    .addTask(new PlotPermissionChangeTask(getPlayer(), false, false))
                    .delay(Delay.TASK_END)
                    .sendChatMessage(getMessages().get(5), Sound.NPC_TALK, false);
        }
    }

    private static class Stage9 extends AbstractPlotStage {
        protected Stage9(Player player, TutorialPlot plot) {
            super(player, 1, plot, 5);
        }

        @Override
        protected String setTitle() {
            return LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE9_TITLE);
        }

        @Override
        public List<String> setMessages() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE9_MESSAGES,
                    CHAT_HIGHLIGHT_COLOR + "//replace" + WHITE);
        }

        @Override
        protected List<String> setTasks() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE9_TASKS);
        }

        @Override
        protected List<AbstractTutorialHologram> setHolograms() {
            return Arrays.asList(
                    new PlotTutorialHologram(getPlayer(), 7, getMessages().get(1)),
                    new PlotTutorialHologram(getPlayer(), 8, getMessages().get(2), 1),
                    new PlotTutorialHologram(getPlayer(), 9, getMessages().get(3))
            );
        }

        @Override
        public StageTimeline getTimeline() {
            return new StageTimeline(getPlayer())
                    .delay(Delay.TIMELINE_START)
                    .addTask(new PlotSchematicPasteTask(getPlayer(), 6)).delay(1)
                    .sendChatMessage(getMessages().get(0), Sound.NPC_TALK, true)
                    .createHolograms(getTasks().get(0), getHolograms().get(0), getHolograms().get(1), getHolograms().get(2))
                    .delay(Delay.TASK_END);
        }
    }

    private static class Stage10 extends AbstractPlotStage {
        protected Stage10(Player player, TutorialPlot plot) {
            super(player, 1, plot, 6);
        }

        @Override
        protected String setTitle() {
            return LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE10_TITLE);
        }

        @Override
        public List<String> setMessages() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE10_MESSAGES,
                    CHAT_HIGHLIGHT_COLOR + "/hdb" + WHITE,
                    CHAT_HIGHLIGHT_COLOR + "/discord" + GRAY);
        }

        @Override
        protected List<String> setTasks() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE10_TASKS);
        }

        @Override
        protected List<AbstractTutorialHologram> setHolograms() {
            return Arrays.asList(
                    new PlotTutorialHologram(getPlayer(), 10, getMessages().get(1)),
                    new PlotTutorialHologram(getPlayer(), 11, getMessages().get(2), 0)
            );
        }

        @Override
        public StageTimeline getTimeline() {
            return new StageTimeline(getPlayer())
                    .delay(Delay.TIMELINE_START)
                    .addTask(new PlotSchematicPasteTask(getPlayer(), 7)).delay(1)
                    .sendChatMessage(getMessages().get(0), Sound.NPC_TALK, true)
                    .createHolograms(getTasks().get(0), getHolograms().get(0), getHolograms().get(1))
                    .delay(Delay.TASK_END)
                    .deleteHolograms()
                    .delay(2)
                    .sendChatMessage(new Object[] {
                        getMessages().get(3),
                        "",
                        new ChatMessageTask.ClickableTaskMessage(CHAT_CLICK_HIGHLIGHT + getMessages().get(4),
                                GRAY + LangUtil.getInstance().get(getPlayer(), LangPaths.Note.Action.READ_MORE) + "...",
                                TutorialUtils.getDocumentationLinks(TutorialCategory.BEGINNER.id).get(0), ClickEvent.Action.OPEN_URL),
                    }, Sound.NPC_TALK, true)
                    .sendChatMessage(getMessages().get(5), Sound.NPC_TALK, true)
                    .sendChatMessage(getMessages().get(6), Sound.NPC_TALK, false).delay(3)
                    .teleport(0);
        }
    }

    /**
     * Get the four plot building points from the config and convert them to Vector
     * @return list of building points as Vector
     */
    private static List<BlockVector3> getPlotPoints(TutorialPlot plot) throws SQLException {
        // Read coordinates from config
        FileConfiguration config = ConfigUtil.getTutorialInstance().getBeginnerTutorial();
        List<String> plotPointsAsString = Arrays.asList(
                config.getString(TutorialPaths.Beginner.POINT_1),
                config.getString(TutorialPaths.Beginner.POINT_2),
                config.getString(TutorialPaths.Beginner.POINT_3),
                config.getString(TutorialPaths.Beginner.POINT_4)
        );

        // Convert coordinates to Vector
        List<BlockVector3> plotPoints = new ArrayList<>();

        World world = plot.getWorld().getBukkitWorld();
        plotPointsAsString.forEach(point -> {
            String[] pointsSplit = point.trim().split(",");
            double x = Double.parseDouble(pointsSplit[0]);
            double z = Double.parseDouble(pointsSplit[1]);
            double y = AlpsUtils.getHighestBlockYAt(world, (int) x, (int) z);
            plotPoints.add(BlockVector3.at(x, y, z));
        });

        return plotPoints;
    }

    private static Map<BlockVector3, BlockData> getWindowBuildPoints() {
        // Read coordinates from config
        FileConfiguration config = ConfigUtil.getTutorialInstance().getBeginnerTutorial();
        List<String> windowPointsAsString = config.getStringList(TutorialPaths.Beginner.WINDOW_POINTS);

        // Convert string to block pos and material
        Map<BlockVector3, BlockData> windowPoints = new HashMap<>();

        windowPointsAsString.forEach(point -> {
            String[] pointsSplit = point.trim().split(";");
            double x = Double.parseDouble(pointsSplit[0]);
            double y = Double.parseDouble(pointsSplit[1]);
            double z = Double.parseDouble(pointsSplit[2]);
            BlockData blockData;
            try {
                blockData = Bukkit.createBlockData(pointsSplit[3].trim().toLowerCase());
            } catch (IllegalArgumentException ex) {
                Bukkit.getLogger().log(Level.WARNING, "Could not read tutorial config value for material.", ex);
                return;
            }
            windowPoints.put(BlockVector3.at(x, y, z), blockData);
        });

        return windowPoints;
    }
}
