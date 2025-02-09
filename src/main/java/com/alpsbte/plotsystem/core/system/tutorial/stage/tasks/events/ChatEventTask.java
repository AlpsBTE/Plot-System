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

package com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.events;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.core.system.tutorial.TutorialEventListener;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.AbstractTask;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class ChatEventTask extends AbstractTask implements EventTask {
    private final int expectedValue;
    private final int offset;
    private final BiTaskAction<Boolean, Integer> onChatAction;

    private int attemptsLeft;

    public ChatEventTask(Player player, Component assignmentMessage, int expectedValue, int offset, int maxAttempts, BiTaskAction<Boolean, Integer> onChatAction) {
        super(player, assignmentMessage, 1);
        this.expectedValue = expectedValue;
        this.offset = offset;
        this.onChatAction = onChatAction;

        attemptsLeft = maxAttempts;
    }

    @Override
    public void performTask() {
        TutorialEventListener.runningEventTasks.put(player.getUniqueId().toString(), this);
    }

    @Override
    public void performEvent(Event event) {
        if (event instanceof AsyncChatEvent chatEvent) {
            chatEvent.setCancelled(true);

            TextComponent message = (TextComponent) chatEvent.message();
            if (AlpsUtils.tryParseInt(message.content()) != null) {
                int value = Integer.parseInt(message.content());
                if (value >= expectedValue - offset && value <= expectedValue + offset) {
                    onChatAction.performAction(true, attemptsLeft);
                    attemptsLeft = 0;
                } else {
                    attemptsLeft--;
                    onChatAction.performAction(false, attemptsLeft);
                }
            }

            if (attemptsLeft <= 0) {
                updateAssignments();
                setTaskDone();
            }
        }
    }
}
