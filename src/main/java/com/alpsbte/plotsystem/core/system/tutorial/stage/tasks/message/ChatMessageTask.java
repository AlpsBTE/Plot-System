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

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.AbstractTask;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

import static net.md_5.bungee.api.ChatColor.*;

public class ChatMessageTask extends AbstractTask {
    public static final String TASK_PREFIX = DARK_GRAY + "> " + GRAY;
    private static final String CONTINUE_TASK_MESSAGE = DARK_GRAY + "[" + GREEN + "{0}" + DARK_GRAY + "]";

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
        // Send the task message
        player.sendMessage("");
        for (Object message : messages) {
            if (message instanceof String) {
                List<String> messageLines = AlpsUtils.createMultilineFromString((String) message, -1, Utils.ChatUtils.LINE_BREAKER);
                messageLines.forEach(msg -> player.sendMessage((!msg.equals("") ? TASK_PREFIX : "") + msg));
            } else if (message instanceof ClickableTaskMessage) {
                TextComponent component = ((ClickableTaskMessage) message).getComponent();
                component.setText(TASK_PREFIX + component.getText());
                player.spigot().sendMessage(component);
            }
        }

        // Send the continue task message
        player.sendMessage("");
        if (waitToContinue) player.spigot().sendMessage(getContinueTextComponent(player));
    }

    private static TextComponent getContinueTextComponent(Player player) {
        return new ClickableTaskMessage(CONTINUE_TASK_MESSAGE.replace("{0}",
                LangUtil.getInstance().get(player, LangPaths.Note.Action.CONTINUE)),
                GRAY + LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_PROCEED),
                "/tutorial continue",
                ClickEvent.Action.RUN_COMMAND).getComponent();
    }


    public static class ClickableTaskMessage {
        private final String message;
        private final String hoverText;
        private final String clickExecute;
        private final ClickEvent.Action clickAction;

        public ClickableTaskMessage(String message, String hoverText, String clickExecute, ClickEvent.Action clickAction) {
            this.message = message;
            this.hoverText = hoverText;
            this.clickExecute = clickExecute;
            this.clickAction = clickAction;
        }

        public TextComponent getComponent() {
            TextComponent clickableMessage = new TextComponent(message);
            clickableMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
            clickableMessage.setClickEvent(new ClickEvent(clickAction, clickExecute));
            return clickableMessage;
        }
    }
}
