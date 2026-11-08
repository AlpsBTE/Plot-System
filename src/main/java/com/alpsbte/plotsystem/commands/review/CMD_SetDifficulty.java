package com.alpsbte.plotsystem.commands.review;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class CMD_SetDifficulty extends BaseCommand {

    private record DifficultyChangeRequest(Plot plot, PlotDifficulty difficulty) {}

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        Player player = getPlayer(sender);
        boolean isAdmin = sender.hasPermission("plotsystem.admin");
        CompletableFuture.runAsync(() -> handleSetDifficulty(sender, player, isAdmin, args));
        return true;
    }

    private void handleSetDifficulty(@NotNull CommandSender sender, @Nullable Player player, boolean isAdmin, String[] args) {
        if (!hasPermission(player, isAdmin)) {
            sendAlert(sender, LangPaths.Message.Error.PLAYER_HAS_NO_PERMISSIONS);
            return;
        }

        DifficultyChangeRequest request = resolveRequest(sender, player, args);
        if (request == null) return;

        Plot plot = request.plot();
        PlotDifficulty difficulty = request.difficulty();

        if (plot.getStatus() == Status.completed) {
            sendAlert(sender, LangPaths.Message.Error.PLAYER_IS_NOT_ALLOWED);
            return;
        }

        if (player != null && !isAdmin && DataProvider.BUILDER.canNotReviewPlot(player.getUniqueId(), plot)) {
            sendAlert(sender, LangPaths.Message.Error.PLAYER_HAS_NO_PERMISSIONS);
            return;
        }

        applyDifficulty(sender, player, plot, difficulty);
    }

    private boolean hasPermission(@Nullable Player player, boolean isAdmin) {
        return isAdmin || (player != null && DataProvider.BUILD_TEAM.isAnyReviewer(player.getUniqueId()));
    }

    private @Nullable DifficultyChangeRequest resolveRequest(
            @NotNull CommandSender sender, @Nullable Player player, String[] args) {
        Integer plotId = args.length > 0 ? AlpsUtils.tryParseInt(args[0]) : null;

        if (plotId != null) {
            return resolveByPlotId(sender, plotId, args);
        }
        if (player != null && args.length >= 1) {
            return resolveByCurrentPlot(sender, player, args[0]);
        }

        sendInfo(sender);
        return null;
    }

    private @Nullable DifficultyChangeRequest resolveByPlotId(
            @NotNull CommandSender sender, int plotId, String[] args) {
        if (args.length < 2) {
            sendInfo(sender);
            return null;
        }

        PlotDifficulty difficulty = parseDifficulty(args[1]);
        if (difficulty == null) {
            sendAlert(sender, LangPaths.Message.Error.INVALID_PLOT_DIFFICULTY);
            return null;
        }

        Plot plot = DataProvider.PLOT.getPlotById(plotId);
        if (plot == null) {
            sendAlert(sender, LangPaths.Message.Error.PLOT_DOES_NOT_EXIST);
            return null;
        }

        return new DifficultyChangeRequest(plot, difficulty);
    }

    private @Nullable DifficultyChangeRequest resolveByCurrentPlot(
            @NotNull CommandSender sender, @NotNull Player player, @NotNull String difficultyArg) {
        PlotDifficulty difficulty = parseDifficulty(difficultyArg);
        if (difficulty == null) {
            sendAlert(sender, LangPaths.Message.Error.INVALID_PLOT_DIFFICULTY);
            return null;
        }

        if (!PlotUtils.isPlotWorld(player.getWorld())) {
            sendInfo(sender);
            return null;
        }

        AbstractPlot currentPlot = PlotUtils.getCurrentPlot(Builder.byUUID(player.getUniqueId()), Status.values());
        if (!(currentPlot instanceof Plot plot)) {
            sendAlert(sender, LangPaths.Message.Error.PLOT_DOES_NOT_EXIST);
            return null;
        }

        return new DifficultyChangeRequest(plot, difficulty);
    }

    private void applyDifficulty(
            @NotNull CommandSender sender,
            @Nullable Player player,
            @NotNull Plot plot,
            @NotNull PlotDifficulty difficulty) {
        String difficultyName = formatDifficultyName(difficulty);

        if (plot.getDifficulty() == difficulty) {
            sender.sendMessage(Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(
                    sender,
                    LangPaths.Message.Info.PLOT_DIFFICULTY_UNCHANGED,
                    String.valueOf(plot.getId()),
                    difficultyName
            )));
            return;
        }

        if (!plot.setDifficulty(difficulty)) {
            sendAlert(sender, LangPaths.Message.Error.ERROR_OCCURRED);
            return;
        }

        sender.sendMessage(Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(
                sender,
                LangPaths.Message.Info.CHANGED_PLOT_DIFFICULTY,
                String.valueOf(plot.getId()),
                difficultyName
        )));

        if (player != null) {
            Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () ->
                    player.playSound(player.getLocation(), Utils.SoundUtils.DONE_SOUND, 1f, 1f));
        }
    }

    private void sendAlert(@NotNull CommandSender sender, @NotNull String langPath) {
        sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, langPath)));
    }

    private static @NotNull String formatDifficultyName(@NotNull PlotDifficulty difficulty) {
        return difficulty.name().charAt(0) + difficulty.name().substring(1).toLowerCase(Locale.ROOT);
    }

    private static @Nullable PlotDifficulty parseDifficulty(@NotNull String input) {
        try {
            return PlotDifficulty.valueOf(input.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException _) {
            return null;
        }
    }

    @Override
    public String[] getNames() {
        return new String[]{"setdifficulty"};
    }

    @Override
    public String getDescription() {
        return "Changes the difficulty of a plot.";
    }

    @Override
    public String[] getParameter() {
        return new String[]{"ID", "Difficulty"};
    }

    @Override
    public String getPermission() {
        return "plotsystem.review.setdifficulty";
    }
}
