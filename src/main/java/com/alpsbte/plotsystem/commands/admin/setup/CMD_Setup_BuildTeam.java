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
import com.alpsbte.plotsystem.core.system.BuildTeam;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class CMD_Setup_BuildTeam extends SubCommand {
    public CMD_Setup_BuildTeam(BaseCommand baseCommand) {
        super(baseCommand);
        register();
    }

    private void register() {
        registerSubCommand(new CMD_Setup_BuildTeam_List(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_BuildTeam_Add(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_BuildTeam_Remove(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_BuildTeam_SetName(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_BuildTeam_AddCityProject(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_BuildTeam_RemoveCityProject(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_BuildTeam_AddReviewer(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_BuildTeam_RemoveReviewer(getBaseCommand(), this));
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        sendInfo(sender);
    }

    @Override
    public String[] getNames() {
        return new String[]{"buildteam"};
    }

    @Override
    public String getDescription() {
        return "Configure build teams";
    }

    @Override
    public String[] getParameter() {
        return new String[0];
    }

    @Override
    public String getPermission() {
        return "plotsystem.admin.pss.buildteam";
    }


    public static class CMD_Setup_BuildTeam_List extends SubCommand {
        public CMD_Setup_BuildTeam_List(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            List<BuildTeam> buildTeams = DataProvider.BUILD_TEAM.getBuildTeams();
            if (buildTeams.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getInfoFormat("There are currently no build teams registered in the database!"));
                return;
            }

            sender.sendMessage(Utils.ChatUtils.getInfoFormat("There are currently " + buildTeams.size() + " build teams registered in the database:"));
            sender.sendMessage(text("--------------------------", DARK_GRAY));
            for (BuildTeam b : buildTeams) {
                StringJoiner citiesAsString = new StringJoiner(", ");
                StringJoiner reviewersAsString = new StringJoiner(", ");
                b.getCityProjects().forEach(c -> citiesAsString.add(c.getID()));
                b.getReviewers().forEach(r -> reviewersAsString.add(r.getName()));
                sender.sendMessage(text(" » ", DARK_GRAY)
                        .append(text(b.getID() + " (" + b.getName() + ") ", AQUA))
                        .append(text("- City Project IDs: " + (citiesAsString.length() == 0 ? "No City Projects" : citiesAsString)
                                + " - Reviewers: " + (reviewersAsString.length() == 0 ? "No Reviewers" : reviewersAsString), WHITE)));
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
            return "plotsystem.admin.pss.buildteam.list";
        }
    }

    public static class CMD_Setup_BuildTeam_Add extends SubCommand {
        public CMD_Setup_BuildTeam_Add(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 1) {sendInfo(sender); return;}

            String name = args[1];
            if (name.length() > 255) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Build team name cannot be longer than 255 characters!"));
                return;
            }

            boolean successful = DataProvider.BUILD_TEAM.addBuildTeam(name);
            if (successful) sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully added build team with name '" + name + "'!"));
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
            return new String[]{"Name"};
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.buildteam.add";
        }
    }

    public static class CMD_Setup_BuildTeam_Remove extends SubCommand {
        public CMD_Setup_BuildTeam_Remove(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 1 || AlpsUtils.tryParseInt(args[1]) == null) {sendInfo(sender); return;}

            Optional<BuildTeam> buildTeam = DataProvider.BUILD_TEAM.getBuildTeam(Integer.parseInt(args[1]));

            // Check if build team exists
            if (buildTeam.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Could not find any build team with ID " + args[1] + "!"));
                sendInfo(sender);
                return;
            }

            boolean successful = DataProvider.BUILD_TEAM.removeBuildTeam(buildTeam.get().getID());
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
            return new String[]{"BuildTeam-ID"};
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.buildteam.remove";
        }
    }

    public static class CMD_Setup_BuildTeam_SetName extends SubCommand {
        public CMD_Setup_BuildTeam_SetName(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 2 || AlpsUtils.tryParseInt(args[1]) == null) {sendInfo(sender); return;}

            Optional<BuildTeam> buildTeam = DataProvider.BUILD_TEAM.getBuildTeam(Integer.parseInt(args[1]));

            // Check if build team exits
            if (buildTeam.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Build team could not be found!"));
                return;
            }

            String name = args[2];
            if (name.length() > 255) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Build team name cannot be longer than 255 characters!"));
                return;
            }

            boolean successful = buildTeam.get().setName(name);
            if (successful) sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully changed name of build team with ID " + args[1] + " to '" + name + "'!"));
            else sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
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
            return new String[]{"BuildTeam-ID", "Name"};
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.buildteam.setname";
        }
    }

    public static class CMD_Setup_BuildTeam_AddCityProject extends SubCommand {
        public CMD_Setup_BuildTeam_AddCityProject(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 2 || AlpsUtils.tryParseInt(args[1]) == null) {
                sendInfo(sender);
                return;
            }

            Optional<BuildTeam> buildTeam = DataProvider.BUILD_TEAM.getBuildTeam(Integer.parseInt(args[1]));
            Optional<CityProject> cityProject = DataProvider.CITY_PROJECT.getById(args[2]);

            if (buildTeam.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Build Team could not be found!"));
                return;
            }
            if (cityProject.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("City Project could not be found!"));
                return;
            }

            boolean successful = buildTeam.get().addCityProject(cityProject.get());
            if (successful) sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully added city project '" + cityProject.get().getID() + "' to build team with ID " + args[1] + "!"));
            else sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
        }

        @Override
        public String[] getNames() {
            return new String[]{"addcityproject"};
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[]{"BuildTeam-ID", "CityProject-ID"};
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.buildteam.addcityproject";
        }
    }

    public static class CMD_Setup_BuildTeam_RemoveCityProject extends SubCommand {
        public CMD_Setup_BuildTeam_RemoveCityProject(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 2 || AlpsUtils.tryParseInt(args[1]) == null) {
                sendInfo(sender);
                return;
            }

            Optional<BuildTeam> buildTeam = DataProvider.BUILD_TEAM.getBuildTeam(Integer.parseInt(args[1]));
            Optional<CityProject> cityProject = DataProvider.CITY_PROJECT.getById(args[2]);

            if (buildTeam.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Build Team could not be found!"));
                return;
            }
            if (cityProject.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("City project could not be found!"));
                return;
            }

            boolean successful = buildTeam.get().removeCityProject(cityProject.get().getID());
            if (successful) sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully removed city project '" + cityProject.get().getID() + "' from build team with ID " + args[1] + "!"));
            else sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
        }

        @Override
        public String[] getNames() {
            return new String[]{"removecityproject"};
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[]{"BuildTeam-ID", "CityProject-ID"};
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.buildteam.removecityproject";
        }
    }

    public static class CMD_Setup_BuildTeam_AddReviewer extends SubCommand {
        public CMD_Setup_BuildTeam_AddReviewer(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 2 || AlpsUtils.tryParseInt(args[1]) == null) {
                sendInfo(sender);
                return;
            }

            // Check if build team exits
            Optional<BuildTeam> buildTeam = DataProvider.BUILD_TEAM.getBuildTeam(Integer.parseInt(args[1]));
            if (buildTeam.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Build team could not be found!"));
                return;
            }

            Builder builder = Builder.byName(args[2]);
            if (builder == null || DataProvider.BUILD_TEAM.getBuildTeamsByReviewer(builder.getUUID()).stream().anyMatch(b -> b.getID() == buildTeam.get().getID())) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Player could not be found or is already reviewer for this build team!"));
                return;
            }
            boolean successful = buildTeam.get().addReviewer(builder);
            if (successful) sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully added '" + builder.getName() + "' as reviewer to build team with ID " + buildTeam.get().getName() + "!"));
            else sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
        }

        @Override
        public String[] getNames() {
            return new String[]{"addreviewer"};
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[]{"BuildTeam-ID", "Name"};
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.buildteam.addreviewer";
        }
    }

    public static class CMD_Setup_BuildTeam_RemoveReviewer extends SubCommand {
        public CMD_Setup_BuildTeam_RemoveReviewer(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 2 || AlpsUtils.tryParseInt(args[1]) == null) {sendInfo(sender); return;}

            // Check if build team exits
            Optional<BuildTeam> buildTeam = DataProvider.BUILD_TEAM.getBuildTeam(Integer.parseInt(args[1]));

            if (buildTeam.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Build team could not be found!"));
                return;
            }

            Builder builder = Builder.byName(args[2]);
            if (builder == null || DataProvider.BUILD_TEAM.getBuildTeamsByReviewer(builder.getUUID()).stream().noneMatch(b -> b.getID() == buildTeam.get().getID())) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Player could not be found or is not a reviewer for this build team!"));
                return;
            }

            boolean successful = buildTeam.get().removeReviewer(builder.getUUID().toString());
            if (successful) sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully removed '" + builder.getName() + "' as reviewer from build team with ID " + args[1] + "!"));
            else sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
        }

        @Override
        public String[] getNames() {
            return new String[]{"removereviewer"};
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[]{"BuildTeam-ID", "Name"};
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.buildteam.removereviewer";
        }
    }
}
