package com.alpsbte.plotsystem.commands.review;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.review.PlotReview;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class CMD_EditFeedback extends BaseCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        Player player = getPlayer(sender);
        if (player == null) {
            Bukkit.getConsoleSender().sendMessage(text("This command can only be used as a player!", RED));
            return true;
        }

        CompletableFuture.runAsync(() -> {
            if (!DataProvider.BUILD_TEAM.isAnyReviewer(player.getUniqueId()) && !sender.hasPermission("plotsystem.admin")) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.PLAYER_HAS_NO_PERMISSIONS)));
                return;
            }

            if (args.length < 1) {
                sendInfo(sender);
                return;
            }

            Integer plotId = AlpsUtils.tryParseInt(args[0]);

            if (plotId == null) {
                sendInfo(sender);
                return;
            }

            Plot plot = DataProvider.PLOT.getPlotById(plotId);

            if (plot.getVersion() <= AbstractPlot.LEGACY_VERSION_THRESHOLD) {
                player.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.CANNOT_MODIFY_LEGACY_PLOT)));
                return;
            }

            Optional<PlotReview> review = plot.getLatestReview();
            if (review.isEmpty()) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.PLOT_EITHER_UNCLAIMED_OR_UNREVIEWED)));
                return;
            }

            Builder builder = DataProvider.BUILDER.getBuilderByUUID(player.getUniqueId());
            if (DataProvider.BUILDER.canNotReviewPlot(builder.getUUID(), plot) && !sender.hasPermission("plotsystem.admin")) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.CANNOT_SEND_FEEDBACK)));
                return;
            }

            StringBuilder feedback = new StringBuilder();
            for (int i = 2; i <= args.length; i++) {
                feedback.append(args.length == 2 ? "" : " ").append(args[i - 1]);
            }

            boolean successful = review.get().updateFeedback(feedback.toString());
            if (successful) sender.sendMessage(Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Info.UPDATED_PLOT_FEEDBACK, plot.getId() + "")));
            else sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.ERROR_OCCURRED)));
        });
        return true;
    }

    @Override
    public String[] getNames() {
        return new String[]{"editfeedback"};
    }

    @Override
    public String getDescription() {
        return "Updates the feedback of a plot.";
    }

    @Override
    public String[] getParameter() {
        return new String[]{"ID", "Feedback"};
    }

    @Override
    public String getPermission() {
        return "plotsystem.review.editfeedback";
    }
}
