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
import com.alpsbte.plotsystem.core.system.BuildTeam;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.List;
import java.util.StringJoiner;

import static net.kyori.adventure.text.Component.text;

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
        registerSubCommand(new CMD_Setup_BuildTeam_AddCountry(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_BuildTeam_RemoveCountry(getBaseCommand(), this));
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
            List<BuildTeam> buildTeams = BuildTeam.getBuildTeams();
            if (buildTeams.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getInfoFormat("There are currently no build teams registered in the database!"));
                return;
            }

            sender.sendMessage(Utils.ChatUtils.getInfoFormat("There are currently " + buildTeams.size() + " build teams registered in the database:"));
            sender.sendMessage("§8--------------------------");
            for (BuildTeam b : buildTeams) {
                try {
                    StringJoiner countriesAsString = new StringJoiner(", ");
                    StringJoiner reviewersAsString = new StringJoiner(", ");
                    b.getCountries().forEach(c -> countriesAsString.add(String.valueOf(c.getID())));
                    b.getReviewers().forEach(r -> {try {reviewersAsString.add(r.getName());} catch (SQLException ex) {PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);}});
                    sender.sendMessage(" §6> §b" + b.getID() + " (" + b.getName() + ") §f- Country IDs: " + (countriesAsString.length() == 0 ? "No Countries" : countriesAsString) + " - Reviewers: " + (reviewersAsString.length() == 0 ? "No Reviewers" : reviewersAsString));
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
            if (args[1].length() > 45) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Build team name cannot be longer than 45 characters!"));
                return;
            }

            try {
                String name = CMD_Setup.appendArgs(args, 1);
                BuildTeam.addBuildTeam(name);
                sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully added build team with name '" + name + "'!"));
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

            // Check if build team exists
            try {
                if (BuildTeam.getBuildTeams().stream().noneMatch(b -> b.getID() == Integer.parseInt(args[1]))) {
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat("Could not find any build team with ID " + args[1] + "!"));
                    sendInfo(sender);
                    return;
                }
                BuildTeam.removeBuildTeam(Integer.parseInt(args[1]));
                sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully removed build team with ID " + args[1] + "!"));
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

            // Check if build team exits
            try {
                if (BuildTeam.getBuildTeams().stream().noneMatch(b -> b.getID() == Integer.parseInt(args[1]))) return;
                String name = CMD_Setup.appendArgs(args, 2);
                if (name.length() > 45) {
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat("Build team name cannot be longer than 45 characters!"));
                    return;
                }

                BuildTeam.setBuildTeamName(Integer.parseInt(args[1]), name);
                sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully changed name of build team with ID " + args[1] + " to '" + name + "'!"));
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
            return new String[]{"BuildTeam-ID", "Name"};
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.buildteam.setname";
        }
    }

    public static class CMD_Setup_BuildTeam_AddCountry extends SubCommand {
        public CMD_Setup_BuildTeam_AddCountry(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 2 || AlpsUtils.tryParseInt(args[1]) == null || AlpsUtils.tryParseInt(args[2]) == null) {
                sendInfo(sender);
                return;
            }

            // Check if build team and country exists
            try {
                if (BuildTeam.getBuildTeams().stream().noneMatch(b -> b.getID() == Integer.parseInt(args[1]))) return;
                if (Country.getCountries().stream().noneMatch(c -> c.getID() == Integer.parseInt(args[2]))) {
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat("Country could not be found or is already added to the build team!"));
                    return;
                }
                Country country = new Country(Integer.parseInt(args[2]));
                BuildTeam.addCountry(Integer.parseInt(args[1]), country.getID());
                sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully added country '" + country.getName() + "' to build team with ID " + args[1] + "!"));
            } catch (SQLException ex) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
                PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
            }
        }

        @Override
        public String[] getNames() {
            return new String[]{"addcountry"};
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[]{"BuildTeam-ID", "Country-ID"};
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.buildteam.addcountry";
        }
    }

    public static class CMD_Setup_BuildTeam_RemoveCountry extends SubCommand {
        public CMD_Setup_BuildTeam_RemoveCountry(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 2 || AlpsUtils.tryParseInt(args[1]) == null || AlpsUtils.tryParseInt(args[2]) == null) {
                sendInfo(sender);
                return;
            }

            // Check if build team and country exists
            try {
                if (BuildTeam.getBuildTeams().stream().noneMatch(b -> b.getID() == Integer.parseInt(args[1]))) return;
                if (Country.getCountries().stream().noneMatch(c -> c.getID() == Integer.parseInt(args[2]))) {
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat("Country could not be found or is not added to the build team!"));
                    return;
                }
                Country country = new Country(Integer.parseInt(args[2]));
                BuildTeam.removeCountry(Integer.parseInt(args[1]), country.getID());
                sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully removed country '" + country.getName() + "' from build team with ID " + args[1] + "!"));
            } catch (SQLException ex) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
                PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
            }
        }

        @Override
        public String[] getNames() {
            return new String[]{"removecountry"};
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[]{"BuildTeam-ID", "Country-ID"};
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.buildteam.removecountry";
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
            try {
                if (BuildTeam.getBuildTeams().stream().noneMatch(b -> b.getID() == Integer.parseInt(args[1]))) return;
                Builder builder = Builder.getBuilderByName(args[2]);
                if (builder == null || BuildTeam.getBuildTeamsByReviewer(builder.getUUID()).stream().anyMatch(b -> b.getID() == Integer.parseInt(args[1]))) {
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat("Player could not be found or is already reviewer for this build team!"));
                    return;
                }
                BuildTeam.addReviewer(Integer.parseInt(args[1]), builder.getUUID().toString());
                sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully added '" + builder.getName() + "' as reviewer to build team with ID " + args[1] + "!"));
            } catch (SQLException ex) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
                PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
            }
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
            try {
                if (BuildTeam.getBuildTeams().stream().noneMatch(b -> b.getID() == Integer.parseInt(args[1]))) return;

                Builder builder = Builder.getBuilderByName(args[2]);
                if (builder == null || BuildTeam.getBuildTeamsByReviewer(builder.getUUID()).stream().noneMatch(b -> b.getID() == Integer.parseInt(args[1]))) {
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat("Player could not be found or is not a reviewer for this build team!"));
                    return;
                }
                BuildTeam.removeReviewer(Integer.parseInt(args[1]), builder.getUUID().toString());
                sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully removed '" + builder.getName() + "' as reviewer from build team with ID " + args[1] + "!"));
            } catch (SQLException ex) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
                PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
            }
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
