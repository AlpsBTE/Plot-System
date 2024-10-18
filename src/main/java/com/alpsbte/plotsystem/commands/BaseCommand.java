/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
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
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseCommand implements CommandExecutor, ICommand {
    private final List<SubCommand> subCommands = new ArrayList<>();
    protected final LangUtil langUtil = LangUtil.getInstance();

    /**
     * Executes base command and checks for sub commands
     *
     * @param sender player or console
     * @param args   parameter
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        if (args.length > 0) {
            SubCommand subCommand = subCommands.stream()
                    .filter(subCmd -> Arrays.stream(subCmd.getNames()).anyMatch(sub -> sub.equalsIgnoreCase(args[0])))
                    .findFirst()
                    .orElse(null);

            // Check if sub commands have sub commands
            if (subCommand != null && args.length > 1) {
                SubCommand iterateSubCommand;
                for (int i = 1; i <= args.length; i++) {
                    final int index = i;
                    iterateSubCommand = subCommand.getSubCommands().stream()
                            .filter(subCmd -> Arrays.stream(subCmd.getNames()).anyMatch(sub -> sub.equalsIgnoreCase(args[index])))
                            .findFirst()
                            .orElse(null);

                    if (iterateSubCommand == null) {
                        break;
                    } else {
                        subCommand = iterateSubCommand;
                    }
                }
            }

            if (subCommand == null) {
                sendInfo(sender);
            } else {
                if (subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())) {
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat("You don't have permission to execute this command!"));
                    return true;
                }
                subCommand.onCommand(sender, Arrays.copyOfRange(args, 1, args.length));
            }
        }
        return true;
    }

    /**
     * @return All sub commands
     */
    public List<SubCommand> getSubCommands() {
        return subCommands;
    }

    /**
     * Gets the player
     *
     * @param sender player
     * @return null if sender is not a player
     */
    protected Player getPlayer(CommandSender sender) {
        return sender instanceof Player ? (Player) sender : null;
    }

    /**
     * Registers sub command
     *
     * @param subCommand base sub command
     */
    protected void registerSubCommand(SubCommand subCommand) {
        subCommands.add(subCommand);
    }

    @Override
    public void sendInfo(CommandSender sender) {
        List<String> lines = new ArrayList<>();
        if (!subCommands.isEmpty()) {
            lines.add("§8--------------------------");
            getSubCommands().forEach(sub -> {
                StringBuilder subCommand = new StringBuilder("§7§l> §b/" + getNames()[0] + " §6" + sub.getNames()[0] + "§7");
                for (String parameter : sub.getParameter()) {
                    subCommand.append(" <").append(parameter).append(">");
                }
                if (sub.getDescription() != null) subCommand.append(" §f- ").append(sub.getDescription());
                lines.add(subCommand.toString());
            });
            lines.add("§8--------------------------");
        } else {
            StringBuilder baseCommand = new StringBuilder("§7§l> §b/" + getNames()[0] + "§7");
            for (String parameter : getParameter()) {
                baseCommand.append(" <").append(parameter).append(">");
            }
            if (getDescription() != null) baseCommand.append(" §f- ").append(getDescription());
            lines.add(baseCommand.toString());
        }
        lines.forEach(sender::sendMessage);
    }
}
