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

package com.alpsbte.plotsystem.commands.admin.setup;

import com.alpsbte.plotsystem.commands.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CMD_Setup extends BaseCommand {

    public CMD_Setup() {
        registerSubCommand(new CMD_Setup_BuildTeam(this));
        registerSubCommand(new CMD_Setup_FTP(this));
        registerSubCommand(new CMD_Setup_Server(this));
        registerSubCommand(new CMD_Setup_Country(this));
        registerSubCommand(new CMD_Setup_City(this));
        registerSubCommand(new CMD_Setup_Difficulty(this));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        if (args.length == 0) {
            sendInfo(sender);
        }

        return super.onCommand(sender, cmd, s, args);
    }

    @Override
    public String[] getNames() {
        return new String[]{"pss"};
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String[] getParameter() {
        return new String[0];
    }

    @Override
    public String getPermission() {
        return null;
    }

    public static String appendArgs(String[] args, int startIndex) {
        StringBuilder name = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            name.append(args[i]);
            // Add space between words
            if (i != args.length - 1) {
                name.append(" ");
            }
        }
        return name.toString();
    }
}
