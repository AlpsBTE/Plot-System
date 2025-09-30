package com.alpsbte.plotsystem.commands.admin.setup;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.commands.SubCommand;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Difficulty;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

public class CMD_Setup_Difficulty extends SubCommand {

    public CMD_Setup_Difficulty(BaseCommand baseCommand) {
        super(baseCommand);
        register();
    }

    private void register() {
        registerSubCommand(new CMD_Setup_Difficulty_List(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_Difficulty_SetMultiplier(getBaseCommand(), this));
        registerSubCommand(new CMD_Setup_Difficulty_SetRequirement(getBaseCommand(), this));
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        sendInfo(sender);
    }

    @Override
    public String[] getNames() {
        return new String[]{"difficulty"};
    }

    @Override
    public String getDescription() {
        return "Configure plot difficulties";
    }

    @Override
    public String[] getParameter() {
        return new String[0];
    }

    @Override
    public String getPermission() {
        return "plotsystem.admin.pss.difficulty";
    }


    public static class CMD_Setup_Difficulty_List extends SubCommand {
        public CMD_Setup_Difficulty_List(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            List<Difficulty> difficulties = DataProvider.DIFFICULTY.getDifficulties();
            sender.sendMessage(text("--------------------------", DARK_GRAY));
            for (Difficulty d : difficulties) {
                sender.sendMessage(text(" » ", DARK_GRAY)
                        .append(text(d.getID(), AQUA))
                        .append(text("Multiplier: " + d.getMultiplier(), WHITE))
                        .append(text(" - Score Requirement: " + d.getScoreRequirement(), WHITE)));
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
            return "plotsystem.admin.pss.difficulty.list";
        }
    }

    public static class CMD_Setup_Difficulty_SetMultiplier extends SubCommand {
        public CMD_Setup_Difficulty_SetMultiplier(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 2 || AlpsUtils.tryParseDouble(args[2]) == null) {
                sendInfo(sender);
                return;
            }

            // Check if difficulty exists
            Optional<Difficulty> difficulty = DataProvider.DIFFICULTY.getDifficultyById(args[1]);
            if (difficulty.isEmpty()) return;

            boolean successful = difficulty.get().setMultiplier(Double.parseDouble(args[2]));
            if (successful) sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully set multiplier of Difficulty with ID " + args[1] + " to " + args[2] + "!"));
            else sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
        }

        @Override
        public String[] getNames() {
            return new String[]{"setmultiplier"};
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[]{"Difficulty-ID", "Multiplier"};
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.difficulty.setmultiplier";
        }
    }

    public static class CMD_Setup_Difficulty_SetRequirement extends SubCommand {
        public CMD_Setup_Difficulty_SetRequirement(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 2 || AlpsUtils.tryParseInt(args[2]) == null) {
                sendInfo(sender);
                return;
            }

            // Check if difficulty exists
            Optional<Difficulty> difficulty = DataProvider.DIFFICULTY.getDifficultyById(args[1]);
            if (difficulty.isEmpty()) return;

            boolean successful = difficulty.get().setScoreRequirement(Integer.parseInt(args[2]));
            if (successful) sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully set score requirement of Difficulty with ID " + args[1] + " to " + args[2] + "!"));
            else sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
        }

        @Override
        public String[] getNames() {
            return new String[]{"setrequirement"};
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[]{"Difficulty-ID", "Score Requirement"};
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.difficulty.setrequirement";
        }
    }
}
