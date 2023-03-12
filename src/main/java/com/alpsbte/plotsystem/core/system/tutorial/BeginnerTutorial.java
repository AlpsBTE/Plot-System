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

import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.language.LangPaths;
import com.alpsbte.plotsystem.utils.io.language.LangUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class BeginnerTutorial extends AbstractTutorial {

    @Override
    protected List<Class<? extends AbstractStage>> setStages() {
        return Arrays.asList(
                Stage1.class
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
                    LangUtil.get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_TITLE),
                    LangUtil.get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_DESC),
                    LangUtil.get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_1),
                    LangUtil.get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_2),
                    LangUtil.get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_3),
                    LangUtil.get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_4),
                    LangUtil.get(player, LangPaths.Tutorials.TUTORIALS_BEGINNER_STAGE1_5)
            );
        }

        @Override
        public StageTimeline setTasks() {
            return new StageTimeline(player)
                    .teleportPlayer(Utils.getSpawnLocation()).delay(2)
                    .sendMessage(messages.get(2), Sound.ENTITY_VILLAGER_AMBIENT).delay(4)
                    .sendMessage(messages.get(3), Sound.ENTITY_VILLAGER_AMBIENT).delay(4)
                    .sendMessage(messages.get(4), Sound.ENTITY_VILLAGER_AMBIENT).delay(4)
                    .sendMessage(messages.get(5), Sound.ENTITY_VILLAGER_AMBIENT).delay(4)
                    .sendMessage(messages.get(6), Sound.ENTITY_VILLAGER_AMBIENT);
        }
    }
}
