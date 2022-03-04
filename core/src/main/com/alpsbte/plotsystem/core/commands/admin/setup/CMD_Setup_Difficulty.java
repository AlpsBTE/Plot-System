package com.alpsbte.plotsystem.core.commands.admin.setup;

import com.alpsbte.plotsystem.core.commands.BaseCommand;
import com.alpsbte.plotsystem.core.commands.SubCommand;
import com.alpsbte.plotsystem.core.system.Difficulty;
import com.alpsbte.plotsystem.core.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

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
        return new String[] { "difficulty" };
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
            List<Difficulty> difficulties = Difficulty.getDifficulties();
            sender.sendMessage(Utils.getInfoMessageFormat("There are currently " + difficulties.size() + " Difficulties registered in the database:"));
            sender.sendMessage("§8--------------------------");
            for (Difficulty d : difficulties) {
                sender.sendMessage(" §6> §b" + d.getID() + " (" + d.getDifficulty().name() + ") §f- Multiplier: " + d.getMultiplier() + " - Score Requirement: " + d.getScoreRequirement());
            }
            sender.sendMessage("§8--------------------------");
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
            return "plotsystem.admin.pss.difficulty.list";
        }
    }

    public static class CMD_Setup_Difficulty_SetMultiplier extends SubCommand {
        public CMD_Setup_Difficulty_SetMultiplier(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length > 2 && Utils.TryParseInt(args[1]) != null && Utils.tryParseDouble(args[2]) != null) {
                // Check if difficulty exists
                try {
                    if (Difficulty.getDifficulties().stream().anyMatch(c -> c.getID() == Integer.parseInt(args[1]))) {
                        Difficulty.setMultiplier(Integer.parseInt(args[1]), Double.parseDouble(args[2]));
                        sender.sendMessage(Utils.getInfoMessageFormat("Successfully set multiplier of Difficulty with ID " + args[1] + " to " + args[2] + "!"));
                        return;
                    }
                } catch (SQLException ex) {
                    sender.sendMessage(Utils.getErrorMessageFormat("An error occurred while executing command!"));
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                    return;
                }
            }
            sendInfo(sender);
        }

        @Override
        public String[] getNames() {
            return new String[] { "setmultiplier" };
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[] { "Difficulty-ID", "Multiplier" };
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
            if (args.length > 2 && Utils.TryParseInt(args[1]) != null && Utils.TryParseInt(args[2]) != null) {
                // Check if difficulty exists
                try {
                    if (Difficulty.getDifficulties().stream().anyMatch(c -> c.getID() == Integer.parseInt(args[1]))) {
                        Difficulty.setScoreRequirement(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                        sender.sendMessage(Utils.getInfoMessageFormat("Successfully set score requirement of Difficulty with ID " + args[1] + " to " + args[2] + "!"));
                        return;
                    }
                } catch (SQLException ex) {
                    sender.sendMessage(Utils.getErrorMessageFormat("An error occurred while executing command!"));
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                    return;
                }
            }
            sendInfo(sender);
        }

        @Override
        public String[] getNames() {
            return new String[] { "setrequirement" };
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String[] getParameter() {
            return new String[] { "Difficulty-ID", "Score Requirement" };
        }

        @Override
        public String getPermission() {
            return "plotsystem.admin.pss.difficulty.setrequirement";
        }
    }
}
