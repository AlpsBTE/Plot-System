/*
 * The MIT License (MIT)
 *
 *  Copyright © 2025, Alps BTE <bte.atchli@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.alpsbte.plotsystem.commands.plot;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.commands.SubCommand;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class CMD_Plot_Abandon extends SubCommand {

    public CMD_Plot_Abandon(BaseCommand baseCommand) {
        super(baseCommand);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        CompletableFuture.runAsync(() -> {
            Plot plot;
            if (!(sender instanceof Player player)) {
                sendInfo(sender);
                return;
            }

            if (args.length > 0 && AlpsUtils.tryParseInt(args[0]) != null) {
                plot = DataProvider.PLOT.getPlotById(Integer.parseInt(args[0]));
            } else if (PlotUtils.isPlotWorld(player.getWorld())) {
                AbstractPlot p = PlotUtils.getCurrentPlot(Builder.byUUID(player.getUniqueId()), Status.unfinished);
                if (!(p instanceof Plot)) {
                    sendInfo(sender);
                    return;
                }
                plot = (Plot) p;
            } else {
                sendInfo(sender);
                return;
            }

            if (plot == null) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(langUtil.get(sender, LangPaths.Message.Error.PLOT_DOES_NOT_EXIST)));
                return;
            }
            if (plot.getStatus() != Status.unfinished) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(langUtil.get(sender, LangPaths.Message.Error.CAN_ONLY_ABANDON_UNFINISHED_PLOTS)));
                return;
            }
            if (!Utils.isOwnerOrReviewer(sender, player, plot)) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(langUtil.get(sender, LangPaths.Message.Error.PLAYER_IS_NOT_ALLOWED)));
                return;
            }

            Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
                if (PlotUtils.Actions.abandonPlot(plot)) {
                    sender.sendMessage(Utils.ChatUtils.getInfoFormat(langUtil.get(sender, LangPaths.Message.Info.ABANDONED_PLOT, plot.getID() + "")));
                    player.playSound(player.getLocation(), Utils.SoundUtils.ABANDON_PLOT_SOUND, 1, 1);
                }
            });
        });
    }

    @Override
    public String[] getNames() {
        return new String[]{"abandon"};
    }

    @Override
    public String getDescription() {
        return "Abandon a unfinished plot.";
    }

    @Override
    public String[] getParameter() {
        return new String[]{"ID"};
    }

    @Override
    public String getPermission() {
        return "plotsystem.plot.abandon";
    }
}
