package com.alpsbte.plotsystem.commands.admin;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.commands.SubCommand;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotHandler;
import com.alpsbte.plotsystem.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.text;

/**
 * Repairs a completed schematic whose absolute min Y drifted from the initial schematic.
 */
public class CMD_PlotMaintenance_Repair extends SubCommand {
    public CMD_PlotMaintenance_Repair(BaseCommand baseCommand) {
        super(baseCommand);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendInfo(sender);
            return;
        }

        Integer plotID = AlpsUtils.tryParseInt(args[0]);
        if (plotID == null) {
            sendInfo(sender);
            return;
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
    }

    @Override
    public String[] getNames() {
        return new String[]{"repair"};
    }

    @Override
    public String getDescription() {
        return "Repair a completed schematic whose min Y drifted, then paste it into the city world.";
    }

    @Override
    public String[] getParameter() {
        return new String[]{"ID"};
    }

    @Override
    public String getPermission() {
        return "plotsystem.admin.plotmaintenance";
    }
}
