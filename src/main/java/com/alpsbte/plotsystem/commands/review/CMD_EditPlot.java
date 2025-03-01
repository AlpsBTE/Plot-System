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

package com.alpsbte.plotsystem.commands.review;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class CMD_EditPlot extends BaseCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        Player player = getPlayer(sender);
        if (player == null) {
            Bukkit.getConsoleSender().sendMessage(Component.text("This command can only be used as a player!", RED));
            return true;
        }

        // TODO: don't register command if this config value is false
        if (!ConfigUtil.getInstance().configs[1].getBoolean(ConfigPaths.EDITPLOT_ENABLED)) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.COMMAND_DISABLED)));
            return true;
        }

        CompletableFuture.runAsync(() -> {
            if (!DataProvider.BUILDER.isAnyReviewer(player.getUniqueId()) && !sender.hasPermission("plotsystem.admin")) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.PLAYER_HAS_NO_PERMISSIONS)));
                return;
            }

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
            if (plot.getStatus() == Status.completed) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.PLAYER_IS_NOT_ALLOWED)));
                return;
            }


            Builder builder = Builder.byUUID(player.getUniqueId());
            if (!DataProvider.BUILDER.canReviewPlot(builder, plot) && !sender.hasPermission("plotsystem.admin")) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.PLAYER_HAS_NO_PERMISSIONS)));
                return;
            }

            Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {

                if (!plot.getPermissions().hasBuildingPerms(builder.getUUID())) {
                    plot.getPermissions().addBuilderPerms(builder.getUUID()).save();
                    sender.sendMessage(Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Info.ENABLED_PLOT_PERMISSIONS, plot.getID() + "")));
                    return;
                }

                plot.getPermissions().removeBuilderPerms(builder.getUUID()).save();
                sender.sendMessage(Utils.ChatUtils.getInfoFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Info.DISABLED_PLOT_PERMISSIONS, plot.getID() + "")));
            });
        });


        return true;
    }

    @Override
    public String[] getNames() {
        return new String[]{"editplot"};
    }

    @Override
    public String getDescription() {
        return "Enables/disables build permissions for reviewers on a plot.";
    }

    @Override
    public String[] getParameter() {
        return new String[]{"ID"};
    }

    @Override
    public String getPermission() {
        return "plotsystem.review.editplot";
    }
}
