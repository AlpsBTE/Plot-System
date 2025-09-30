package com.alpsbte.plotsystem.commands;

import com.alpsbte.plotsystem.core.menus.companion.CompanionMenu;
import com.alpsbte.plotsystem.core.menus.tutorial.TutorialStagesMenu;
import com.alpsbte.plotsystem.core.menus.tutorial.TutorialsMenu;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.Tutorial;
import com.alpsbte.plotsystem.core.system.tutorial.TutorialCategory;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CMD_Companion extends BaseCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.PLAYER_HAS_NO_PERMISSIONS)));
            return true;
        }

        Player player = getPlayer(sender);
        if (player == null) return true;

        Tutorial tutorial = AbstractTutorial.getActiveTutorial(player.getUniqueId());
        if (tutorial != null) {
            new TutorialStagesMenu(player, tutorial.getId());
        } else if (TutorialPlot.isRequiredAndInProgress(TutorialCategory.BEGINNER.getId(), player.getUniqueId()) && player.hasPermission("plotsystem.tutorial")) {
            new TutorialsMenu(player);
        } else CompanionMenu.open((Player) sender);

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
