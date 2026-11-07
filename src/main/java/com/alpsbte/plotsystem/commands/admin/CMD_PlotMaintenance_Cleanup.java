package com.alpsbte.plotsystem.commands.admin;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.commands.SubCommand;
import com.alpsbte.plotsystem.core.system.plot.PlotWorldMaintenance;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.text;

/**
 * Deletes leftover finished {@code P-} worlds in batches.
 */
public class CMD_PlotMaintenance_Cleanup extends SubCommand {
    private record CleanupRequest(boolean dryRun, @Nullable Integer limit) {}

    public CMD_PlotMaintenance_Cleanup(BaseCommand baseCommand) {
        super(baseCommand);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        CleanupRequest request = parseRequest(sender, args);
        if (request == null) return;
        if (!PlotWorldMaintenance.tryBegin()) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat("A world maintenance command is already running!"));
            return;
        }

        sender.sendMessage(Utils.ChatUtils.getInfoFormat(startMessage(request)));
        CompletableFuture.runAsync(() -> execute(sender, request));
    }

    private @Nullable CleanupRequest parseRequest(CommandSender sender, String[] args) {
        boolean dryRun = false;
        Integer limit = null;
        for (String argument : args) {
            if (isDryRun(argument)) {
                if (dryRun) {
                    sendInfo(sender);
                    return null;
                }
                dryRun = true;
                continue;
            }
            Integer amount = AlpsUtils.tryParseInt(argument);
            if (amount == null || amount < 1 || limit != null) {
                sendInfo(sender);
                return null;
            }
            limit = amount;
        }
        if (!dryRun && limit == null) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat(
                    "Provide how many worlds to process, for example /plotmaintenance cleanup 10."));
            return null;
        }
        return new CleanupRequest(dryRun, limit);
    }

    private void execute(CommandSender sender, CleanupRequest request) {
        try {
            PlotWorldMaintenance.FinishedWorldCleanup cleanup =
                    PlotWorldMaintenance.deleteFinishedOnePlotWorlds(request.dryRun(), request.limit());
            Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
                sender.sendMessage(Utils.ChatUtils.getInfoFormat(summary(request.dryRun(), cleanup)));
                if (cleanup.failed() == 0) playDoneSound(sender);
            });
        } catch (Exception exception) {
            Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () ->
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while cleaning up finished plot worlds!")));
            PlotSystem.getPlugin().getComponentLogger().error(text("Could not run /plotmaintenance cleanup!"), exception);
        } finally {
            PlotWorldMaintenance.end();
        }
    }

    private void playDoneSound(CommandSender sender) {
        Player player = getPlayer(sender);
        if (player == null) return;
        player.playSound(player.getLocation(), Utils.SoundUtils.DONE_SOUND, 1f, 1f);
    }

    private static String startMessage(CleanupRequest request) {
        if (request.dryRun()) {
            if (request.limit() == null) return "Checking single-plot worlds left behind by finished plots...";
            return "Checking up to " + request.limit() + " single-plot worlds left behind by finished plots...";
        }
        return "Processing up to " + request.limit() + " finished single-plot worlds (delete / skip / fail each count)...";
    }

    private static String summary(boolean dryRun, PlotWorldMaintenance.FinishedWorldCleanup cleanup) {
        return outcomeLabel(dryRun, "Would delete ", "Deleted ") + cleanup.deleted() + " world(s). "
                + outcomeLabel(dryRun, "Would skip ", "Skipped ") + cleanup.skippedOccupied() + " world(s) that still have players. "
                + outcomeLabel(dryRun, "Failed to check ", "Failed to delete ") + cleanup.failed() + " world(s).";
    }

    private static String outcomeLabel(boolean dryRun, String dryRunLabel, String deleteLabel) {
        return dryRun ? dryRunLabel : deleteLabel;
    }

    private static boolean isDryRun(String argument) {
        return argument.equalsIgnoreCase("--dry-run") || argument.equalsIgnoreCase("--dry");
    }

    @Override
    public String[] getNames() {
        return new String[]{"cleanup"};
    }

    @Override
    public String getDescription() {
        return "Process leftover finished P- worlds (success/skip/fail all count toward the limit).";
    }

    @Override
    public String[] getParameter() {
        return new String[]{"amount", "--dry-run?"};
    }

    @Override
    public String getPermission() {
        return "plotsystem.admin.plotmaintenance";
    }
}
