package com.alpsbte.plotsystem.commands.admin;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotHandler;
import com.alpsbte.plotsystem.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.text;

/**
 * Rewrites a plot's completed schematic into creator-compatible Y/origin form.
 * Use for legacy saves (origin stuck on the city floor) and corrupt absolute Y.
 */
public class CMD_RepairCompleted extends BaseCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat("You don't have permission to use this command!"));
            return true;
        }

        if (args.length == 0) {
            sendInfo(sender);
            return true;
        }

        Integer plotID = AlpsUtils.tryParseInt(args[0]);
        if (plotID == null) {
            sendInfo(sender);
            return true;
        }

        CompletableFuture.runAsync(() -> {
            Plot plot = DataProvider.PLOT.getPlotById(plotID);
            if (plot == null) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat("Could not find plot with ID #" + plotID + "!"));
                return;
            }

            try {
                PlotHandler.CompletedSchematicNormalization result = PlotHandler.normalizeCompletedSchematic(plot);
                if (result == null) {
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat(
                            "Plot #" + plotID + " has no completed schematic to repair!"));
                    return;
                }

                // Repair only rewrites the DB blob; paste into the city world so the fixed build is visible
                // without waiting for a later visit (city worlds that already exist used to skip paste).
                boolean pasted = PlotHandler.pasteCompletedSchematic(plot);

                Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
                    if (!result.changed()) {
                        sender.sendMessage(Utils.ChatUtils.getInfoFormat(
                                Component.text("Plot #" + plotID + " completed schematic is already aligned (minY ")
                                        .append(Component.text(String.valueOf(result.toMinY()), NamedTextColor.GOLD))
                                        .append(Component.text(")."))
                                        .append(Component.text(pasted ? " Re-pasted into city world." : ""))
                        ));
                        return;
                    }
                    sender.sendMessage(Utils.ChatUtils.getInfoFormat(
                            Component.text("Repaired plot #" + plotID + " completed schematic: minY ")
                                    .append(Component.text(String.valueOf(result.fromMinY()), NamedTextColor.GOLD))
                                    .append(Component.text(" -> "))
                                    .append(Component.text(String.valueOf(result.toMinY()), NamedTextColor.GOLD))
                                    .append(Component.text(result.originFixed() ? " (origin reset)" : ""))
                                    .append(Component.text(pasted ? ". Pasted into city world." : "."))
                    ));
                    if (getPlayer(sender) != null) {
                        getPlayer(sender).playSound(getPlayer(sender).getLocation(), Utils.SoundUtils.DONE_SOUND, 1f, 1f);
                    }
                });
            } catch (Exception exception) {
                PlotSystem.getPlugin().getComponentLogger().error(
                        text("Failed to repair completed schematic for plot #" + plotID),
                        exception
                );
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(
                        "Failed to repair plot #" + plotID + ": " + exception.getMessage()));
            }
        });
        return true;
    }

    @Override
    public String[] getNames() {
        return new String[]{"repaircompleted"};
    }

    @Override
    public String getDescription() {
        return "Repair a completed schematic's Y frame and paste origin.";
    }

    @Override
    public String[] getParameter() {
        return new String[]{"ID"};
    }

    @Override
    public String getPermission() {
        return "plotsystem.admin.repaircompleted";
    }
}
