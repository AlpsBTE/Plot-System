/*
 *  The MIT License (MIT)
 *
 *  Copyright © 2021-2025, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.commands.admin.setup;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.commands.SubCommand;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.review.ToggleCriteria;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class CMD_Setup_ReviewCriteria extends SubCommand {
    public CMD_Setup_ReviewCriteria(BaseCommand baseCommand) {
        super(baseCommand);
        registerSubCommand(new CMD_Setup_ReviewCriteria_List(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_ReviewCriteria_Add(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_ReviewCriteria_Remove(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_ReviewCriteria_SetOptional(getBaseCommand(), this));
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        sendInfo(sender);
    }

    @Override
    public String[] getNames() {
        return new String[]{"review"};
    }

    @Override
    public String getDescription() {
        return "Configure review criteria";
    }

    @Override
    public String[] getParameter() {
        return new String[0];
    }

    @Override
    public String getPermission() {
        return "plotsystem.admin.pss.review";
    }

    public static class CMD_Setup_ReviewCriteria_List extends SubCommand {

        public CMD_Setup_ReviewCriteria_List(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            List<ToggleCriteria> criteria = DataProvider.REVIEW.getAllToggleCriteria();
            if (criteria.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getInfoFormat("There are currently no toggle criteria registered in the database!"));
                return;
            }

            sender.sendMessage(Utils.ChatUtils.getInfoFormat("There are currently " + criteria.size() + " toggle criteria registered in the database:"));
            sender.sendMessage(text("--------------------------", DARK_GRAY));
            for (ToggleCriteria c : criteria) {
                sender.sendMessage(text(" » ", DARK_GRAY)
                        .append(text(c.getCriteriaName() + " (" + (c.isOptional() ? "optional" : "required") + ")")));
            }
            sender.sendMessage(text("--------------------------", DARK_GRAY));
        }

        @Override
        public String[] getNames() {
            return new String[]{"list"};
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[0];
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.review.list";
        }
    }

    public static class CMD_Setup_ReviewCriteria_Add extends SubCommand {
        public CMD_Setup_ReviewCriteria_Add(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 2) {sendInfo(sender); return;}

            String name = args[1];
            if (name.length() > 255) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Toggle criteria name cannot be longer than 255 characters!"));
                return;
            }

            boolean isOptional = args[2].equalsIgnoreCase("true");

            boolean successful = DataProvider.REVIEW.addToggleCriteria(name, isOptional);
            if (!successful) sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
            else sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully added toggle criteria with name '" + name + "'!"));
        }

        @Override
        public String[] getNames() {
            return new String[]{"add"};
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[]{"Name","Is-Optional"};
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.review.add";
        }
    }

    public static class CMD_Setup_ReviewCriteria_Remove extends SubCommand {
        public CMD_Setup_ReviewCriteria_Remove(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 1) {sendInfo(sender); return;}

            // Check if criteria exists
            Optional<ToggleCriteria> criteria = DataProvider.REVIEW.getToggleCriteria(args[1]);
            if (criteria.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Could not find any toggle criteria with name " + args[1] + "!"));
                sendInfo(sender);
                return;
            }

            boolean successful = DataProvider.REVIEW.removeToggleCriteria(criteria.get().getCriteriaName());
            if (successful) sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully removed build team with ID " + args[1] + "!"));
            else sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
        }

        @Override
        public String[] getNames() {
            return new String[]{"remove"};
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[]{"Name"};
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.review.remove";
        }
    }

    public static class CMD_Setup_ReviewCriteria_SetOptional extends SubCommand {
        public CMD_Setup_ReviewCriteria_SetOptional(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 2) {sendInfo(sender); return;}

            // Check if criteria exits
            Optional<ToggleCriteria> criteria = DataProvider.REVIEW.getToggleCriteria(args[1]);
            if (criteria.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Toggle criteria could not be found!"));
                return;
            }

            String name = args[1];
            boolean isOptional = args[2].equalsIgnoreCase("true");

            boolean successful = DataProvider.REVIEW.setToggleCriteriaOptional(name, isOptional);
            if (successful) sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully changed optionality of toggle criteria with name " + name + " to '" + isOptional + "'!"));
            else sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
        }

        @Override
        public String[] getNames() {
            return new String[]{"setoptional"};
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[]{"Name", "Is-Optional"};
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.buildteam.setoptional";
        }
    }

}
