package com.alpsbte.plotsystem.commands.plot;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.commands.SubCommand;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotHandler;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CMD_Plot_Next extends SubCommand {

    public CMD_Plot_Next(BaseCommand baseCommand) {
        super(baseCommand);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player player = getPlayer(sender);
        if (player == null) {
            sender.sendMessage(Component.text("This command can only be used as a player!", NamedTextColor.RED));
            return;
        }

        CompletableFuture.runAsync(() -> {
            if (!DataProvider.BUILD_TEAM.isAnyReviewer(player.getUniqueId()) && !sender.hasPermission("plotsystem.admin")) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(langUtil.get(sender, LangPaths.Message.Error.PLAYER_HAS_NO_PERMISSIONS)));
                return;
            }

            Builder builder = Builder.byUUID(player.getUniqueId());
            if (builder == null) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(langUtil.get(sender, LangPaths.Message.Error.ERROR_OCCURRED)));
                return;
            }

            List<CityProject> cityProjects = DataProvider.BUILD_TEAM.getReviewerCities(player.getUniqueId());
            List<Plot> plots = DataProvider.PLOT.getReviewBookPlots(cityProjects).stream()
                    .filter(plot -> plot.getVersion() > AbstractPlot.LEGACY_VERSION_THRESHOLD)
                    .toList();

            if (plots.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(langUtil.get(sender, LangPaths.Message.Error.NO_OPEN_PLOTS)));
                return;
            }

            Plot target = resolveNextPlot(builder, plots);
            if (target == null) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(langUtil.get(sender, LangPaths.Message.Error.NO_OTHER_OPEN_PLOTS)));
                return;
            }

            PlotWorld world = PlotHandler.visitWorld(target);
            if (!PlotHandler.prepareVisit(target)) {
                player.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(player, LangPaths.Message.Error.ERROR_OCCURRED)));
                return;
            }

            Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
                player.sendMessage(Utils.ChatUtils.getInfoFormat(langUtil.get(player, LangPaths.Message.Info.TELEPORTING_PLOT, String.valueOf(target.getId()))));
                world.teleportPlayer(player);
            });
        });
    }

    /**
     * From the lobby (or any non-listed plot): first plot in the review book.
     * From a listed plot: the following one. Does not wrap when already on the last plot.
     */
    private static Plot resolveNextPlot(Builder builder, List<Plot> plots) {
        AbstractPlot current = PlotUtils.getCurrentPlot(builder, Status.unreviewed, Status.unfinished);
        if (!(current instanceof Plot currentPlot)) {
            return plots.getFirst();
        }

        int currentIndex = -1;
        for (int i = 0; i < plots.size(); i++) {
            if (plots.get(i).getId() == currentPlot.getId()) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex < 0) {
            return plots.getFirst();
        }
        if (currentIndex + 1 >= plots.size()) {
            return null;
        }
        return plots.get(currentIndex + 1);
    }

    @Override
    public String[] getNames() {
        return new String[]{"next"};
    }

    @Override
    public String getDescription() {
        return "Teleport to the next open plot in the review book.";
    }

    @Override
    public String[] getParameter() {
        return new String[0];
    }

    @Override
    public String getPermission() {
        return "plotsystem.plot.next";
    }
}
