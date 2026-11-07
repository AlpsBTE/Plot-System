package com.alpsbte.plotsystem.commands.admin;

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
 * Deletes and recreates CIM city worlds, then re-pastes all completed plots.
 */
public class CMD_PlotMaintenance_Cim extends SubCommand {
    private record CimRequest(@Nullable String cityId, boolean dryRun) {}

    public CMD_PlotMaintenance_Cim(BaseCommand baseCommand) {
        super(baseCommand);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        CimRequest request = parseRequest(sender, args);
        if (request == null) return;
        if (!PlotWorldMaintenance.tryBegin()) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat("A world maintenance command is already running!"));
            return;
        }

        sender.sendMessage(Utils.ChatUtils.getInfoFormat(startMessage(request)));
        sender.sendMessage(Utils.ChatUtils.getAlertFormat(
                "Warning: rebuilding a CIM world deletes it. In-progress CIM plots in that city would be wiped — the command fails if any exist."));
        CompletableFuture.runAsync(() -> execute(sender, request));
    }

    private @Nullable CimRequest parseRequest(CommandSender sender, String[] args) {
        boolean dryRun = false;
        String cityId = null;
        boolean hasTarget = false;
        for (String argument : args) {
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
                    "Provide a city id or all, for example /plotmaintenance cim all or /plotmaintenance cim <cityId>."));
            return null;
        }
        return new CimRequest(cityId, dryRun);
    }

    private void execute(CommandSender sender, CimRequest request) {
        try {
            PlotWorldMaintenance.CimRebuildResult result =
                    PlotWorldMaintenance.rebuildCityInspirationWorlds(request.cityId(), request.dryRun());
            Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
                sender.sendMessage(Utils.ChatUtils.getInfoFormat(summary(request.dryRun(), result)));
                if (result.citiesFailed() == 0 && result.plotsFailed() == 0) playDoneSound(sender);
            });
        } catch (IllegalStateException | IllegalArgumentException exception) {
            Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () ->
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat(exception.getMessage())));
        } catch (Exception exception) {
            Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () ->
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while rebuilding CIM world(s)!")));
            PlotSystem.getPlugin().getComponentLogger().error(text("Could not run /plotmaintenance cim!"), exception);
        } finally {
            PlotWorldMaintenance.end();
        }
    }

    private void playDoneSound(CommandSender sender) {
        Player player = getPlayer(sender);
        if (player == null) return;
        player.playSound(player.getLocation(), Utils.SoundUtils.DONE_SOUND, 1f, 1f);
    }

    private static String startMessage(CimRequest request) {
        String target = request.cityId() == null ? "all CIM cities" : "CIM city " + request.cityId();
        if (request.dryRun()) return "Checking rebuild of " + target + "...";
        return "Rebuilding " + target + " (delete + recreate world, then paste completed plots with delay)...";
    }

    private static String summary(boolean dryRun, PlotWorldMaintenance.CimRebuildResult result) {
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

    private static boolean isDryRun(String argument) {
        return argument.equalsIgnoreCase("--dry-run") || argument.equalsIgnoreCase("--dry");
    }

    @Override
    public String[] getNames() {
        return new String[]{"cim"};
    }

    @Override
    public String getDescription() {
        return "Delete/recreate CIM world(s) and re-paste completed plots. Fails if in-progress CIM plots exist.";
    }

    @Override
    public String[] getParameter() {
        return new String[]{"cityId|all", "--dry-run?"};
    }

    @Override
    public String getPermission() {
        return "plotsystem.admin.plotmaintenance";
    }
}
