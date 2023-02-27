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

package com.alpsbte.plotsystem.commands.admin;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.holograms.HologramManager;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

public class CMD_PReload extends BaseCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (!sender.hasPermission(getPermission())){
            sender.sendMessage(Utils.getErrorMessageFormat("You don't have permission to use this command!"));
            return true;
        }

        try {
            PlotSystem.getPlugin().getConfigManager().reloadFiles();
            PlotSystem.getPlugin().getConfigManager().saveFiles();
            sender.sendMessage(Utils.getInfoMessageFormat("Successfully reloaded config!"));

            HologramManager.reloadHolograms();
            sender.sendMessage(Utils.getInfoMessageFormat("Successfully reloaded holograms!"));

            DatabaseConnection.InitializeDatabase();
        } catch (Exception ex) {
            sender.sendMessage(Utils.getErrorMessageFormat("An error occurred while executing command!"));
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
        return true;
    }

    @Override
    public String[] getNames() {
        return new String[] { "preload" };
    }

    @Override
    public String getDescription() {
        return "Reloads configuration files and holograms.";
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