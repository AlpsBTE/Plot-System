
package com.alpsbte.plotsystem.commands;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.menus.tutorial.TutorialStagesMenu;
import com.alpsbte.plotsystem.core.menus.tutorial.TutorialsMenu;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.Tutorial;
import com.alpsbte.plotsystem.core.system.tutorial.TutorialCategory;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CMD_Tutorial extends BaseCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.PLAYER_HAS_NO_PERMISSIONS)));
            return true;
        }
        if (getPlayer(sender) == null) {
            Bukkit.getConsoleSender().sendMessage(Component.text("This command can only be used as a player!", NamedTextColor.RED));
            return true;
        }
        if (!PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.TUTORIAL_ENABLE)) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.TUTORIAL_DISABLED)));
            return true;
        }

        if (args.length == 0) {
            Tutorial tutorial = AbstractTutorial.getActiveTutorial(getPlayer(sender).getUniqueId());
            if (tutorial != null) {
                new TutorialStagesMenu(tutorial.getPlayer(), tutorial.getId());
            } else {
                new TutorialsMenu(getPlayer(sender));
            }
        } else if (args.length == 1 && AlpsUtils.tryParseInt(args[0]) != null) {
            int tutorialId = Integer.parseInt(args[0]);
            if (TutorialCategory.byId(tutorialId) == null) return true;
            AbstractTutorial.loadTutorial(getPlayer(sender), tutorialId);
        }
        return true;
    }

    @Override
    public String[] getNames() {
        return new String[]{"tutorial"};
    }

    @Override
    public String getDescription() {
        return "Start and manage your tutorials.";
    }

    @Override
    public String[] getParameter() {
        return new String[0];
    }

    @Override
    public String getPermission() {
        return "plotsystem.tutorial";
    }
}
