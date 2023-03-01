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
import com.alpsbte.plotsystem.utils.io.language.LangPaths;
import com.alpsbte.plotsystem.utils.io.language.LangUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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
            if (activeStage.isDone) NextStage(); else activeStage.performStage();
        }, 0, 20 / 2); // every half seconds
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
                activeStage = stages.get(activeStageIndex + 1).getDeclaredConstructor(Builder.class).newInstance(builder);
                activeStageIndex++;
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to initialize tutorial stage.");
                player.sendMessage(Utils.getErrorMessageFormat(LangUtil.get(player, LangPaths.Message.Error.ERROR_OCCURRED)));
            }
        }
    }

    private void StopTutorial() {
        if (tutorialTask != null) tutorialTask.cancel();
        activeTutorials.remove(this);
    }
}
