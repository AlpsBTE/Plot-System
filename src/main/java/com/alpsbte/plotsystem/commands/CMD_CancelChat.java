package com.alpsbte.plotsystem.commands;

import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.chat.ChatInput;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CMD_CancelChat extends BaseCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.PLAYER_HAS_NO_PERMISSIONS)));
            return true;
        }

        if (getPlayer(sender) == null) return true;

        Player player = getPlayer(sender);
        if (!ChatInput.awaitChatInput.containsKey(player.getUniqueId())) return true;
        ChatInput.awaitChatInput.remove(player.getUniqueId());
        player.sendMessage(Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(player, LangPaths.Message.Error.CHAT_INPUT_EXPIRED)));
        player.playSound(player.getLocation(), Utils.SoundUtils.DONE_SOUND, 1f, 1f);
        return true;
    }

    @Override
    public String[] getNames() {
        return new String[]{"cancelchat"};
    }

    @Override
    public String getDescription() {
        return "Cancels the chat input.";
    }

    @Override
    public String[] getParameter() {
        return new String[0];
    }

    @Override
    public String getPermission() {
        return "plotsystem.chatcancel";
    }
}
