package com.alpsbte.plotsystem.commands.review;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.review.PlotReview;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class CMD_UndoReview extends BaseCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        Player player = getPlayer(sender);
        if (player == null) {
            sender.sendMessage(Component.text("This command can only be used as a player!", RED));
            return true;
        }

        CompletableFuture.runAsync(() -> {
            if (!DataProvider.BUILD_TEAM.isAnyReviewer(player.getUniqueId()) && !sender.hasPermission("plotsystem.admin")) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.PLAYER_HAS_NO_PERMISSIONS)));
                return;
            }

            if (args.length < 1 || AlpsUtils.tryParseInt(args[0]) == null) {
                sendInfo(sender);
                return;
            }

            Plot plot = DataProvider.PLOT.getPlotById(Integer.parseInt(args[0]));
            if (plot == null || plot.getStatus() != Status.completed) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender,
                        LangPaths.Message.Error.PLOT_DOES_NOT_EXIST)));
                return;
            }

            if (plot.getVersion() <= AbstractPlot.LEGACY_VERSION_THRESHOLD) {
                player.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.CANNOT_MODIFY_LEGACY_PLOT)));
                return;
            }

            Builder builder = DataProvider.BUILDER.getBuilderByUUID(player.getUniqueId());
            if (DataProvider.BUILDER.canNotReviewPlot(builder.getUUID(), plot) && !sender.hasPermission("plotsystem.admin")) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.PLAYER_HAS_NO_PERMISSIONS)));
                return;
            }

            // Players cannot review their own plots
            boolean isParticipant = plot.getPlotOwner().getUUID() == player.getUniqueId() || plot.getPlotMembers().stream().anyMatch(b -> b.getUUID() == player.getUniqueId());
            if (!PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.DEV_MODE) && isParticipant) {
                player.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(player, LangPaths.Message.Error.CANNOT_REVIEW_OWN_PLOT)));
                return;
            }

            Optional<PlotReview> review = plot.getLatestReview();
            if (review.isEmpty()) {
                player.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(player, LangPaths.Message.Error.REVIEW_NOT_FOUND)));
                return;
            }

            boolean successful = review.get().undoReview();
            if (successful) player.sendMessage(Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(player, LangPaths.Message.Info.UNDID_REVIEW, plot.getId() + "", plot.getPlotOwner().getName())));
            else player.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(player, LangPaths.Message.Error.ERROR_OCCURRED)));
        });
        return true;
    }

    @Override
    public String[] getNames() {
        return new String[]{"undoReview"};
    }

    @Override
    public String getDescription() {
        return "Undo a review of a plot.";
    }

    @Override
    public String[] getParameter() {
        return new String[]{"ID"};
    }

    @Override
    public String getPermission() {
        return "plotsystem.review.undoreview";
    }
}
