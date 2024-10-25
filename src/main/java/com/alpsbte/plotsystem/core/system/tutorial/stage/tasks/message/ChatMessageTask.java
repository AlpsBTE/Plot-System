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

package com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.message;

import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialUtils;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.AbstractTask;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class ChatMessageTask extends AbstractTask {
    private final Object[] messages;
    private final Sound soundEffect;
    private final boolean isWaitToContinue;

    public ChatMessageTask(Player player, Object[] messages, Sound soundEffect, boolean isWaitToContinue) {
        super(player);
        this.messages = messages;
        this.soundEffect = soundEffect;
        this.isWaitToContinue = isWaitToContinue;
    }

    @Override
    public void performTask() {
        sendTaskMessage(player, messages, isWaitToContinue);
        if (soundEffect != null) player.playSound(player.getLocation(), soundEffect, 1f, 1f);
        setTaskDone();
    }

    @Override
    public String toString() {
        return "ChatMessageTask";
    }

    public boolean isWaitToContinue() {
        return isWaitToContinue;
    }

    public static void sendTaskMessage(Player player, Object[] messages, boolean waitToContinue) {
        AbstractTutorial tutorial = AbstractTutorial.getActiveTutorial(player.getUniqueId());
        if (tutorial == null || tutorial.getNPC() == null) return;

        // Send the task message
        player.sendMessage(text());
        player.sendMessage(text(tutorial.getNPC().getDisplayName() + " ")
                .append(TutorialUtils.CHAT_PREFIX_COMPONENT));
        for (Object message : messages) {
            if (message instanceof ClickableTaskMessage) {
                player.sendMessage(((ClickableTaskMessage) message).getComponent());
            } else if (message instanceof Component) {
                player.sendMessage(((Component) message).color(GRAY));
            } else if (message instanceof String) {
                player.sendMessage(text((String) message).color(GRAY));
            }
        }

        // Send the continue task message
        player.sendMessage(text());
        if (waitToContinue) player.sendMessage(getContinueButtonComponent(LangUtil.getInstance().get(player, LangPaths.Note.Action.CONTINUE),
                LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_PROCEED)));
    }

    public static Component getContinueButtonComponent(String text, String hoverText) {
        return text("[", DARK_GRAY).append(text(text, GREEN).append(text("]", DARK_GRAY)))
                .hoverEvent(HoverEvent.showText(text(hoverText, GRAY))).clickEvent(ClickEvent.runCommand("/tutorial continue"));
    }


    public static class ClickableTaskMessage {
        private final Component messageComponent;
        private final Component hoverTextComponent;
        private final ClickEvent clickEvent;

        public ClickableTaskMessage(String message, String hoverText, ClickEvent clickEvent) {
            this.messageComponent = text(message);
            this.hoverTextComponent = text(hoverText);
            this.clickEvent = clickEvent;
        }

        public ClickableTaskMessage(Component messageComponent, Component hoverTextComponent, ClickEvent clickEvent) {
            this.messageComponent = messageComponent;
            this.hoverTextComponent = hoverTextComponent;
            this.clickEvent = clickEvent;
        }

        public Component getComponent() {
            return messageComponent.hoverEvent(HoverEvent.showText(hoverTextComponent)).clickEvent(clickEvent);
        }
    }
}
