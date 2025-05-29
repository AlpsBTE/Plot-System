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

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.commands.SubCommand;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class CMD_Setup_City extends SubCommand {

    public CMD_Setup_City(BaseCommand baseCommand) {
        super(baseCommand);
        register();
    }

    private void register() {
        registerSubCommand(new CMD_Setup_City_List(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_City_Add(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_City_Remove(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_City_SetServer(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_City_SetBuildTeam(getBaseCommand(), this));
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
            List<CityProject> cities = DataProvider.CITY_PROJECT.get(false);
            if (cities.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getInfoFormat("There are currently no City Projects registered in the database!"));
                return;
            }

            sender.sendMessage(Utils.ChatUtils.getInfoFormat("There are currently " + cities.size() + " City Projects registered in the database:"));
            sender.sendMessage(text("--------------------------", DARK_GRAY));
            for (CityProject c : cities) {
                sender.sendMessage(text(" » ", DARK_GRAY)
                        .append(text(c.getID(), AQUA))
                        .append(text(" - Country: " + c.getCountry().getCode()
                                + " - Server: " + c.getServerName()
                                + " - Build Team: " + c.getBuildTeam().getName()
                                + " - Visible: " + c.isVisible(), WHITE)));
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
            return "plotsystem.admin.pss.city.list";
        }
    }

    public static class CMD_Setup_City_Add extends SubCommand {
        public CMD_Setup_City_Add(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 4) {sendInfo(sender); return;}

            String cityProjectId = args[1];
            String countryCode = args[2];
            Optional<Country> country = DataProvider.COUNTRY.getCountryByCode(countryCode);
            if (country.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Could not find any country with code " + countryCode + "!"));
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Type </pss country list> to see all countries!"));
                return;
            }
            String serverName = args[3];
            if (serverName.length() > 255) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Server name cannot be longer than 255 characters!"));
                return;
            }
            // Check if server exists
            if (!DataProvider.SERVER.serverExists(serverName)) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Could not find any server with Name " + serverName + "!"));
                sendInfo(sender);
                return;
            }

            Integer buildTeamId = AlpsUtils.tryParseInt(args[4]);
            if (buildTeamId == null) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Build Team ID must be a number!"));
                sendInfo(sender);
                return;
            }
            if (DataProvider.BUILD_TEAM.getBuildTeam(buildTeamId).isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Could not find any build team with ID " + buildTeamId + "!"));
                sendInfo(sender);
                return;
            }

            boolean added = DataProvider.CITY_PROJECT.add(cityProjectId, buildTeamId, country.get().getCode(), serverName);
            if (added) {
                try {
                    LangUtil.getInstance().languageFiles[0].set(LangPaths.Database.CITY_PROJECT + "." + cityProjectId + ".name", cityProjectId);
                    LangUtil.getInstance().languageFiles[0].set(LangPaths.Database.CITY_PROJECT + "." + cityProjectId + ".description", "");
                    LangUtil.getInstance().languageFiles[0].save(LangUtil.getInstance().languageFiles[0].getFile()); // TODO Fix ugly config file
                } catch (Exception e) {
                    PlotSystem.getPlugin().getComponentLogger().warn(text("An error occurred while saving the language file for City Project " + cityProjectId + "!").color(RED), e);
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while saving the language file for City Project " + cityProjectId + "!"));
                }
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Edit the " + LangPaths.Database.CITY_PROJECT + "." + cityProjectId + " language config setting, otherwise the name will be the ID of the City & no description will be present!"));
                sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully added City Project with Name '" + cityProjectId + "' under country with the code " + countryCode + "!"));
            }
            else sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while adding City Project!"));
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
            return new String[]{"City-Project-Name", "Country-Code", "Server-Name", "Build-Team-ID"};
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
            if (args.length <= 1) {sendInfo(sender); return;}
            String cityProjectId = args[1];

            // Check if City Project exists
            Optional<CityProject> cityProject = DataProvider.CITY_PROJECT.getById(cityProjectId);
            if (cityProject.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Could not find any City Project with ID " + cityProjectId + "!"));
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Type </pss city list> to see all City Projects!"));
                return;
            }

            boolean removed = DataProvider.CITY_PROJECT.remove(cityProjectId);
            if (removed) sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully removed City Project with ID " + cityProjectId + "!"));
            else sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while removing city project!"));
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

    public static class CMD_Setup_City_SetServer extends SubCommand {
        public CMD_Setup_City_SetServer(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 2) {sendInfo(sender); return;}

            // Check if City Project exits
            Optional<CityProject> cityProject = DataProvider.CITY_PROJECT.getById(args[1]);
            if (cityProject.isEmpty()) return;

            String serverName = args[2];
            if (serverName.length() > 255) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Server name cannot be longer than 255 characters!"));
                return;
            }
            // Check if server exists
            if (!DataProvider.SERVER.serverExists(serverName)) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Could not find any server with ID " + serverName + "!"));
                sendInfo(sender);
                return;
            }

            boolean successful = cityProject.get().setServer(serverName);
            if (successful) sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully changed server of City Project with ID " + args[1] + " to '" + serverName + "'!"));
            else sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while updating city project server!"));
        }

        @Override
        public String[] getNames() {
            return new String[]{"setserver"};
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[]{"City-ID", "Server-Name"};
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.city.setserver";
        }
    }

    public static class CMD_Setup_City_SetBuildTeam extends SubCommand {
        public CMD_Setup_City_SetBuildTeam(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 2) {sendInfo(sender); return;}

            // Check if City Project exits
            Optional<CityProject> cityProject = DataProvider.CITY_PROJECT.getById(args[1]);
            if (cityProject.isEmpty()) return;

            // Check if Build Team exists
            int buildTeamId = Integer.parseInt(args[2]);
            if (DataProvider.BUILD_TEAM.getBuildTeam(buildTeamId).isEmpty()) return;

            boolean successful = cityProject.get().setBuildTeam(buildTeamId);

            if (successful) sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully set Build Team of City Project with ID " + args[1] + " to " + buildTeamId + "!"));
            else sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while updating city project build team!"));
        }

        @Override
        public String[] getNames() {
            return new String[]{"setbuildteam"};
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[]{"City-ID", "Build-Team-ID"};
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.city.setbuildteam";
        }
    }

    public static class CMD_Setup_City_SetVisible extends SubCommand {
        public CMD_Setup_City_SetVisible(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 2) {sendInfo(sender); return;}

            // Check if City Project exits
            Optional<CityProject> cityProject = DataProvider.CITY_PROJECT.getById(args[1]);
            if (cityProject.isEmpty()) return;
            if (!args[2].equalsIgnoreCase("true") && !args[2].equalsIgnoreCase("false")) return;

            boolean isVisible = args[2].equalsIgnoreCase("true");
            boolean successful = cityProject.get().setVisible(isVisible);

            if (successful) sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully set visibility of City Project with ID " + args[1] + " to " + args[2].toUpperCase() + "!"));
            else sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while updating city project visibility!"));
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
