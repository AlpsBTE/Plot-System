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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class SubCommand implements ICommand {

    private final BaseCommand baseCommand;

    private final List<SubCommand> subCommands = new ArrayList<>();

    public SubCommand(BaseCommand baseCommand) {
        this.baseCommand = baseCommand;
    }

    public abstract void onCommand(CommandSender sender, String[] args);

    protected Player getPlayer(CommandSender sender) {
        return sender instanceof Player ? (Player) sender : null;
    }

    public void registerSubCommand(SubCommand subCommand) {
        subCommands.add(subCommand);
    }

    public BaseCommand getBaseCommand() {
        return baseCommand;
    }

    public List<SubCommand> getSubCommands() {
        return subCommands;
    }

    @Override
    public void sendInfo(CommandSender sender) {
        List<String> lines = new ArrayList<>();
        if (!subCommands.isEmpty()) {
            lines.add(Utils.getInfoMessageFormat(getNames()[0].substring(0, 1).toUpperCase() + getNames()[0].substring(1) + " Commands:"));
            lines.add("§8--------------------------");
            getSubCommands().forEach(sub -> {
                StringBuilder subCommand = new StringBuilder("§7§l> §b/" + getBaseCommand().getNames()[0] + " §6" + getNames()[0] + " " + sub.getNames()[0] + "§7");
                for (String parameter : sub.getParameter()) {
                    subCommand.append(" <").append(parameter).append(">");
                }
                subCommand.append(" §f- ").append(sub.getDescription());
                lines.add(subCommand.toString());
            });
            lines.add("§8--------------------------");
        } else {
            StringBuilder baseCommand = new StringBuilder("§7§l> §b/" + getBaseCommand().getNames()[0] + " §6" + getNames()[0] + "§7");
            for (String parameter : getParameter()) {
                baseCommand.append(" <").append(parameter).append(">");
            }
            baseCommand.append(" §f- ").append(getDescription());
            lines.add(baseCommand.toString());
        }
        lines.forEach(sender::sendMessage);
    }
}
