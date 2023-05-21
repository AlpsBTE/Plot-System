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
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class CMD_Setup_City extends SubCommand {

    public CMD_Setup_City(BaseCommand baseCommand) {
        super(baseCommand);
        register();
    }

    private void register() {
        registerSubCommand(new CMD_Setup_City_List(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_City_Add(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_City_Remove(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_City_SetName(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_City_SetDescription(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_City_SetVisible(getBaseCommand(), this));
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        sendInfo(sender);
    }

    @Override
    public String[] getNames() {
        return new String[] { "city" };
    }

    @Override
    public String getDescription() {
        return "Configure city projects";
    }

    @Override
    public String[] getParameter() {
        return new String[0];
    }

    @Override
    public String getPermission() {
        return "plotsystem.admin.pss.city";
    }



    public static class CMD_Setup_City_List extends SubCommand {
        public CMD_Setup_City_List(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            List<CityProject> cities = CityProject.getCityProjects(false);
            if (cities.size() != 0) {
                sender.sendMessage(Utils.ChatUtils.getInfoMessageFormat("There are currently " + cities.size() + " City Projects registered in the database:"));
                sender.sendMessage("§8--------------------------");
                for (CityProject c : cities) {
                    try {
                        sender.sendMessage(" §6> §b" + c.getID() + " (" + c.getName() + ") §f- Description: " + c.getDescription() + " - Country: " + c.getCountry().getName() + " - Visible: " + c.isVisible());
                    } catch (SQLException ex) {
                        sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("An error occurred while executing command!"));
                        Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                    }
                }
                sender.sendMessage("§8--------------------------");
            } else {
                sender.sendMessage(Utils.ChatUtils.getInfoMessageFormat("There are currently no City Projects registered in the database!"));
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
            return "plotsystem.admin.pss.city.list";
        }
    }

    public static class CMD_Setup_City_Add extends SubCommand {
        public CMD_Setup_City_Add(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length > 2 && AlpsUtils.TryParseInt(args[1]) != null) {
                Country country = Country.getCountries().stream().filter(c -> c.getID() == Integer.parseInt(args[1])).findFirst().orElse(null);
                if (country != null) {
                    String name = CMD_Setup.appendArgs(args,2);
                    if (name.length() <= 45) {
                        try {
                            CityProject.addCityProject(country, name);
                            sender.sendMessage(Utils.ChatUtils.getInfoMessageFormat("Successfully added City Project with name '" + name + "' in country with the ID " + args[1] + "!"));
                        } catch (SQLException ex) {
                            sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("An error occurred while executing command!"));
                            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                            return;
                        }
                    } else {
                        sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("City Project name cannot be longer than 45 characters!"));
                    }
                } else {
                    sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("Could not find any country with ID " + args[1] + "!"));
                    sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("Type </pss country list> to see all countries!"));
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
            return new String[] { "Country-ID", "Name" };
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.city.add";
        }
    }

    public static class CMD_Setup_City_Remove extends SubCommand {
        public CMD_Setup_City_Remove(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length > 1 && AlpsUtils.TryParseInt(args[1]) != null) {
                // Check if City Project exists
                try {
                    if (CityProject.getCityProjects(false).stream().anyMatch(c -> c.getID() == Integer.parseInt(args[1]))) {
                        CityProject.removeCityProject(Integer.parseInt(args[1]));
                        sender.sendMessage(Utils.ChatUtils.getInfoMessageFormat("Successfully removed City Project with ID " + args[1] + "!"));
                    } else {
                        sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("Could not find any City Project with ID " + args[1] + "!"));
                        sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("Type </pss city list> to see all City Projects!"));
                    }
                    return;
                } catch (SQLException ex) {
                    sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("An error occurred while executing command!"));
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                    return;
                }
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
            return new String[] { "City-ID" };
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.city.remove";
        }
    }

    public static class CMD_Setup_City_SetName extends SubCommand {
        public CMD_Setup_City_SetName(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length > 2 && AlpsUtils.TryParseInt(args[1]) != null) {
                // Check if City Project exits
                try {
                    if (CityProject.getCityProjects(false).stream().anyMatch(c -> c.getID() == Integer.parseInt(args[1]))) {
                        String name = CMD_Setup.appendArgs(args,2);
                        if (name.length() <= 45) {
                            CityProject.setCityProjectName(Integer.parseInt(args[1]), name);
                            sender.sendMessage(Utils.ChatUtils.getInfoMessageFormat("Successfully changed name of City Project with ID " + args[1] + " to '" + name + "'!"));
                        } else {
                            sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("City Project name cannot be longer than 45 characters!"));
                        }
                        return;
                    }
                } catch (SQLException ex) {
                    sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("An error occurred while executing command!"));
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                    return;
                }
            }
            sendInfo(sender);
        }

        @Override
        public String[] getNames() {
            return new String[] { "setname" };
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[] { "City-ID", "Name" };
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.city.setname";
        }
    }

    public static class CMD_Setup_City_SetDescription extends SubCommand {
        public CMD_Setup_City_SetDescription(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length > 2 && AlpsUtils.TryParseInt(args[1]) != null) {
                // Check if City Project exits
                try {
                    if (CityProject.getCityProjects(false).stream().anyMatch(c -> c.getID() == Integer.parseInt(args[1]))) {
                        String description = CMD_Setup.appendArgs(args,2);
                        if (description.length() <= 255) {
                            CityProject.setCityProjectDescription(Integer.parseInt(args[1]), description);
                            sender.sendMessage(Utils.ChatUtils.getInfoMessageFormat("Successfully set description of City Project with ID " + args[1] + " to '" + description + "'!"));
                        } else {
                            sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("City Project description cant be longer than 255 characters!"));
                        }
                        return;
                    }
                } catch (SQLException ex) {
                    sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("An error occurred while executing command!"));
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                    return;
                }
            }
            sendInfo(sender);
        }

        @Override
        public String[] getNames() {
            return new String[] { "setdescription" };
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[] { "City-ID", "Description" };
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.city.setdescription";
        }
    }

    public static class CMD_Setup_City_SetVisible extends SubCommand {
        public CMD_Setup_City_SetVisible(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length > 2 && AlpsUtils.TryParseInt(args[1]) != null) {
                // Check if City Project exits
                try {
                    if (CityProject.getCityProjects(false).stream().anyMatch(c -> c.getID() == Integer.parseInt(args[1]))) {
                        if (args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("false")) {
                            CityProject.setCityProjectVisibility(Integer.parseInt(args[1]), args[2].equalsIgnoreCase("true"));
                            sender.sendMessage(Utils.ChatUtils.getInfoMessageFormat("Successfully set visibility of City Project with ID " + args[1] + " to " + args[2].toUpperCase() + "!"));
                            return;
                        }
                    }
                } catch (SQLException ex) {
                    sender.sendMessage(Utils.ChatUtils.getErrorMessageFormat("An error occurred while executing command!"));
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                    return;
                }
            }
            sendInfo(sender);
        }

        @Override
        public String[] getNames() {
            return new String[] { "setvisible" };
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[] { "City-ID", "True/False" };
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.city.setvisible";
        }
    }
}
