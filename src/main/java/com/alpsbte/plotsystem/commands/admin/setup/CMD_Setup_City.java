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
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.commands.SubCommand;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.List;

import static net.kyori.adventure.text.Component.text;

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
        return new String[]{"city"};
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
            if (cities.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getInfoFormat("There are currently no City Projects registered in the database!"));
                return;
            }

            sender.sendMessage(Utils.ChatUtils.getInfoFormat("There are currently " + cities.size() + " City Projects registered in the database:"));
            sender.sendMessage("§8--------------------------");
            for (CityProject c : cities) {
                try {
                    sender.sendMessage(" §6> §b" + c.getID() + " (" + c.getName() + ") §f- Description: " + c.getDescription() + " - Country: " + c.getCountry().getName() + " - Visible: " + c.isVisible());
                } catch (SQLException ex) {
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
                    PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
                }
            }
            sender.sendMessage("§8--------------------------");
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
            return "plotsystem.admin.pss.city.list";
        }
    }

    public static class CMD_Setup_City_Add extends SubCommand {
        public CMD_Setup_City_Add(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 2 || AlpsUtils.tryParseInt(args[1]) == null) {sendInfo(sender); return;}

            Country country = Country.getCountries().stream().filter(c -> c.getID() == Integer.parseInt(args[1])).findFirst().orElse(null);
            if (country == null) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Could not find any country with ID " + args[1] + "!"));
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Type </pss country list> to see all countries!"));
                return;
            }
            String name = CMD_Setup.appendArgs(args, 2);
            if (name.length() > 45) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("City Project name cannot be longer than 45 characters!"));
                return;
            }

            try {
                CityProject.addCityProject(country, name);
                sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully added City Project with name '" + name + "' in country with the ID " + args[1] + "!"));
            } catch (SQLException ex) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
                PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
            }
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
            return new String[]{"Country-ID", "Name"};
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
            if (args.length <= 1 || AlpsUtils.tryParseInt(args[1]) == null) {sendInfo(sender); return;}

            // Check if City Project exists
            try {
                if (CityProject.getCityProjects(false).stream().noneMatch(c -> c.getID() == Integer.parseInt(args[1]))) {
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat("Could not find any City Project with ID " + args[1] + "!"));
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat("Type </pss city list> to see all City Projects!"));
                    return;
                }
                CityProject.removeCityProject(Integer.parseInt(args[1]));
                sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully removed City Project with ID " + args[1] + "!"));
            } catch (SQLException ex) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
                PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
            }
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
            return new String[]{"City-ID"};
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
            if (args.length <= 2 || AlpsUtils.tryParseInt(args[1]) == null) {sendInfo(sender); return;}

            // Check if City Project exits
            try {
                if (CityProject.getCityProjects(false).stream().noneMatch(c -> c.getID() == Integer.parseInt(args[1]))) return;

                String name = CMD_Setup.appendArgs(args, 2);
                if (name.length() > 45) {
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat("City Project name cannot be longer than 45 characters!"));
                    return;
                }

                CityProject.setCityProjectName(Integer.parseInt(args[1]), name);
                sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully changed name of City Project with ID " + args[1] + " to '" + name + "'!"));

            } catch (SQLException ex) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
                PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
            }
        }

        @Override
        public String[] getNames() {
            return new String[]{"setname"};
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[]{"City-ID", "Name"};
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
            if (args.length <= 2 || AlpsUtils.tryParseInt(args[1]) == null) {sendInfo(sender); return;}

            // Check if City Project exits
            try {
                if (CityProject.getCityProjects(false).stream().noneMatch(c -> c.getID() == Integer.parseInt(args[1]))) return;

                String description = CMD_Setup.appendArgs(args, 2);
                if (description.length() > 255) {
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat("City Project description cant be longer than 255 characters!"));
                    return;
                }
                CityProject.setCityProjectDescription(Integer.parseInt(args[1]), description);
                sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully set description of City Project with ID " + args[1] + " to '" + description + "'!"));
            } catch (SQLException ex) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
                PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
            }
        }

        @Override
        public String[] getNames() {
            return new String[]{"setdescription"};
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[]{"City-ID", "Description"};
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
            if (args.length <= 2 || AlpsUtils.tryParseInt(args[1]) == null) {sendInfo(sender); return;}

            // Check if City Project exits
            try {
                if (CityProject.getCityProjects(false).stream().noneMatch(c -> c.getID() == Integer.parseInt(args[1]))) return;
                if (!args[2].equalsIgnoreCase("true") && !args[2].equalsIgnoreCase("false")) return;

                CityProject.setCityProjectVisibility(Integer.parseInt(args[1]), args[2].equalsIgnoreCase("true"));
                sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully set visibility of City Project with ID " + args[1] + " to " + args[2].toUpperCase() + "!"));
            } catch (SQLException ex) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
                PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
            }
        }

        @Override
        public String[] getNames() {
            return new String[]{"setvisible"};
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[]{"City-ID", "True/False"};
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.city.setvisible";
        }
    }
}
