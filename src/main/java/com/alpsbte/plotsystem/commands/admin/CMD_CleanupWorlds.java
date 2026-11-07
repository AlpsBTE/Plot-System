package com.alpsbte.plotsystem.commands.admin;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.core.system.plot.PlotWorldMaintenance;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.text;

/**
 * World maintenance: leftover finished {@code P-} worlds, or rebuild CIM {@code C-} worlds
 * by deleting/recreating them and re-pasting all completed plots.
 */
public class CMD_CleanupWorlds extends BaseCommand {
    private sealed interface CleanupRequest permits OnePlotCleanupRequest, CimRebuildRequest {}

    private record OnePlotCleanupRequest(boolean dryRun, @Nullable Integer limit) implements CleanupRequest {}

    private record CimRebuildRequest(@Nullable String cityId, boolean dryRun) implements CleanupRequest {}

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat("You don't have permission to use this command!"));
            return true;
        }

        CleanupRequest request = parseRequest(sender, args);
        if (request == null) return true;
        if (!PlotWorldMaintenance.tryBegin()) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat("A world maintenance command is already running!"));
            return true;
        }

        sender.sendMessage(Utils.ChatUtils.getInfoFormat(startMessage(request)));
        if (request instanceof CimRebuildRequest) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat(
                    "Warning: rebuilding a CIM world deletes it. In-progress CIM plots in that city would be wiped — the command fails if any exist."));
        }
        CompletableFuture.runAsync(() -> executeCleanup(sender, request));
        return true;
    }

    private @Nullable CleanupRequest parseRequest(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendInfo(sender);
            return null;
        }
        if (args[0].equalsIgnoreCase("cim")) {
            return parseCimRequest(sender, args);
        }
        return parseOnePlotRequest(sender, args);
    }

    private @Nullable OnePlotCleanupRequest parseOnePlotRequest(CommandSender sender, String[] args) {
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
                    "Provide how many worlds to process, for example /cleanupworlds 10 — or /cleanupworlds cim <cityId|all>."));
            return null;
        }
        return new OnePlotCleanupRequest(dryRun, limit);
    }

    private @Nullable CimRebuildRequest parseCimRequest(CommandSender sender, String[] args) {
        boolean dryRun = false;
        String cityId = null;
        boolean hasTarget = false;
        for (int i = 1; i < args.length; i++) {
            String argument = args[i];
            if (isDryRun(argument)) {
                if (dryRun) {
                    sendInfo(sender);
                    return null;
                }
                dryRun = true;
                continue;
            }
            if (hasTarget) {
                sendInfo(sender);
                return null;
            }
            hasTarget = true;
            cityId = argument.equalsIgnoreCase("all") ? null : argument;
        }
        if (!hasTarget) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat(
                    "Provide a city id or all, for example /cleanupworlds cim all or /cleanupworlds cim <cityId>."));
            return null;
        }
        return new CimRebuildRequest(cityId, dryRun);
    }

    private void executeCleanup(CommandSender sender, CleanupRequest request) {
        try {
            if (request instanceof OnePlotCleanupRequest onePlot) {
                PlotWorldMaintenance.FinishedWorldCleanup cleanup =
                        PlotWorldMaintenance.deleteFinishedOnePlotWorlds(onePlot.dryRun(), onePlot.limit());
                Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> reportOnePlotResult(sender, onePlot.dryRun(), cleanup));
                return;
            }

            CimRebuildRequest cim = (CimRebuildRequest) request;
            PlotWorldMaintenance.CimRebuildResult result =
                    PlotWorldMaintenance.rebuildCityInspirationWorlds(cim.cityId(), cim.dryRun());
            Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> reportCimResult(sender, cim.dryRun(), result));
        } catch (IllegalStateException | IllegalArgumentException exception) {
            Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () ->
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat(exception.getMessage())));
        } catch (Exception exception) {
            Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () ->
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while running world maintenance!")));
            PlotSystem.getPlugin().getComponentLogger().error(text("Could not run /cleanupworlds!"), exception);
        } finally {
            PlotWorldMaintenance.end();
        }
    }

    private void reportOnePlotResult(CommandSender sender, boolean dryRun, PlotWorldMaintenance.FinishedWorldCleanup cleanup) {
        sender.sendMessage(Utils.ChatUtils.getInfoFormat(onePlotSummary(dryRun, cleanup)));
        if (cleanup.failed() == 0) playDoneSound(sender);
    }

    private void reportCimResult(CommandSender sender, boolean dryRun, PlotWorldMaintenance.CimRebuildResult result) {
        sender.sendMessage(Utils.ChatUtils.getInfoFormat(cimSummary(dryRun, result)));
        if (result.citiesFailed() == 0 && result.plotsFailed() == 0) playDoneSound(sender);
    }

    private void playDoneSound(CommandSender sender) {
        Player player = getPlayer(sender);
        if (player == null) return;
        player.playSound(player.getLocation(), Utils.SoundUtils.DONE_SOUND, 1f, 1f);
    }

    private static String startMessage(CleanupRequest request) {
        if (request instanceof OnePlotCleanupRequest onePlot) {
            if (onePlot.dryRun()) {
                if (onePlot.limit() == null) return "Checking single-plot worlds left behind by finished plots...";
                return "Checking up to " + onePlot.limit() + " single-plot worlds left behind by finished plots...";
            }
            return "Processing up to " + onePlot.limit() + " finished single-plot worlds (delete / skip / fail each count)...";
        }
        CimRebuildRequest cim = (CimRebuildRequest) request;
        String target = cim.cityId() == null ? "all CIM cities" : "CIM city " + cim.cityId();
        if (cim.dryRun()) return "Checking rebuild of " + target + "...";
        return "Rebuilding " + target + " (delete + recreate world, then paste completed plots with delay)...";
    }

    private static String onePlotSummary(boolean dryRun, PlotWorldMaintenance.FinishedWorldCleanup cleanup) {
        return outcomeLabel(dryRun, "Would delete ", "Deleted ") + cleanup.deleted() + " world(s). "
                + outcomeLabel(dryRun, "Would skip ", "Skipped ") + cleanup.skippedOccupied() + " world(s) that still have players. "
                + outcomeLabel(dryRun, "Failed to check ", "Failed to delete ") + cleanup.failed() + " world(s).";
    }

    private static String cimSummary(boolean dryRun, PlotWorldMaintenance.CimRebuildResult result) {
        if (dryRun) {
            return "Would rebuild " + result.citiesProcessed() + " city world(s), delete "
                    + result.worldsWouldDelete() + " existing world(s), and paste "
                    + result.plotsWouldPaste() + " completed plot(s).";
        }
        return "Rebuilt " + result.citiesProcessed() + " city world(s). Deleted "
                + result.worldsDeleted() + " world(s). Pasted " + result.plotsPasted()
                + " completed plot(s). Failed cities: " + result.citiesFailed()
                + ". Failed plots: " + result.plotsFailed() + ".";
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
        return "Maintain leftover finished P- worlds, or rebuild CIM worlds (cim <cityId|all>). "
                + "CIM rebuild fails if unfinished/unreviewed CIM plots exist. --dry-run only reports.";
    }

    @Override
    public String[] getParameter() {
        return new String[]{"amount|cim"};
    }

    @Override
    public String getPermission() {
        return "plotsystem.admin.cleanupworlds";
    }
}
