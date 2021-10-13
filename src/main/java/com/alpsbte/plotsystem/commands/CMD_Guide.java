package com.alpsbte.plotsystem.commands;

import com.alpsbte.plotsystem.core.menus.GuideBook;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMD_Guide extends BaseCommand{

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender.hasPermission(getPermission())) {
            if (getPlayer(sender) != null) {
                //TODO: Open Menu
                new GuideBook(getPlayer(sender));
            } else {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "This command can only be used as a player!");
            }
        } else {
            sender.sendMessage(Utils.getErrorMessageFormat("You don't have permission to use this command!"));
        }
        return true;
    }

    @Override
    public String[] getNames() {
        return new String[] { "guide" };
    }

    @Override
    public String getDescription() {
        return "Opens the guidebook.";
    }

    @Override
    public String[] getParameter() {
        return new String[0];
    }

    @Override
    public String getPermission() {
        return "plotsystem.guide";
    }
}
