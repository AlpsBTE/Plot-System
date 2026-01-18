package com.alpsbte.plotsystem.commands;

import org.bukkit.command.CommandSender;

public interface ICommand {
    /**
     * @return Command Name(s)
     */
    String[] getNames();

    /**
     * @return Command Description
     */
    String getDescription();

    /**
     * @return Command Parameter(s)
     */
    String[] getParameter();

    /**
     * @return Command Permission
     */
    String getPermission();

    /**
     * Lists all relevant commands to the player
     *
     * @param sender player or console
     */
    void sendInfo(CommandSender sender);
}
