/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
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
import com.alpsbte.plotsystem.core.menus.FeedbackMenu;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
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

public class CMD_Plot_Feedback extends SubCommand {

    public CMD_Plot_Feedback(BaseCommand baseCommand) {
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
            Plot plot;
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
            if (!plot.getPlotOwner().getUUID().equals(player.getUniqueId())
                    && plot.getPlotMembers().stream().noneMatch(m -> m.getUUID().equals(player.getUniqueId())) && !player.hasPermission("plotsystem.plot.review")) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(langUtil.get(sender, LangPaths.Message.Error.PLAYER_HAS_NO_PERMISSIONS)));
                return;
            }
            if (!plot.isReviewed()) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(langUtil.get(sender, LangPaths.Message.Error.PLOT_HAS_NOT_YET_REVIEWED)));
                return;
            }

            Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> new FeedbackMenu(player, plot.getID()));
        });
    }

    @Override
    public String[] getNames() {
        return new String[]{"feedback"};
    }

    @Override
    public String getDescription() {
        return "Shows all feedback information of a plot (Points, Feedback and Reviewer).";
    }

    @Override
    public String[] getParameter() {
        return new String[]{"ID"};
    }

    @Override
    public String getPermission() {
        return "plotsystem.plot.feedback";
    }
}
