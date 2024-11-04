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
import com.alpsbte.plotsystem.PlotSystem;
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
import com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialUtils;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialUtils.*;
import static com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialUtils.Sound;
import static net.kyori.adventure.text.Component.text;
import static com.alpsbte.alpslib.utils.AlpsUtils.deserialize;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class BeginnerTutorial extends AbstractPlotTutorial {
    public BeginnerTutorial(Player player, int stageId) throws SQLException {
        super(player, TutorialCategory.BEGINNER.getId(), stageId);
    }

    @Override
    protected List<TutorialWorld> initWorlds() {
        try {
            return Arrays.asList(
                    new TutorialWorld(getId(), 0, Utils.getSpawnLocation().getWorld().getName()),
                    new TutorialWorld(getId(), 1, plot.getWorld().getWorldName()),
                    new TutorialWorld(getId(), 2, Utils.getSpawnLocation().getWorld().getName())
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

    @Override
    public FileConfiguration getConfig() {
        return ConfigUtil.getTutorialInstance().getBeginnerTutorial();
    }

    @Override
    public void onTutorialComplete(UUID playerUUID) {
        super.onTutorialComplete(playerUUID);
        Bukkit.getScheduler().runTaskLaterAsynchronously(PlotSystem.getPlugin(), () -> {
            sendTutorialCompletedTipMessage(getPlayer());
            getPlayer().playSound(getPlayer().getLocation(), Utils.SoundUtils.NOTIFICATION_SOUND, 1f, 1f);
        }, 20 * 7);
    }

    private static class Stage1 extends AbstractPlotStage {
        protected Stage1(Player player, TutorialPlot plot) {
            super(player, 0, plot, -1);
        }

        @Override
        public String setTitle() {
            return LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.Beginner.STAGE1_TITLE);
        }

        @Override
        public List<String> setMessages() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.Beginner.STAGE1_MESSAGES,
                    TEXT_HIGHLIGHT_START + getPlayer().getName() + TEXT_HIGHLIGHT_END,
                    ConfigUtil.getInstance().configs[0].getString(ConfigPaths.TUTORIAL_NPC_NAME));
        }

        @Override
        protected List<String> setTasks() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.Beginner.STAGE1_TASKS,
                    TEXT_HIGHLIGHT_START + ConfigUtil.getInstance().configs[0].getString(ConfigPaths.TUTORIAL_NPC_NAME) +
                            TEXT_HIGHLIGHT_END);
        }

        @Override
        protected List<AbstractTutorialHologram> setHolograms() {
            return null;
        }

        @Override
        public StageTimeline getTimeline() {
            return new StageTimeline(getPlayer())
                    .delay(Delay.TIMELINE_START)
                    .interactNPC(deserialize(getTasks().get(0)))
                    .sendChatMessage(deserialize(getMessages().get(0)), Sound.NPC_TALK, true)
                    .sendChatMessage(deserialize(getMessages().get(1)), Sound.NPC_TALK, true)
                    .sendChatMessage(deserialize(getMessages().get(2)), Sound.NPC_TALK, true)
                    .sendChatMessage(deserialize(getMessages().get(3)), Sound.NPC_TALK, true);
        }
    }

    private static class Stage2 extends AbstractPlotStage {
        public static final String GOOGLE_MAPS = "Google Maps";
        public static final String STREET_VIEW = "Street View";
        public static final String GOOGLE_EARTH = "Google Earth";

        protected Stage2(Player player, TutorialPlot plot) {
            super(player, 1, plot, 0);
        }

        @Override
        protected String setTitle() {
            return LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.Beginner.STAGE2_TITLE);
        }

        @Override
        public List<String> setMessages() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.Beginner.STAGE2_MESSAGES,
                    TEXT_HIGHLIGHT_START + GOOGLE_MAPS + TEXT_HIGHLIGHT_END,
                    TEXT_HIGHLIGHT_START + GOOGLE_EARTH + TEXT_HIGHLIGHT_END,
                    TEXT_HIGHLIGHT_START + GOOGLE_MAPS + TEXT_HIGHLIGHT_END,
                    TEXT_HIGHLIGHT_START + STREET_VIEW + TEXT_HIGHLIGHT_END,
                    TEXT_CLICK_HIGHLIGHT + LangUtil.getInstance().get(getPlayer(), LangPaths.Note.Action.CLICK_TO_OPEN_LINK, GOOGLE_MAPS),
                    TEXT_HIGHLIGHT_START + GOOGLE_EARTH + TEXT_HIGHLIGHT_END,
                    TEXT_CLICK_HIGHLIGHT + LangUtil.getInstance().get(getPlayer(), LangPaths.Note.Action.CLICK_TO_OPEN_LINK, GOOGLE_EARTH),
                    TEXT_HIGHLIGHT_START + "/plot links" + TEXT_HIGHLIGHT_END);
        }

        @Override
        protected List<String> setTasks() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.Beginner.STAGE2_TASKS);
        }

        @Override
        protected List<AbstractTutorialHologram> setHolograms() {
            return null;
        }

        @Override
        public StageTimeline getTimeline() throws IOException {
            return new StageTimeline(getPlayer())
                    .delay(Delay.TIMELINE_START)
                    .sendChatMessage(deserialize(getMessages().get(0)), Sound.NPC_TALK, true)
                    .sendChatMessage(deserialize(getMessages().get(1)), Sound.NPC_TALK, true)
                    .sendChatMessage(new Object[]{
                            deserialize(getMessages().get(2)),
                            "",
                            new ChatMessageTask.ClickableTaskMessage(deserialize(getMessages().get(3)).color(GRAY),
                                    text(GOOGLE_MAPS, GRAY), ClickEvent.openUrl(getPlot().getGoogleMapsLink()))
                    }, Sound.NPC_TALK, true)
                    .sendChatMessage(new Object[]{
                            deserialize(getMessages().get(4)),
                            "",
                            new ChatMessageTask.ClickableTaskMessage(deserialize(getMessages().get(5)).color(GRAY),
                                    text(GOOGLE_EARTH, GRAY), ClickEvent.openUrl(getPlot().getGoogleEarthLink()))
                    }, Sound.NPC_TALK, true)
                    .sendChatMessage(deserialize(getMessages().get(6)), Sound.NPC_TALK, true);
        }
    }

    private static class Stage3 extends AbstractPlotStage {
        protected Stage3(Player player, TutorialPlot plot) {
            super(player, 1, plot, 0);
        }

        @Override
        protected String setTitle() {
            return LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.Beginner.STAGE3_TITLE);
        }

        @Override
        public List<String> setMessages() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.Beginner.STAGE3_MESSAGES,
                    TEXT_HIGHLIGHT_START + "/tpll <lat> <lon>" + TEXT_HIGHLIGHT_END,
                    TEXT_HIGHLIGHT_START + LangUtil.getInstance().get(getPlayer(), LangPaths.Note.Action.RIGHT_CLICK) + TEXT_HIGHLIGHT_END,
                    TEXT_CLICK_HIGHLIGHT + LangUtil.getInstance().get(getPlayer(), LangPaths.Note.Action.CLICK_TO_OPEN_LINK, Stage2.GOOGLE_MAPS)
            );
        }

        @Override
        protected List<String> setTasks() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.Beginner.STAGE3_TASKS,
                    TEXT_HIGHLIGHT_START + "4" + TEXT_HIGHLIGHT_END,
                    TEXT_HIGHLIGHT_START + "/tpll" + TEXT_HIGHLIGHT_END);
        }

        @Override
        protected List<AbstractTutorialHologram> setHolograms() {
            return Collections.singletonList(
                    new PlotTutorialHologram(getPlayer(), getId(), 0, getMessages().get(4), 3)
            );
        }

        @Override
        public StageTimeline getTimeline() throws SQLException, IOException {
            return new StageTimeline(getPlayer())
                    .delay(Delay.TIMELINE_START)
                    .sendChatMessage(deserialize(getMessages().get(0)), Sound.NPC_TALK, true)
                    .sendChatMessage(new Object[]{
                            deserialize(getMessages().get(1)),
                            "",
                            new ChatMessageTask.ClickableTaskMessage(deserialize(getMessages().get(2)).color(GRAY),
                                    text(Stage2.GOOGLE_MAPS, GRAY), ClickEvent.openUrl(getPlot().getGoogleMapsLink()))
                    }, Sound.NPC_TALK, false)
                    .delay(Delay.TASK_START)
                    .createHolograms(getHolograms().get(0))
                    .addTeleportEvent(deserialize(getTasks().get(0)), getPlotPoints(getPlot()), 1, (teleportPoint, isCorrect) -> {
                        if (isCorrect) {
                            setBlockAt(getPlayer().getWorld(), teleportPoint, Material.LIME_CONCRETE_POWDER);
                            getPlayer().playSound(new Location(getPlayer().getWorld(), teleportPoint.getBlockX(), teleportPoint.getBlockY(), teleportPoint.getBlockZ()),
                                    Sound.ASSIGNMENT_COMPLETED, 1f, 1f);
                        } else {
                            ChatMessageTask.sendTaskMessage(getPlayer(), new Object[]{deserialize(getMessages().get(3))}, false);
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
            return LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.Beginner.STAGE4_TITLE);
        }

        @Override
        public List<String> setMessages() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.Beginner.STAGE4_MESSAGES,
                    TEXT_HIGHLIGHT_START + "WorldEdit" + TEXT_HIGHLIGHT_END,
                    TEXT_HIGHLIGHT_START + LangUtil.getInstance().get(getPlayer(), LangPaths.Note.Action.RIGHT_CLICK) + TEXT_HIGHLIGHT_END,
                    TEXT_HIGHLIGHT_START + LangUtil.getInstance().get(getPlayer(), LangPaths.Note.Action.LEFT_CLICK) + TEXT_HIGHLIGHT_END);
        }

        @Override
        protected List<String> setTasks() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.Beginner.STAGE4_TASKS,
                    TEXT_HIGHLIGHT_START + "//wand" + TEXT_HIGHLIGHT_END);
        }

        @Override
        protected List<AbstractTutorialHologram> setHolograms() {
            return null;
        }

        @Override
        public StageTimeline getTimeline() {
            return new StageTimeline(getPlayer())
                    .delay(Delay.TIMELINE_START)
                    .sendChatMessage(deserialize(getMessages().get(0)), Sound.NPC_TALK, true)
                    .sendChatMessage(deserialize(getMessages().get(1)), Sound.NPC_TALK, false)
                    .delay(Delay.TASK_START)
                    .addTask(new WandCmdEventTask(getPlayer(), deserialize(getTasks().get(0))))
                    .delay(Delay.TASK_END)
                    .sendChatMessage(deserialize(getMessages().get(2)), Sound.NPC_TALK, true);
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
            return LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.Beginner.STAGE5_TITLE);
        }

        @Override
        public List<String> setMessages() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.Beginner.STAGE5_MESSAGES,
                    TEXT_HIGHLIGHT_START + "//line <pattern>" + TEXT_HIGHLIGHT_END,
                    TEXT_HIGHLIGHT_START + LangUtil.getInstance().get(getPlayer(), LangPaths.Note.Action.RIGHT_CLICK) + TEXT_HIGHLIGHT_END,
                    TEXT_HIGHLIGHT_START + LangUtil.getInstance().get(getPlayer(), LangPaths.Note.Action.LEFT_CLICK) + TEXT_HIGHLIGHT_END);
        }

        @Override
        protected List<String> setTasks() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.Beginner.STAGE5_TASKS,
                    TEXT_HIGHLIGHT_START + "//line " + BASE_BLOCK.toLowerCase() + TEXT_HIGHLIGHT_END);
        }

        @Override
        protected List<AbstractTutorialHologram> setHolograms() {
            return null;
        }

        @Override
        public StageTimeline getTimeline() throws SQLException {
            List<Vector> buildingPoints = getPlotPoints(getPlot());

            // Map building points to lines
            Map<Vector, Vector> buildingLinePoints = new HashMap<>();
            buildingLinePoints.put(buildingPoints.get(0), buildingPoints.get(1));
            buildingLinePoints.put(buildingPoints.get(1), buildingPoints.get(2));
            buildingLinePoints.put(buildingPoints.get(2), buildingPoints.get(3));
            buildingLinePoints.put(buildingPoints.get(3), buildingPoints.get(0));

            return new StageTimeline(getPlayer())
                    .delay(Delay.TIMELINE_START)
                    .sendChatMessage(deserialize(getMessages().get(0)), Sound.NPC_TALK, true)
                    .sendChatMessage(new Object[]{
                            deserialize(getMessages().get(1)),
                            "",
                            deserialize(getMessages().get(2))
                    }, Sound.NPC_TALK, false)
                    .delay(Delay.TASK_START)
                    .addTask(new PlotPermissionChangeTask(getPlayer(), false, true))
                    .addTask(new LineCmdEventTask(getPlayer(), deserialize(getTasks().get(0)), BASE_BLOCK, BASE_BLOCK_ID, buildingLinePoints, ((minPoint, maxPoint) -> {
                        if (minPoint != null && maxPoint != null) {
                            buildingLinePoints.remove(minPoint);

                            setBlockAt(getPlayer().getWorld(), minPoint, Material.LIME_CONCRETE_POWDER);
                            setBlockAt(getPlayer().getWorld(), maxPoint, Material.LIME_CONCRETE_POWDER);
                            getPlayer().playSound(getPlayer().getLocation(), Sound.ASSIGNMENT_COMPLETED, 1f, 1f);
                        } else {
                            ChatMessageTask.sendTaskMessage(getPlayer(), new Object[]{deserialize(getMessages().get(3))}, false);
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
            return LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.Beginner.STAGE6_TITLE);
        }

        @Override
        public List<String> setMessages() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.Beginner.STAGE6_MESSAGES,
                    TEXT_CLICK_HIGHLIGHT + LangUtil.getInstance().get(getPlayer(), LangPaths.Note.Action.CLICK_TO_OPEN_LINK, Stage2.GOOGLE_EARTH),
                    TEXT_HIGHLIGHT_START + HEIGHT + TEXT_HIGHLIGHT_END);
        }

        @Override
        protected List<String> setTasks() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.Beginner.STAGE6_TASKS);
        }

        @Override
        protected List<AbstractTutorialHologram> setHolograms() {
            return Collections.singletonList(new PlotTutorialHologram(getPlayer(), getId(), 13, getMessages().get(7), 4));
        }

        @Override
        public StageTimeline getTimeline() throws IOException {
            StageTimeline stage = new StageTimeline(getPlayer())
                    .delay(Delay.TIMELINE_START)
                    .sendChatMessage(deserialize(getMessages().get(0)), Sound.NPC_TALK, true)
                    .sendChatMessage(deserialize(getMessages().get(1)), Sound.NPC_TALK, true)
                    .sendChatMessage(new Object[]{
                            deserialize(getMessages().get(2)),
                            "",
                            new ChatMessageTask.ClickableTaskMessage(deserialize(getMessages().get(3)).color(GRAY),
                                    text(Stage2.GOOGLE_EARTH, GRAY), ClickEvent.openUrl(getPlot().getGoogleEarthLink()))
                    }, Sound.NPC_TALK, false)
                    .delay(Delay.TASK_START)
                    .createHolograms(getHolograms().get(0));
            stage.addPlayerChatEvent(deserialize(getTasks().get(0)), HEIGHT, HEIGHT_OFFSET, 3, (isCorrect, attemptsLeft) -> {
                if (!isCorrect && attemptsLeft > 0) {
                    ChatMessageTask.sendTaskMessage(getPlayer(), new Object[]{deserialize(getMessages().get(6))}, false);
                    getPlayer().playSound(getPlayer().getLocation(), Sound.ASSIGNMENT_WRONG, 1f, 1f);
                } else {
                    ChatMessageTask.sendTaskMessage(getPlayer(), new Object[]{isCorrect ? deserialize(getMessages().get(4)) :
                            deserialize(getMessages().get(5))}, false);
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
            return LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.Beginner.STAGE7_TITLE);
        }

        @Override
        public List<String> setMessages() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.Beginner.STAGE7_MESSAGES,
                    "//stack",
                    "//line",
                    "//sel convex");
        }

        @Override
        protected List<String> setTasks() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.Beginner.STAGE7_TASKS);
        }

        @Override
        protected List<AbstractTutorialHologram> setHolograms() {
            return Arrays.asList(
                    new PlotTutorialHologram(getPlayer(), getId(), 1, getMessages().get(1)),
                    new PlotTutorialHologram(getPlayer(), getId(), 2, getMessages().get(2)),
                    new PlotTutorialHologram(getPlayer(), getId(), 3, getMessages().get(4), 2),
                    new PlotTutorialHologram(getPlayer(), getId(), 4, getMessages().get(5), 5),
                    new PlotTutorialHologram(getPlayer(), getId(), 5, getMessages().get(6))
            );
        }

        @Override
        public StageTimeline getTimeline() {
            return new StageTimeline(getPlayer())
                    .delay(Delay.TIMELINE_START)
                    .addTask(new PlotSchematicPasteTask(getPlayer(), 3)).delay(1)
                    .sendChatMessage(deserialize(getMessages().get(0)), Sound.NPC_TALK, true)
                    .createHolograms(deserialize(getTasks().get(0)), getHolograms().get(0), getHolograms().get(1))
                    .delay(Delay.TASK_END)
                    .deleteHolograms()
                    .delay(2)
                    .addTask(new PlotSchematicPasteTask(getPlayer(), 4)).delay(1)
                    .sendChatMessage(deserialize(getMessages().get(3)), Sound.NPC_TALK, true)
                    .createHolograms(deserialize(getTasks().get(0)), getHolograms().get(2), getHolograms().get(3), getHolograms().get(4))
                    .delay(Delay.TASK_END);
        }
    }

    private static class Stage8 extends AbstractPlotStage {
        protected Stage8(Player player, TutorialPlot plot) {
            super(player, 1, plot, 4);
        }

        @Override
        protected String setTitle() {
            return LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.Beginner.STAGE8_TITLE);
        }

        @Override
        public List<String> setMessages() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.Beginner.STAGE8_MESSAGES);
        }

        @Override
        protected List<String> setTasks() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.Beginner.STAGE8_TASKS);
        }

        @Override
        protected List<AbstractTutorialHologram> setHolograms() {
            return Arrays.asList(
                    new PlotTutorialHologram(getPlayer(), getId(), 6, getMessages().get(2)),
                    new PlotTutorialHologram(getPlayer(), getId(), 7, getMessages().get(3))
            );
        }

        @Override
        public StageTimeline getTimeline() {
            return new StageTimeline(getPlayer())
                    .delay(Delay.TIMELINE_START)
                    .addTask(new PlotSchematicPasteTask(getPlayer(), 5)).delay(1)
                    .sendChatMessage(deserialize(getMessages().get(0)), Sound.NPC_TALK, true)
                    .sendChatMessage(deserialize(getMessages().get(1)), Sound.NPC_TALK, true)
                    .createHolograms(getHolograms().get(0), getHolograms().get(1))
                    .addTask(new PlotPermissionChangeTask(getPlayer(), true, false))
                    .addTask(new BuildEventTask(getPlayer(), deserialize(getTasks().get(0)), getWindowBuildPoints(), (blockPos, isCorrect) -> {
                        if (isCorrect) {
                            getPlayer().playSound(getPlayer().getLocation(), Sound.ASSIGNMENT_COMPLETED, 1f, 1f);
                        } else {
                            ChatMessageTask.sendTaskMessage(getPlayer(), new Object[]{deserialize(getMessages().get(4))}, false);
                            getPlayer().playSound(getPlayer().getLocation(), Sound.ASSIGNMENT_WRONG, 1f, 1f);
                        }
                    }))
                    .addTask(new PlotPermissionChangeTask(getPlayer(), false, false))
                    .delay(Delay.TASK_END)
                    .sendChatMessage(deserialize(getMessages().get(5)), Sound.NPC_TALK, false).delay(1);
        }
    }

    private static class Stage9 extends AbstractPlotStage {
        protected Stage9(Player player, TutorialPlot plot) {
            super(player, 1, plot, 5);
        }

        @Override
        protected String setTitle() {
            return LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.Beginner.STAGE9_TITLE);
        }

        @Override
        public List<String> setMessages() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.Beginner.STAGE9_MESSAGES,
                    "//replace");
        }

        @Override
        protected List<String> setTasks() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.Beginner.STAGE9_TASKS);
        }

        @Override
        protected List<AbstractTutorialHologram> setHolograms() {
            return Arrays.asList(
                    new PlotTutorialHologram(getPlayer(), getId(), 8, getMessages().get(1)),
                    new PlotTutorialHologram(getPlayer(), getId(), 9, getMessages().get(2), 1),
                    new PlotTutorialHologram(getPlayer(), getId(), 10, getMessages().get(3))
            );
        }

        @Override
        public StageTimeline getTimeline() {
            return new StageTimeline(getPlayer())
                    .delay(Delay.TIMELINE_START)
                    .addTask(new PlotSchematicPasteTask(getPlayer(), 6)).delay(1)
                    .sendChatMessage(deserialize(getMessages().get(0)), Sound.NPC_TALK, true)
                    .createHolograms(deserialize(getTasks().get(0)), getHolograms().get(0), getHolograms().get(1), getHolograms().get(2))
                    .delay(Delay.TASK_END);
        }
    }

    private static class Stage10 extends AbstractPlotStage {
        protected Stage10(Player player, TutorialPlot plot) {
            super(player, 1, plot, 6);
        }

        @Override
        protected String setTitle() {
            return LangUtil.getInstance().get(getPlayer(), LangPaths.Tutorials.Beginner.STAGE10_TITLE);
        }

        @Override
        public List<String> setMessages() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.Beginner.STAGE10_MESSAGES,
                    "/hdb",
                    TEXT_HIGHLIGHT_START + "/discord" + TEXT_HIGHLIGHT_END);
        }

        @Override
        protected List<String> setTasks() {
            return LangUtil.getInstance().getList(getPlayer(), LangPaths.Tutorials.Beginner.STAGE10_TASKS);
        }

        @Override
        protected List<AbstractTutorialHologram> setHolograms() {
            return Arrays.asList(
                    new PlotTutorialHologram(getPlayer(), getId(), 11, getMessages().get(1)),
                    new PlotTutorialHologram(getPlayer(), getId(), 12, getMessages().get(2))
            );
        }

        @Override
        public StageTimeline getTimeline() {
            return new StageTimeline(getPlayer())
                    .delay(Delay.TIMELINE_START)
                    .addTask(new PlotSchematicPasteTask(getPlayer(), 7)).delay(1)
                    .sendChatMessage(deserialize(getMessages().get(0)), Sound.NPC_TALK, true)
                    .createHolograms(deserialize(getTasks().get(0)), getHolograms().get(0), getHolograms().get(1))
                    .delay(Delay.TASK_END)
                    .deleteHolograms()
                    .delay(2)
                    .sendChatMessage(new Object[]{
                            deserialize(getMessages().get(3)),
                            "",
                            new ChatMessageTask.ClickableTaskMessage(deserialize(TEXT_CLICK_HIGHLIGHT + getMessages().get(4)).color(GRAY),
                                    deserialize(LangUtil.getInstance().get(getPlayer(), LangPaths.Note.Action.READ_MORE)).color(GRAY).append(text("...")),
                                    ClickEvent.openUrl(getDocumentationLinks(ConfigUtil.getTutorialInstance().getBeginnerTutorial()).get(6))),
                    }, Sound.NPC_TALK, true).delay(2)
                    .teleport(2).delay(2)
                    .sendChatMessage(deserialize(getMessages().get(5)), Sound.NPC_TALK, true)
                    .sendChatMessage(deserialize(getMessages().get(6)), Sound.NPC_TALK, true)
                    .sendChatMessage(deserialize(getMessages().get(7)), Sound.NPC_TALK, false).delay(3);
        }
    }

    /**
     * Get the four plot building points from the config and convert them to Vector
     *
     * @return list of building points as Vector
     */
    private static List<Vector> getPlotPoints(TutorialPlot plot) throws SQLException {
        // Read coordinates from config
        FileConfiguration config = ConfigUtil.getTutorialInstance().getBeginnerTutorial();
        List<String> plotPointsAsString = Arrays.asList(
                config.getString(TutorialPaths.Beginner.POINT_1),
                config.getString(TutorialPaths.Beginner.POINT_2),
                config.getString(TutorialPaths.Beginner.POINT_3),
                config.getString(TutorialPaths.Beginner.POINT_4)
        );

        // Convert coordinates to Vector
        List<Vector> plotPoints = new ArrayList<>();

        World world = plot.getWorld().getBukkitWorld();
        plotPointsAsString.forEach(point -> {
            String[] pointsSplit = point.trim().split(",");
            double x = Double.parseDouble(pointsSplit[0]);
            double z = Double.parseDouble(pointsSplit[1]);
            double y = AlpsUtils.getHighestBlockYAt(world, (int) x, (int) z);
            plotPoints.add(new Vector(x, y, z));
        });

        return plotPoints;
    }

    private static Map<Vector, BlockData> getWindowBuildPoints() {
        // Read coordinates from config
        FileConfiguration config = ConfigUtil.getTutorialInstance().getBeginnerTutorial();
        List<String> windowPointsAsString = config.getStringList(TutorialPaths.Beginner.WINDOW_POINTS);

        // Convert string to block pos and material
        Map<Vector, BlockData> windowPoints = new HashMap<>();

        windowPointsAsString.forEach(point -> {
            String[] pointsSplit = point.trim().split(";");
            double x = Double.parseDouble(pointsSplit[0]);
            double y = Double.parseDouble(pointsSplit[1]);
            double z = Double.parseDouble(pointsSplit[2]);
            BlockData blockData;
            try {
                blockData = Bukkit.createBlockData(pointsSplit[3].trim().toLowerCase());
            } catch (IllegalArgumentException ex) {
                PlotSystem.getPlugin().getComponentLogger().warn(text("Could not read tutorial config value for material!"), ex);
                return;
            }
            windowPoints.put(new Vector(x, y, z), blockData);
        });

        return windowPoints;
    }

    public static void sendTutorialCompletedTipMessage(Player player) {
        Component clickComponent = text("[", DARK_GRAY, BOLD)
                .append(text(LangUtil.getInstance().get(player, LangPaths.Note.Action.CREATE_PLOT), GREEN))
                .append(text("]", DARK_GRAY))
                .clickEvent(ClickEvent.runCommand("/companion"))
                .hoverEvent(HoverEvent.showText(text(LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_CREATE), GRAY)));

        player.sendMessage(text());
        player.sendMessage(TutorialUtils.CHAT_TASK_PREFIX_COMPONENT
                .append(deserialize(LangUtil.getInstance().get(player, LangPaths.Message.Info.BEGINNER_TUTORIAL_COMPLETED)).color(GRAY)));
        player.sendMessage(clickComponent);
        player.sendMessage(text());
    }
}
