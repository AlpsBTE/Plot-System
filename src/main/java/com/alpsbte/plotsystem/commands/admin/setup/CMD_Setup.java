package com.alpsbte.plotsystem.commands.admin.setup;

import com.alpsbte.plotsystem.commands.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CMD_Setup extends BaseCommand {

    public CMD_Setup() {
        registerSubCommand(new CMD_Setup_BuildTeam(this));
        registerSubCommand(new CMD_Setup_Server(this));
        registerSubCommand(new CMD_Setup_Country(this));
        registerSubCommand(new CMD_Setup_City(this));
        registerSubCommand(new CMD_Setup_Difficulty(this));
        registerSubCommand(new CMD_Setup_ReviewCriteria(this));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        if (args.length == 0) {
            sendInfo(sender);
        }

        return super.onCommand(sender, cmd, s, args);
    }

    @Override
    public String[] getNames() {
        return new String[]{"pss"};
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
