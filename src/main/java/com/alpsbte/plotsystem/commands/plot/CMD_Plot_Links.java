package com.alpsbte.plotsystem.commands.plot;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.commands.SubCommand;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.text;

public class CMD_Plot_Links extends SubCommand {

    public CMD_Plot_Links(BaseCommand baseCommand) {
        super(baseCommand);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player player = getPlayer(sender);
        if (player == null) {
            Bukkit.getConsoleSender().sendMessage(text("This command can only be used as a player!", NamedTextColor.RED));
            return;
        }

        CompletableFuture.runAsync(() -> {
            Builder builder = Builder.byUUID(player.getUniqueId());
            if (args.length == 0) {
                if (!PlotUtils.isPlotWorld(player.getWorld())) {
                    sendInfo(sender);
                    return;
                }
                PlotUtils.ChatFormatting.sendLinkMessages(PlotUtils.getCurrentPlot(builder, Status.unfinished, Status.unreviewed), player);
                return;
            }

            if (AlpsUtils.tryParseInt(args[0]) == null) {
                sendInfo(sender);
                return;
            }
            Plot plot = DataProvider.PLOT.getPlotById(Integer.parseInt(args[0]));
            if (plot == null) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(langUtil.get(sender, LangPaths.Message.Error.PLOT_DOES_NOT_EXIST)));
                return;
            }

            PlotUtils.ChatFormatting.sendLinkMessages(plot, player);
        });
    }

    @Override
    public String[] getNames() {
        return new String[]{"links", "link"};
    }

    @Override
    public String getDescription() {
        return "Sends you the links of the plot you are currently on.";
    }

    @Override
    public String[] getParameter() {
        return new String[]{"ID"};
    }

    @Override
    public String getPermission() {
        return "plotsystem.plot.links";
    }
}
