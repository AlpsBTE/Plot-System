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

package com.alpsbte.plotsystem.commands;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.menus.companion.CompanionMenu;
import com.alpsbte.plotsystem.core.menus.tutorial.TutorialStagesMenu;
import com.alpsbte.plotsystem.core.menus.tutorial.TutorialsMenu;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.Tutorial;
import com.alpsbte.plotsystem.core.system.tutorial.TutorialCategory;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

import static net.kyori.adventure.text.Component.text;

public class CMD_Companion extends BaseCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.PLAYER_HAS_NO_PERMISSIONS)));
            return true;
        }

        if (getPlayer(sender) == null) return true;

        try {
            FileConfiguration config = PlotSystem.getPlugin().getConfig();
            Tutorial tutorial = AbstractTutorial.getActiveTutorial(getPlayer(sender).getUniqueId());
            if (tutorial != null) {
                new TutorialStagesMenu(getPlayer(sender), tutorial.getId());
            } else if (config.getBoolean(ConfigPaths.TUTORIAL_ENABLE) && config.getBoolean(ConfigPaths.TUTORIAL_REQUIRE_BEGINNER_TUTORIAL) &&
                    !TutorialPlot.isPlotCompleted(getPlayer(sender), TutorialCategory.BEGINNER.getId()) && getPlayer(sender).hasPermission("plotsystem.tutorial")) {
                new TutorialsMenu(getPlayer(sender));
            } else CompanionMenu.open((Player) sender);
        } catch (SQLException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
        }

        return true;
    }

    @Override
    public String[] getNames() {
        return new String[]{"companion"};
    }

    @Override
    public String getDescription() {
        return "Open the Companion menu (Displays all city projects and your ongoing projects).";
    }

    @Override
    public String[] getParameter() {
        return new String[0];
    }

    @Override
    public String getPermission() {
        return "plotsystem.companion";
    }
}
