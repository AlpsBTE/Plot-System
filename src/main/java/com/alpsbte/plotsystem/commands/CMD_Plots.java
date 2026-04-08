package com.alpsbte.plotsystem.commands;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.menus.PlayerPlotsMenu;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class CMD_Plots extends BaseCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        Player player = getPlayer(sender);
        if (player == null) {
            sender.sendMessage(Component.text("This command can only be used as a player!", NamedTextColor.RED));
            return true;
        }

        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.PLAYER_HAS_NO_PERMISSIONS)));
            return true;
        }

        if (args.length < 1) {
            CompletableFuture.runAsync(() -> {
                Builder builder = Builder.byUUID(player.getUniqueId());
                Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> new PlayerPlotsMenu(player, builder));
            });
        }

        CompletableFuture.runAsync(() -> {
            Builder builder = Builder.byName(args[0]);
            if (builder == null) {
                player.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.PLAYER_NOT_FOUND)));
                return;
            }
            Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> new PlayerPlotsMenu(player, builder));
        });
        return true;
    }

    @Override
    public String[] getNames() {
        return new String[]{"plots"};
    }

    @Override
    public String getDescription() {
        return "Shows all plots of the given player.";
    }

    @Override
    public String[] getParameter() {
        return new String[]{"Player"};
    }

    @Override
    public String getPermission() {
        return "plotsystem.plots";
    }
}
