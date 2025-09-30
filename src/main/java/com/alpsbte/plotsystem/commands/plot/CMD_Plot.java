package com.alpsbte.plotsystem.commands.plot;

import com.alpsbte.plotsystem.commands.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CMD_Plot extends BaseCommand {

    // Register Sub-Commands
    public CMD_Plot() {
        registerSubCommand(new CMD_Plot_Teleport(this));
        registerSubCommand(new CMD_Plot_Links(this));
        registerSubCommand(new CMD_Plot_Submit(this));
        registerSubCommand(new CMD_Plot_Abandon(this));
        registerSubCommand(new CMD_Plot_Invite(this));
        registerSubCommand(new CMD_Plot_Feedback(this));
        registerSubCommand(new CMD_Plot_UndoSubmit(this));
        registerSubCommand(new CMD_Plot_Members(this));
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
        return new String[]{"plot", "p"};
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
