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

package com.alpsbte.plotsystem.commands.admin.setup;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.commands.SubCommand;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.command.CommandSender;

import java.util.List;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;

public class CMD_Setup_Server extends SubCommand {

    public CMD_Setup_Server(BaseCommand baseCommand) {
        super(baseCommand);
        register();
    }

    private void register() {
        registerSubCommand(new CMD_Setup_Server_List(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_Server_Add(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_Server_Remove(getBaseCommand(), this));
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        sendInfo(sender);
    }

    @Override
    public String[] getNames() {
        return new String[]{"server"};
    }

    @Override
    public String getDescription() {
        return "Configure servers";
    }

    @Override
    public String[] getParameter() {
        return new String[0];
    }

    @Override
    public String getPermission() {
        return "plotsystem.admin.pss.server";
    }


    public static class CMD_Setup_Server_List extends SubCommand {
        public CMD_Setup_Server_List(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            List<String> servers = DataProvider.SERVER.getServers();
            if (servers.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getInfoFormat("There are currently no Servers registered in the database!"));
                return;
            }

            sender.sendMessage(Utils.ChatUtils.getInfoFormat("There are currently " + servers.size() + " Servers registered in the database:"));

            for (String server : servers) {
                sender.sendMessage(text(" » ", DARK_GRAY).append(text(server, AQUA)));
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
            return "plotsystem.admin.pss.server.list";
        }
    }

    public static class CMD_Setup_Server_Add extends SubCommand {
        public CMD_Setup_Server_Add(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 2 || AlpsUtils.tryParseInt(args[2]) == null) {sendInfo(sender); return;}

            String serverName = args[1];
            if (serverName.length() > 255) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Server name cannot be longer than 255 characters!"));
                sendInfo(sender);
                return;
            }

            int buildTeamId = AlpsUtils.tryParseInt(args[2]);
            if (DataProvider.BUILD_TEAM.getBuildTeam(buildTeamId) == null) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Build team with id " + buildTeamId + " could not be found!"));
            }

            boolean successful = DataProvider.SERVER.addServer(serverName, buildTeamId);
            if (successful) sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully added server!"));
            else sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
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
            return new String[]{"Name", "BuildTeamId"};
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.server.add";
        }
    }

    public static class CMD_Setup_Server_Remove extends SubCommand {
        public CMD_Setup_Server_Remove(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 1) {sendInfo(sender); return;}

            // Check if server exists
            if (!DataProvider.SERVER.serverExists(args[1])) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Could not find any server with ID " + args[1] + "!"));
                sendInfo(sender);
                return;
            }

            boolean successful = DataProvider.SERVER.removeServer(args[1]);
            if (successful) sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully removed server with ID " + args[1] + "!"));
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
            return "plotsystem.admin.pss.server.remove";
        }
    }
}
