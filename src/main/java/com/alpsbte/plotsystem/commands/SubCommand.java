/*
 * The MIT License (MIT)
 *
 *  Copyright © 2025, Alps BTE <bte.atchli@gmail.com>
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

import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class SubCommand implements ICommand {

    private final BaseCommand baseCommand;
    private final SubCommand subCommand;
    private final List<SubCommand> subCommands = new ArrayList<>();
    protected final LangUtil langUtil = LangUtil.getInstance();

    protected SubCommand(BaseCommand baseCommand) {
        this.baseCommand = baseCommand;
        this.subCommand = null;
    }

    protected SubCommand(BaseCommand baseCommand, SubCommand subCommand) {
        this.baseCommand = baseCommand;
        this.subCommand = subCommand;
    }

    /**
     * Executes sub command
     *
     * @param sender player or console
     * @param args   parameter
     */
    public abstract void onCommand(CommandSender sender, String[] args);

    /**
     * Gets the player
     *
     * @param sender player
     * @return null if sender is not a player
     */
    protected @Nullable Player getPlayer(CommandSender sender) {
        return sender instanceof Player player ? player : null;
    }

    /**
     * Registers sub command
     *
     * @param subCommand this
     */
    public void registerSubCommand(SubCommand subCommand) {
        subCommands.add(subCommand);
    }

    /**
     * @return Base Command
     */
    public BaseCommand getBaseCommand() {
        return baseCommand;
    }

    /**
     * @return Sub Command (if it exists)
     */
    public SubCommand getSubCommand() {
        return subCommand;
    }

    /**
     * @return All sub commands
     */
    public List<SubCommand> getSubCommands() {
        return subCommands;
    }

    @Override
    public void sendInfo(CommandSender sender) {
        List<String> lines = new ArrayList<>();
        if (!subCommands.isEmpty()) {
            lines.add("§8--------------------------");
            getSubCommands().forEach(sub -> {
                StringBuilder subCommand = new StringBuilder("§7§l> §b/" + getBaseCommand().getNames()[0] + " §6" + getNames()[0] + " " + sub.getNames()[0] + "§7");
                for (String parameter : sub.getParameter()) {
                    subCommand.append(" <").append(parameter).append(">");
                }
                if (sub.getDescription() != null) subCommand.append(" §f- ").append(sub.getDescription());
                lines.add(subCommand.toString());
            });
            lines.add("§8--------------------------");
        } else {
            StringBuilder baseCommand = new StringBuilder("§7§l> §b/" + getBaseCommand().getNames()[0] + " §6" + (getSubCommand() != null ? getSubCommand().getNames()[0] + " " : "") + getNames()[0] + "§7");
            for (String parameter : getParameter()) {
                baseCommand.append(" <").append(parameter).append(">");
            }
            if (getDescription() != null) baseCommand.append(" §f- ").append(getDescription());
            lines.add(baseCommand.toString());
        }
        lines.forEach(sender::sendMessage);
    }
}
