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

package com.alpsbte.plotsystem.commands.admin;

import com.alpsbte.alpslib.hologram.DecentHologramDisplay;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.holograms.HologramConfiguration;
import com.alpsbte.plotsystem.core.holograms.HologramRegister;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.text;

public class CMD_PReload extends BaseCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat("You don't have permission to use this command!"));
            return true;
        }

        try {
            PlotSystem.getPlugin().reloadConfig();
            sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully reloaded config!"));

            DecentHologramDisplay.activeDisplays.forEach(leaderboard -> leaderboard.setLocation(HologramRegister
                    .getLocation((HologramConfiguration) leaderboard)));
            HologramRegister.reload();
            sender.sendMessage(Utils.ChatUtils.getInfoFormat("Successfully reloaded leaderboards!"));

            DatabaseConnection.InitializeDatabase();
        } catch (Exception ex) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
            PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
        }
        return true;
    }

    @Override
    public String[] getNames() {
        return new String[]{"preload"};
    }

    @Override
    public String getDescription() {
        return "Reloads configuration files and leaderboards.";
    }

    @Override
    public String[] getParameter() {
        return new String[0];
    }

    @Override
    public String getPermission() {
        return "plotsystem.admin.preload";
    }
}