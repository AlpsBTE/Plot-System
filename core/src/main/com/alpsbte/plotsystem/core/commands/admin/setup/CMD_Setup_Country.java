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
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class CMD_Setup_Country extends SubCommand {

    public CMD_Setup_Country(BaseCommand baseCommand) {
        super(baseCommand);
        register();
    }

    private void register() {
        registerSubCommand(new CMD_Setup_Country_List(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_Country_Add(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_Country_Remove(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_Country_SetHead(getBaseCommand(), this));
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        sendInfo(sender);
    }

    @Override
    public String[] getNames() {
        return new String[] { "country" };
    }

    @Override
    public String getDescription() {
        return "Configure countries";
    }

    @Override
    public String[] getParameter() {
        return new String[0];
    }

    @Override
    public String getPermission() {
        return "plotsystem.admin.pss.country";
    }



    public static class CMD_Setup_Country_List extends SubCommand {
        public CMD_Setup_Country_List(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            List<Country> countries = Country.getCountries();
            if (countries.size() != 0) {
                sender.sendMessage(Utils.getInfoMessageFormat("There are currently " + countries.size() + " Countries registered in the database:"));
                sender.sendMessage("§8--------------------------");
                for (Country c : countries) {
                    try {
                        sender.sendMessage(" §6> §b" + c.getID() + " (" + c.getName() + ") §f- Server: " + c.getServer().getID() + " (" + c.getServer().getName() + ")");
                    } catch (SQLException ex) {
                        Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                    }
                }
                sender.sendMessage("§8--------------------------");
            } else {
                sender.sendMessage(Utils.getInfoMessageFormat("There are currently no countries registered in the database!"));
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
            return "plotsystem.admin.pss.country.list";
        }
    }

    public static class CMD_Setup_Country_Add extends SubCommand {
        public CMD_Setup_Country_Add(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length > 2 && Utils.TryParseInt(args[1]) != null) {
                if (args[2].length() <= 45) {
                    try {
                        Country.addCountry(Integer.parseInt(args[1]), args[2]);
                        sender.sendMessage(Utils.getInfoMessageFormat("Successfully added country!"));
                    } catch (SQLException ex) {
                        sender.sendMessage(Utils.getErrorMessageFormat("An error occurred while executing command!"));
                        Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                    }
                } else {
                    sender.sendMessage(Utils.getErrorMessageFormat("Country name cannot be longer than 45 characters!"));
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
            return new String[] { "Server-ID", "Name" };
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.country.add";
        }
    }

    public static class CMD_Setup_Country_Remove extends SubCommand {
        public CMD_Setup_Country_Remove(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length > 1 && Utils.TryParseInt(args[1]) != null) {
                // Check if country exists
                try {
                    if (Country.getCountries().stream().anyMatch(c -> c.getID() == Integer.parseInt(args[1]))) {
                        Country.removeCountry(Integer.parseInt(args[1]));
                        sender.sendMessage(Utils.getInfoMessageFormat("Successfully removed country!"));
                    } else {
                        sender.sendMessage(Utils.getErrorMessageFormat("Could not find any country with ID " + args[1] + "!"));
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
            return new String[] { "Country-ID" };
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.country.remove";
        }
    }

    public static class CMD_Setup_Country_SetHead extends SubCommand {
        public CMD_Setup_Country_SetHead(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length > 2 && Utils.TryParseInt(args[1]) != null && Utils.TryParseInt(args[2]) != null) {
                // Check if country exists
                try {
                    if (Country.getCountries().stream().anyMatch(c -> c.getID() == Integer.parseInt(args[1]))) {
                        Country.setHeadID(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                        sender.sendMessage(Utils.getInfoMessageFormat("Successfully set head id of country " + args[1] + " to " + args[2] + "!"));
                    } else {
                        sender.sendMessage(Utils.getErrorMessageFormat("Could not find any country with name " + args[1] + "!"));
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
            return new String[] { "sethead" };
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[] { "Country-ID", "Head-ID" };
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.country.sethead";
        }
    }
}
