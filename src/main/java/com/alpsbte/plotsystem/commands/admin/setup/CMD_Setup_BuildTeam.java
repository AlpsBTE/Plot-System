package com.alpsbte.plotsystem.commands.admin.setup;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.commands.SubCommand;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.BuildTeam;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.review.ToggleCriteria;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

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
        registerSubCommand(new CMD_Setup_BuildTeam_AddReviewer(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_BuildTeam_RemoveReviewer(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_BuildTeam_Criteria(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_BuildTeam_AssignCriteria(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_BuildTeam_RemoveCriteria(getBaseCommand(), this));
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
                b.getCityProjects().forEach(c -> citiesAsString.add(c.getId()));
                b.getReviewers().forEach(r -> reviewersAsString.add(r.getName()));
                sender.sendMessage(text(" » ", DARK_GRAY)
                        .append(text(b.getId() + " (" + b.getName() + ") ", AQUA))
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
            else sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command! Check console for any exceptions."));
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
            if (args.length <= 1) {sendInfo(sender); return;}

            Integer input = AlpsUtils.tryParseInt(args[1]);
            if (input == null) {sendInfo(sender); return;}

            Optional<BuildTeam> buildTeam = DataProvider.BUILD_TEAM.getBuildTeam(input);

            // Check if build team exists
            if (buildTeam.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Could not find any build team with ID " + args[1] + "!"));
                sendInfo(sender);
                return;
            }

            boolean successful = DataProvider.BUILD_TEAM.removeBuildTeam(buildTeam.get().getId());
            if (successful) sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully removed build team with ID " + args[1] + "!"));
            else sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command! Check console for any exceptions."));
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
            if (args.length <= 2) {sendInfo(sender); return;}

            Integer input = AlpsUtils.tryParseInt(args[1]);
            if (input == null) {sendInfo(sender); return;}

            Optional<BuildTeam> buildTeam = DataProvider.BUILD_TEAM.getBuildTeam(input);

            // Check if build team exists
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
            else sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command! Check console for any exceptions."));
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

    public static class CMD_Setup_BuildTeam_AddReviewer extends SubCommand {
        public CMD_Setup_BuildTeam_AddReviewer(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 2) {sendInfo(sender); return;}

            Integer input = AlpsUtils.tryParseInt(args[1]);
            if (input == null) {sendInfo(sender); return;}

            // Check if build team exists
            Optional<BuildTeam> buildTeam = DataProvider.BUILD_TEAM.getBuildTeam(input);
            if (buildTeam.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Build team could not be found!"));
                return;
            }

            Builder builder = Builder.byName(args[2]);
            if (builder == null || DataProvider.BUILD_TEAM.getBuildTeamsByReviewer(builder.getUUID()).stream().anyMatch(b -> b.getId() == buildTeam.get().getId())) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Player could not be found or is already reviewer for this build team!"));
                return;
            }
            boolean successful = buildTeam.get().addReviewer(builder);
            if (successful) sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully added '" + builder.getName() + "' as reviewer to build team with ID " + buildTeam.get().getName() + "!"));
            else sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command! Check console for any exceptions."));
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
            if (args.length <= 2) {sendInfo(sender); return;}

            Integer input = AlpsUtils.tryParseInt(args[1]);
            if (input == null) {sendInfo(sender); return;}

            // Check if build team exists
            Optional<BuildTeam> buildTeam = DataProvider.BUILD_TEAM.getBuildTeam(input);

            if (buildTeam.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Build team could not be found!"));
                return;
            }

            Builder builder = Builder.byName(args[2]);
            if (builder == null || DataProvider.BUILD_TEAM.getBuildTeamsByReviewer(builder.getUUID()).stream().noneMatch(b -> b.getId() == buildTeam.get().getId())) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Player could not be found or is not a reviewer for this build team!"));
                return;
            }

            boolean successful = buildTeam.get().removeReviewer(builder.getUUID().toString());
            if (successful) sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully removed '" + builder.getName() + "' as reviewer from build team with ID " + args[1] + "!"));
            else sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command! Check console for any exceptions."));
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

    public static class CMD_Setup_BuildTeam_Criteria extends SubCommand {
        public CMD_Setup_BuildTeam_Criteria(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 1) {sendInfo(sender); return;}

            Integer input = AlpsUtils.tryParseInt(args[1]);
            if (input == null) {sendInfo(sender); return;}

            List<ToggleCriteria> criteria = DataProvider.REVIEW.getBuildTeamToggleCriteria(input);
            if (criteria.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getInfoFormat("There are currently no toggle criteria assigned to the build team " + args[1] + " in the database!"));
                return;
            }

            sender.sendMessage(Utils.ChatUtils.getInfoFormat("There are currently " + criteria.size() + " toggle criteria associated to this build team:"));
            sender.sendMessage(text("--------------------------", DARK_GRAY));
            for (ToggleCriteria c : criteria) {
                sender.sendMessage(text(" » ", DARK_GRAY)
                        .append(text(c.criteriaName() + " (" + (c.isOptional() ? "optional" : "required") + ")")));
            }
            sender.sendMessage(text("--------------------------", DARK_GRAY));
        }

        @Override
        public String[] getNames() {
            return new String[]{"criteria"};
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[]{"Build-Team-ID"};
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.buildteam.criteria";
        }
    }

    public static class CMD_Setup_BuildTeam_AssignCriteria extends SubCommand {
        public CMD_Setup_BuildTeam_AssignCriteria(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 2) {sendInfo(sender); return;}

            Integer input = AlpsUtils.tryParseInt(args[1]);
            if (input == null) {sendInfo(sender); return;}

            // Check if build team exists
            Optional<BuildTeam> buildTeam = DataProvider.BUILD_TEAM.getBuildTeam(input);
            if (buildTeam.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Build team could not be found!"));
                return;
            }

            // Check if toggle criteria exists
            Optional<ToggleCriteria> criteria = DataProvider.REVIEW.getToggleCriteria(args[2]);
            if (criteria.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Toggle criteria could not be found!"));
                return;
            }

            boolean successful = DataProvider.REVIEW.assignBuildTeamToggleCriteria(buildTeam.get().getId(), criteria.get());
            if (successful) sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully assigned criteria '" + criteria.get().criteriaName() + "' to build team with ID '" + args[1] + "'!"));
            else sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command! Check console for any exceptions."));
        }

        @Override
        public String[] getNames() {
            return new String[]{"assigncriteria"};
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[]{"Build-Team-ID", "Criteria-Name"};
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.buildteam.assigncriteria";
        }
    }

    public static class CMD_Setup_BuildTeam_RemoveCriteria extends SubCommand {
        public CMD_Setup_BuildTeam_RemoveCriteria(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 2) {sendInfo(sender); return;}

            Integer input = AlpsUtils.tryParseInt(args[1]);
            if (input == null) {sendInfo(sender); return;}

            // Check if build team exists
            Optional<BuildTeam> buildTeam = DataProvider.BUILD_TEAM.getBuildTeam(input);
            if (buildTeam.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Build team could not be found!"));
                return;
            }

            // Check if toggle criteria exists
            Optional<ToggleCriteria> criteria = DataProvider.REVIEW.getBuildTeamToggleCriteria(buildTeam.get().getId())
                    .stream().filter(t -> t.criteriaName().equalsIgnoreCase(args[2])).findFirst();
            if (criteria.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Toggle criteria could not be found or is not assigned!"));
                return;
            }

            boolean successful = DataProvider.REVIEW.removeBuildTeamToggleCriteria(buildTeam.get().getId(), criteria.get());
            if (successful) sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully removed criteria '" + criteria.get().criteriaName() + "' from build team with ID '" + args[1] + "'!"));
            else sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command! Check console for any exceptions."));
        }

        @Override
        public String[] getNames() {
            return new String[]{"removecriteria"};
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[]{"Build-Team-ID", "Criteria-Name"};
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.buildteam.removecriteria";
        }
    }
}
