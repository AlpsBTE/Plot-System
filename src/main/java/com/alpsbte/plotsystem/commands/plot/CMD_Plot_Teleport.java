package com.alpsbte.plotsystem.commands.plot;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.commands.ICommand;
import com.alpsbte.plotsystem.commands.SubCommand;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.generator.DefaultPlotGenerator;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class CMD_Plot_Teleport extends SubCommand implements ICommand {

    public CMD_Plot_Teleport(BaseCommand baseCommand) {
        super(baseCommand);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player player = getPlayer(sender);
        if (player == null) {
            Bukkit.getConsoleSender().sendMessage(Component.text("This command can only be used as a player!", NamedTextColor.RED));
            return;
        }

        if (args.length == 0 || AlpsUtils.tryParseInt(args[0]) == null) {
            sendInfo(sender);
            return;
        }

        int plotID = Integer.parseInt(args[0]);

        CompletableFuture.runAsync(() -> {
            Plot plot = DataProvider.PLOT.getPlotById(plotID);

            if (plot != null && plot.getVersion() <= AbstractPlot.LEGACY_VERSION_THRESHOLD) {
                player.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.CANNOT_LOAD_LEGACY_PLOT)));
                return;
            }

            if (plot == null || plot.getStatus() == Status.unclaimed) {
                if (!sender.hasPermission("plotsystem.admin") || plot == null) {
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat(langUtil.get(sender, LangPaths.Message.Error.PLOT_DOES_NOT_EXIST)));
                    return;
                }

                Builder builder = Builder.byUUID(player.getUniqueId());
                if (builder == null) {
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat(langUtil.get(sender, LangPaths.Message.Error.ERROR_OCCURRED)));
                    return;
                }

                Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> new DefaultPlotGenerator(plot, builder));
                return;
            }

            Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> plot.getWorld().teleportPlayer(player));
        });
    }

    @Override
    public String[] getNames() {
        return new String[]{"tp", "teleport"};
    }

    @Override
    public String getDescription() {
        return "Teleport to a plot.";
    }

    @Override
    public String[] getParameter() {
        return new String[]{"ID"};
    }

    @Override
    public String getPermission() {
        return "plotsystem.plot.teleport";
    }
}
