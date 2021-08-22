/*
 * The MIT License (MIT)
 *
 *  Copyright © 2021, Alps BTE <bte.atchli@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.alpsbte.plotsystem.commands;

import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseCommand implements CommandExecutor, ICommand {
    private final List<SubCommand> subCommands = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (args.length > 0) {
            SubCommand subCommand = subCommands.stream()
                    .filter(subCmd -> Arrays.stream(subCmd.getNames()).anyMatch(sub -> sub.equalsIgnoreCase(args[0])))
                    .findFirst()
                    .orElse(null);

            if (subCommand == null) {
                sender.sendMessage("This sub command does not exist!");
            } else {
                if (!sender.hasPermission(subCommand.getPermission())) {
                    sender.sendMessage(Utils.getErrorMessageFormat("You don't have permission to execute this command!"));
                    return true;
                }
                subCommand.onCommand(sender, Arrays.copyOfRange(args, 1, args.length));
            }
        }
        return true;
    }

    public List<SubCommand> getSubCommands() {
        return subCommands;
    }

    protected Player getPlayer(CommandSender sender) {
        return sender instanceof Player ? (Player) sender : null;
    }

    protected void registerSubCommand(SubCommand subCommand) {
        subCommands.add(subCommand);
    }

    @Override
    public void sendInfo(CommandSender sender) {
        List<String> lines = new ArrayList<>();
        if (!subCommands.isEmpty()) {
            lines.add(Utils.getInfoMessageFormat(getNames()[0] + " Commands:"));
            lines.add("§8--------------------------");
            getSubCommands().forEach(sub -> {
                StringBuilder subCommand = new StringBuilder("§b/" + getNames()[0] + " §6" + sub.getNames()[0] + "§7");
                for (String parameter : sub.getParameter()) {
                    subCommand.append(" <").append(parameter).append(">");
                }
                subCommand.append(" §f- ").append(sub.getDescription());
                lines.add(subCommand.toString());
            });
            lines.add("§8--------------------------");
        } else {
            StringBuilder baseCommand = new StringBuilder("§b/" + getNames()[0] + "§7");
            for (String parameter : getParameter()) {
                baseCommand.append(" <").append(parameter).append(">");
            }
            baseCommand.append(" §f- ").append(getDescription());
            lines.add(baseCommand.toString());
        }
        lines.forEach(sender::sendMessage);
    }
}
