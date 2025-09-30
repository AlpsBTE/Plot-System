package com.alpsbte.plotsystem.commands.admin;

import com.alpsbte.alpslib.hologram.DecentHologramDisplay;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.core.holograms.HologramConfiguration;
import com.alpsbte.plotsystem.core.holograms.HologramRegister;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.text;

public class CMD_PReload extends BaseCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        PlotSystem plugin = PlotSystem.getPlugin();
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat("You don't have permission to use this command!"));
            return true;
        }

        try {
            plugin.reloadConfig();
            LangUtil.getInstance().reloadFiles();
            sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully reloaded config!"));

            DecentHologramDisplay.activeDisplays.forEach(leaderboard -> leaderboard.setLocation(HologramRegister
                    .getLocation((HologramConfiguration) leaderboard)));
            HologramRegister.reload();
            sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully reloaded leaderboards!"));
            plugin.initDatabase();
        } catch (Exception ex) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
            plugin.getComponentLogger().error(text("An error occurred!"), ex);
        }
        return true;
    }

    @Override
    public String[] getNames() {
        return new String[]{"preload"};
    }

    @Override
    public String getDescription() {
        return "Reloads configuration files and leaderboards.";
    }

    @Override
    public String[] getParameter() {
        return new String[0];
    }

    @Override
    public String getPermission() {
        return "plotsystem.admin.preload";
    }
}