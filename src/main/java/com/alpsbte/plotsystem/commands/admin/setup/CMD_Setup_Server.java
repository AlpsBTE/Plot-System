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
import com.alpsbte.plotsystem.core.system.FTPConfiguration;
import com.alpsbte.plotsystem.core.system.Server;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class CMD_Setup_Server extends SubCommand {

    public CMD_Setup_Server(BaseCommand baseCommand) {
        super(baseCommand);
        register();
    }

    private void register() {
        registerSubCommand(new CMD_Setup_Server_List(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_Server_Add(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_Server_Remove(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_Server_SetFTP(getBaseCommand(), this));
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        sendInfo(sender);
    }

    @Override
    public String[] getNames() {
        return new String[] { "server" };
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
            List<Server> servers = Server.getServers();
            if (servers.size() != 0) {
                sender.sendMessage(Utils.ChatUtils.getInfoMessageFormat("There are currently " + servers.size() + " Servers registered in the database:"));
                sender.sendMessage("§8--------------------------");
                for (Server s : servers) {
                    try {
                        sender.sendMessage(" §6> §b" + s.getID() + " (" + s.getName() + ") §f- FTP-Configuration: " + (s.getFTPConfiguration() == null ? "None" : s.getFTPConfiguration().getID()));
                    } catch (SQLException ex) {
                        Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                    }
                }
                sender.sendMessage("§8--------------------------");
            } else {
                sender.sendMessage(Utils.ChatUtils.getInfoMessageFormat("There are currently no Servers registered in the database!"));
            }
        }

        @Override
        public String[] getNames() {
            return new String[] { "list" };
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
            if (args.length > 1) {
                if (args[1].length() <= 45) {
                    try {
                        Server server = Server.addServer(args[1]);
                        Path serverPath = Paths.get(PlotManager.getDefaultSchematicPath(), String.valueOf(server.getID()));
                        if (serverPath.toFile().exists()) FileUtils.deleteDirectory(serverPath.toFile());
                        if (!serverPath.toFile().mkdirs()) throw new IOException();
                        sender.sendMessage(Utils.ChatUtils.getInfoMessageFormat("Successfully added server!"));
                    } catch (SQLException | IOException ex) {
                        sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("An error occurred while executing command!"));
                        Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                    }
                } else {
                    sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("Server name cannot be longer than 45 characters!"));
                }
                return;
            }
            sendInfo(sender);
        }

        @Override
        public String[] getNames() {
            return new String[] { "add" };
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[] { "Name" };
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
            if (args.length > 1 && AlpsUtils.TryParseInt(args[1]) != null) {
                // Check if server exists
                try {
                    if (Server.getServers().stream().anyMatch(s -> s.getID() == Integer.parseInt(args[1]))) {
                        Server.removeServer(Integer.parseInt(args[1]));
                        Path serverPath = Paths.get(PlotManager.getDefaultSchematicPath(), args[1]);
                        if (serverPath.toFile().exists()) FileUtils.deleteDirectory(serverPath.toFile());
                        sender.sendMessage(Utils.ChatUtils.getInfoMessageFormat("Successfully removed server with ID " + args[1] + "!"));
                    } else {
                        sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("Could not find any server with ID " + args[1] + "!"));
                        sendInfo(sender);
                    }
                } catch (SQLException | IOException ex) {
                    sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("An error occurred while executing command!"));
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                }
                return;
            }
            sendInfo(sender);
        }

        @Override
        public String[] getNames() {
            return new String[] { "remove" };
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[] { "Server-ID" };
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.server.remove";
        }
    }

    public static class CMD_Setup_Server_SetFTP extends SubCommand {
        public CMD_Setup_Server_SetFTP(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length > 2 && AlpsUtils.TryParseInt(args[1]) != null) {
                // Check if server exists
                try {
                    if (Server.getServers().stream().anyMatch(s -> s.getID() == Integer.parseInt(args[1]))) {
                        if (args[2].equalsIgnoreCase("none") || AlpsUtils.TryParseInt(args[2]) != null && FTPConfiguration.getFTPConfigurations().stream().anyMatch(f -> f.getID() == Integer.parseInt(args[2]))) {
                            int ftpID = AlpsUtils.TryParseInt(args[2]) != null ? Integer.parseInt(args[2]) : -1;
                            Server.setFTP(Integer.parseInt(args[1]), ftpID);
                            sender.sendMessage(Utils.ChatUtils.getInfoMessageFormat("Successfully set FTP Configuration of server with ID " + args[1] + " to " + (ftpID == -1 ? "None" : ftpID) + "!"));
                        } else {
                            sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("Could not find any ftp configurations with ID " + args[2] + "!"));
                            sendInfo(sender);
                        }
                    } else {
                        sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("Could not find any server with ID " + args[1] + "!"));
                        sendInfo(sender);
                    }
                } catch (SQLException ex) {
                    sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("An error occurred while executing command!"));
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                }
                return;
            }
            sendInfo(sender);
        }

        @Override
        public String[] getNames() {
            return new String[] { "setftp" };
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[] { "Server-ID", "FTP-ID/None" };
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.server.setftp";
        }
    }
}
