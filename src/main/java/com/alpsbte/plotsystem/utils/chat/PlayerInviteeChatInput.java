package com.alpsbte.plotsystem.utils.chat;

import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

public class PlayerInviteeChatInput implements ChatInput {
    private final LocalDateTime dateTime;
    private final Plot plot;

    public PlayerInviteeChatInput(UUID playerUUID, Plot plot) {
        this.dateTime = LocalDateTime.now();
        this.plot = plot;
        awaitChatInput.put(playerUUID, this);
    }

    @Override
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public Plot getPlot() {
        return plot;
    }

    public static void sendChatInputMessage(Player player) {
        player.sendMessage(Component.empty());
        player.sendMessage(Utils.ChatUtils.getInfoFormat(text(LangUtil.getInstance().get(player, LangPaths.Message.Info.CHAT_ENTER_PLAYER), GRAY)));
        Utils.ChatUtils.sendChatInputExpiryComponent(player);
        player.sendMessage(Component.empty());
        player.playSound(player.getLocation(), Utils.SoundUtils.NOTIFICATION_SOUND, 1f, 1f);
    }
}
