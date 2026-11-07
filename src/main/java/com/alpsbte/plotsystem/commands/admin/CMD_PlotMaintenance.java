package com.alpsbte.plotsystem.commands.admin;

import com.alpsbte.plotsystem.commands.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Admin maintenance for leftover finished plot worlds, CIM rebuilds, and completed schematic repair.
 */
public class CMD_PlotMaintenance extends BaseCommand {
    public CMD_PlotMaintenance() {
        registerSubCommand(new CMD_PlotMaintenance_Cleanup(this));
        registerSubCommand(new CMD_PlotMaintenance_Cim(this));
        registerSubCommand(new CMD_PlotMaintenance_Repair(this));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        if (args.length == 0) {
            sendInfo(sender);
            return true;
        }
        return super.onCommand(sender, cmd, s, args);
    }

    @Override
    public String[] getNames() {
        return new String[]{"plotmaintenance"};
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String[] getParameter() {
        return new String[0];
    }

    @Override
    public String getPermission() {
        return null;
    }
}
