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

package com.alpsbte.plotsystem.commands.review;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class CMD_EditFeedback extends BaseCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        Player player = getPlayer(sender);
        if (player == null) {
            Bukkit.getConsoleSender().sendMessage(text("This command can only be used as a player!", RED));
            return true;
        }

        CompletableFuture.runAsync(() -> {
            if (!DataProvider.BUILDER.isAnyReviewer(player.getUniqueId()) && !sender.hasPermission("plotsystem.admin")) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.PLAYER_HAS_NO_PERMISSIONS)));
                return;
            }

            if (args.length < 1 || AlpsUtils.tryParseInt(args[0]) == null) {
                sendInfo(sender);
                return;
            }

            Plot plot = DataProvider.PLOT.getPlotById(Integer.parseInt(args[0]));

            if (!plot.isReviewed() && !plot.isRejected()) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.PLOT_EITHER_UNCLAIMED_OR_UNREVIEWED)));
                return;
            }

            Builder builder = DataProvider.BUILDER.getBuilderByUUID(player.getUniqueId());
            if (!DataProvider.BUILDER.canReviewPlot(builder, plot) && !sender.hasPermission("plotsystem.admin")) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.CANNOT_SEND_FEEDBACK)));
                return;
            }

            StringBuilder feedback = new StringBuilder();
            for (int i = 2; i <= args.length; i++) {
                feedback.append(args.length == 2 ? "" : " ").append(args[i - 1]);
            }
            try {
                plot.getReview().setFeedback(feedback.toString());
            } catch (SQLException e) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.ERROR_OCCURRED)));
                PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), e);
            }

            sender.sendMessage(Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Info.UPDATED_PLOT_FEEDBACK, plot.getID() + "")));
        });
        return true;
    }

    @Override
    public String[] getNames() {
        return new String[]{"editfeedback"};
    }

    @Override
    public String getDescription() {
        return "Updates the feedback of a plot.";
    }

    @Override
    public String[] getParameter() {
        return new String[]{"ID", "Feedback"};
    }

    @Override
    public String getPermission() {
        return "plotsystem.review.editfeedback";
    }
}
