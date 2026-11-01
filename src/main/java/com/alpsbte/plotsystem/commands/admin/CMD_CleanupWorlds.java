package com.alpsbte.plotsystem.commands.admin;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.core.system.plot.PlotHandler;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.kyori.adventure.text.Component.text;

public class CMD_CleanupWorlds extends BaseCommand {
    private static final AtomicBoolean RUNNING = new AtomicBoolean();

    private record CleanupRequest(boolean dryRun, @Nullable Integer limit) {}

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat("You don't have permission to use this command!"));
            return true;
        }

        CleanupRequest request = parseRequest(sender, args);
        if (request == null) return true;
        if (!markRunning(sender)) return true;

        runCleanup(sender, request);
        return true;
    }

    private @Nullable CleanupRequest parseRequest(CommandSender sender, String[] args) {
        boolean dryRun = false;
        Integer limit = null;
        for (String argument : args) {
            if (isDryRun(argument)) {
                dryRun = acceptDryRun(sender, dryRun);
                if (!dryRun) return null;
                continue;
            }
            limit = acceptLimit(sender, argument, limit);
            if (limit == null) return null;
        }
        if (!dryRun && limit == null) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat("Provide how many worlds to delete, for example /cleanupworlds 10."));
            return null;
        }
        return new CleanupRequest(dryRun, limit);
    }

    private boolean acceptDryRun(CommandSender sender, boolean alreadySet) {
        if (alreadySet) {
            sendInfo(sender);
            return false;
        }
        return true;
    }

    private @Nullable Integer acceptLimit(CommandSender sender, String argument, @Nullable Integer currentLimit) {
        Integer amount = AlpsUtils.tryParseInt(argument);
        if (amount == null || amount < 1 || currentLimit != null) {
            sendInfo(sender);
            return null;
        }
        return amount;
    }

    private boolean markRunning(CommandSender sender) {
        if (RUNNING.compareAndSet(false, true)) return true;
        sender.sendMessage(Utils.ChatUtils.getAlertFormat("A finished-plot world cleanup is already running!"));
        return false;
    }

    private void runCleanup(CommandSender sender, CleanupRequest request) {
        sender.sendMessage(Utils.ChatUtils.getInfoFormat(startMessage(request)));
        CompletableFuture.runAsync(() -> executeCleanup(sender, request));
    }

    private void executeCleanup(CommandSender sender, CleanupRequest request) {
        try {
            PlotHandler.FinishedWorldCleanup cleanup = PlotHandler.deleteFinishedOnePlotWorlds(request.dryRun(), request.limit());
            Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> reportResult(sender, request.dryRun(), cleanup));
        } catch (Exception exception) {
            Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () ->
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while cleaning up finished plot worlds!")));
            PlotSystem.getPlugin().getComponentLogger().error(text("Could not clean up finished plot worlds!"), exception);
        } finally {
            RUNNING.set(false);
        }
    }

    private void reportResult(CommandSender sender, boolean dryRun, PlotHandler.FinishedWorldCleanup cleanup) {
        sender.sendMessage(Utils.ChatUtils.getInfoFormat(summary(dryRun, cleanup)));
        playDoneSound(sender, cleanup);
    }

    private void playDoneSound(CommandSender sender, PlotHandler.FinishedWorldCleanup cleanup) {
        if (cleanup.failed() != 0) return;
        Player player = getPlayer(sender);
        if (player == null) return;
        player.playSound(player.getLocation(), Utils.SoundUtils.DONE_SOUND, 1f, 1f);
    }

    private static String startMessage(CleanupRequest request) {
        if (request.dryRun()) return "Checking single-plot worlds left behind by finished plots...";
        return "Deleting up to " + request.limit() + " single-plot worlds left behind by finished plots...";
    }

    private static String summary(boolean dryRun, PlotHandler.FinishedWorldCleanup cleanup) {
        return outcomeLabel(dryRun, "Would delete ", "Deleted ") + cleanup.deleted() + " world(s). "
                + outcomeLabel(dryRun, "Would skip ", "Skipped ") + cleanup.skippedOccupied() + " world(s) that still have players. "
                + outcomeLabel(dryRun, "Failed to check ", "Failed to delete ") + cleanup.failed() + " world(s).";
    }

    private static String outcomeLabel(boolean dryRun, String dryRunLabel, String deleteLabel) {
        if (dryRun) return dryRunLabel;
        return deleteLabel;
    }

    private static boolean isDryRun(String argument) {
        return argument.equalsIgnoreCase("--dry-run") || argument.equalsIgnoreCase("--dry");
    }

    @Override
    public String[] getNames() {
        return new String[]{"cleanupworlds"};
    }

    @Override
    public String getDescription() {
        return "Delete up to <amount> single-plot worlds left behind by finished plots. --dry-run only reports the counts.";
    }

    @Override
    public String[] getParameter() {
        return new String[]{"amount"};
    }

    @Override
    public String getPermission() {
        return "plotsystem.admin.cleanupworlds";
    }
}
