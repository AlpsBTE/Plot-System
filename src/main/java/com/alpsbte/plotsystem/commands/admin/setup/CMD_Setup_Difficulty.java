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
import com.alpsbte.plotsystem.core.system.Difficulty;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.List;

import static net.kyori.adventure.text.Component.text;

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
            List<Difficulty> difficulties = Difficulty.getDifficulties();
            sender.sendMessage(Utils.ChatUtils.getInfoFormat("There are currently " + difficulties.size() + " Difficulties registered in the database:"));
            sender.sendMessage("§8--------------------------");
            for (Difficulty d : difficulties) {
                sender.sendMessage(" §6> §b" + d.getID() + " (" + d.getDifficulty().name() + ") §f- Multiplier: " + d.getMultiplier() + " - Score Requirement: " + d.getScoreRequirement());
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
            return "plotsystem.admin.pss.difficulty.list";
        }
    }

    public static class CMD_Setup_Difficulty_SetMultiplier extends SubCommand {
        public CMD_Setup_Difficulty_SetMultiplier(BaseCommand baseCommand, SubCommand subCommand) {
            super(baseCommand, subCommand);
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args.length <= 2 || AlpsUtils.tryParseInt(args[1]) == null || AlpsUtils.tryParseDouble(args[2]) == null) {
                sendInfo(sender);
                return;
            }

            // Check if difficulty exists
            try {
                if (Difficulty.getDifficulties().stream().noneMatch(c -> c.getID() == Integer.parseInt(args[1]))) return;
                Difficulty.setMultiplier(Integer.parseInt(args[1]), Double.parseDouble(args[2]));
                sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully set multiplier of Difficulty with ID " + args[1] + " to " + args[2] + "!"));
            } catch (SQLException ex) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
                PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
            }
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
            if (args.length <= 2 || AlpsUtils.tryParseInt(args[1]) == null || AlpsUtils.tryParseInt(args[2]) == null) {
                sendInfo(sender);
                return;
            }

            // Check if difficulty exists
            try {
                if (Difficulty.getDifficulties().stream().noneMatch(c -> c.getID() == Integer.parseInt(args[1]))) return;
                Difficulty.setScoreRequirement(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully set score requirement of Difficulty with ID " + args[1] + " to " + args[2] + "!"));
            } catch (SQLException ex) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
                PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
            }
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
