package com.alpsbte.plotsystem.utils.chat;

import com.alpsbte.plotsystem.core.system.review.PlotReview;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

public class PlayerFeedbackChatInput implements ChatInput {
    private final LocalDateTime dateTime;
    private final PlotReview review;

    public PlayerFeedbackChatInput(UUID playerUUID, PlotReview review) {
        this.dateTime = LocalDateTime.now();
        this.review = review;
        awaitChatInput.put(playerUUID, this);
    }

    @Override
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public PlotReview getReview() {
        return review;
    }

    public static void sendChatInputMessage(Player player) {
        player.sendMessage(Component.empty());
        player.sendMessage(Utils.ChatUtils.getInfoFormat(text(LangUtil.getInstance().get(player, LangPaths.Message.Info.CHAT_ENTER_FEEDBACK), GRAY)));
        Utils.ChatUtils.sendChatInputExpiryComponent(player);
        player.sendMessage(Component.empty());
        player.playSound(player.getLocation(), Utils.SoundUtils.NOTIFICATION_SOUND, 1f, 1f);
    }
}
