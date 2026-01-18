package com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.message;

import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.AbstractTask;
import com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialUtils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;

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

    public static void sendTaskMessage(@NotNull Player player, Object[] messages, boolean waitToContinue) {
        AbstractTutorial tutorial = AbstractTutorial.getActiveTutorial(player.getUniqueId());
        if (tutorial == null || tutorial.getNPC() == null) return;

        // Send the task message
        player.sendMessage(text());
        player.sendMessage(text(tutorial.getNPC().getDisplayName() + " ")
                .append(TutorialUtils.CHAT_PREFIX_COMPONENT));
        for (Object message : messages) {
            if (message instanceof ClickableTaskMessage ctm) {
                player.sendMessage(ctm.getComponent());
            } else if (message instanceof Component c) {
                player.sendMessage(c.color(GRAY));
            } else if (message instanceof String s) {
                player.sendMessage(text(s).color(GRAY));
            }
        }

        // Send the continue task message
        player.sendMessage(text());
        if (waitToContinue) player.sendMessage(getContinueButtonComponent(LangUtil.getInstance().get(player, LangPaths.Note.Action.CONTINUE),
                LangUtil.getInstance().get(player, LangPaths.Note.Action.CLICK_TO_PROCEED)));
    }

    public static @NotNull Component getContinueButtonComponent(String text, String hoverText) {
        return text("[", DARK_GRAY).append(text(text, GREEN).append(text("]", DARK_GRAY)))
                .hoverEvent(HoverEvent.showText(text(hoverText, GRAY))).clickEvent(ClickEvent.runCommand("/tutorial continue"));
    }


    public static class ClickableTaskMessage {
        private final Component messageComponent;
        private final Component hoverTextComponent;
        private final ClickEvent clickEvent;

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
