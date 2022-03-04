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

package com.alpsbte.plotsystem.core.commands.admin.setup;

import com.alpsbte.plotsystem.core.commands.BaseCommand;
import com.alpsbte.plotsystem.core.commands.SubCommand;
import com.alpsbte.plotsystem.core.system.FTPConfiguration;
import com.alpsbte.plotsystem.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class CMD_Setup_FTP extends SubCommand {

    public CMD_Setup_FTP(BaseCommand baseCommand) {
        super(baseCommand);
        register();
    }

    private void register() {
        registerSubCommand(new CMD_Setup_FTP_List(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_FTP_Add(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_FTP_Remove(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_FTP_SetPath(getBaseCommand(), this));
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        sendInfo(sender);
    }

    @Override
    public String[] getNames() {
        return new String[] { "ftp" };
    }

    @Override
    public String getDescription() {
        return "Configure SFTP/FTP configurations";
    }

    @Override
    public String[] getParameter() {
        return new String[0];
    }

    @Override
    public String getPermission() {
        return "plotsystem.admin.pss.ftp";
    }



    public static class CMD_Setup_FTP_List extends SubCommand {
        public CMD_Setup_FTP_List(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            List<FTPConfiguration> ftpConfigs = FTPConfiguration.getFTPConfigurations();
            if (ftpConfigs.size() != 0) {
                sender.sendMessage(Utils.getInfoMessageFormat("There are currently " + ftpConfigs.size() + " FTP-Configurations registered in the database:"));
                sender.sendMessage("§8--------------------------");
                for (FTPConfiguration ftp : ftpConfigs) {
                    sender.sendMessage(" §6> §b" + ftp.getID() + " §f- Address: " + ftp.getAddress() + " - Port: " + ftp.getPort() + " - SFTP: " + (ftp.isSFTP() ? "True" : "False") + " - Username: " + getCensorString(ftp.getUsername().length()) + " - Password: " + getCensorString(ftp.getPassword().length()) + " - Path: " + ftp.getSchematicPath());
                }
                sender.sendMessage("§8--------------------------");
            } else {
                sender.sendMessage(Utils.getInfoMessageFormat("There are currently no FTP Configurations registered in the database!"));
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
            return "plotsystem.admin.pss.ftp.list";
        }

        public String getCensorString(int length) {
            StringBuilder output = new StringBuilder();
            for (int i = 0; i < length; i++) {
                output.append("*");
            }
            return output.toString();
        }
    }

    public static class CMD_Setup_FTP_Add extends SubCommand {
        public CMD_Setup_FTP_Add(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length > 5 && Utils.TryParseInt(args[2]) != null) {
                if (args[3].equalsIgnoreCase("true") || args[3].equalsIgnoreCase("false")) {
                    if (!args[1].toLowerCase().startsWith("sftp:") && !args[1].toLowerCase().startsWith("ftp:")) {
                        try {
                            FTPConfiguration.addFTPConfiguration(args[1], Integer.parseInt(args[2]), args[3].equalsIgnoreCase("true"), args[4], args[5]);
                            sender.sendMessage(Utils.getInfoMessageFormat("Successfully added FTP-Configuration!"));
                        } catch (SQLException ex) {
                            sender.sendMessage(Utils.getErrorMessageFormat("An error occurred while executing command!"));
                            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                        }
                    } else {
                        sender.sendMessage(Utils.getErrorMessageFormat("Please remove the protocol URL from the host address!"));
                    }
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
            return new String[] { "Address", "Port", "isSFTP (True/False)", "Username", "Password" };
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.ftp.add";
        }
    }

    public static class CMD_Setup_FTP_Remove extends SubCommand {
        public CMD_Setup_FTP_Remove(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length > 1 && Utils.TryParseInt(args[1]) != null) {
                // Check if ftp config exists
                try {
                    if (FTPConfiguration.getFTPConfigurations().stream().anyMatch(f -> f.getID() == Integer.parseInt(args[1]))) {
                        FTPConfiguration.removeFTPConfiguration(Integer.parseInt(args[1]));
                        sender.sendMessage(Utils.getInfoMessageFormat("Successfully removed FTP-Configuration!"));
                    } else {
                        sender.sendMessage(Utils.getErrorMessageFormat("Could not find any FTP-Configurations with ID " + args[1] + "!"));
                        sendInfo(sender);
                    }
                } catch (SQLException ex) {
                    sender.sendMessage(Utils.getErrorMessageFormat("An error occurred while executing command!"));
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
            return new String[] { "FTP-ID" };
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.ftp.remove";
        }
    }

    public static class CMD_Setup_FTP_SetPath extends SubCommand {
        public CMD_Setup_FTP_SetPath(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length > 2 && Utils.TryParseInt(args[1]) != null) {
                // Check if ftp config exists
                try {
                    if (FTPConfiguration.getFTPConfigurations().stream().anyMatch(f -> f.getID() == Integer.parseInt(args[1]))) {
                        FTPConfiguration.setSchematicPath(Integer.parseInt(args[1]), args[2]);
                        sender.sendMessage(Utils.getInfoMessageFormat("Successfully set path of FTP-Configuration " + args[1] + " to " + args[2] + "!"));
                    } else {
                        sender.sendMessage(Utils.getErrorMessageFormat("Could not find any FTP-Configurations with ID " + args[1] + "!"));
                        sendInfo(sender);
                    }
                } catch (SQLException ex) {
                    sender.sendMessage(Utils.getErrorMessageFormat("An error occurred while executing command!"));
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                }
                return;
            }
            sendInfo(sender);
        }

        @Override
        public String[] getNames() {
            return new String[] { "setpath" };
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[] { "FTP-ID", "Path" };
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.ftp.setpath";
        }
    }
}
