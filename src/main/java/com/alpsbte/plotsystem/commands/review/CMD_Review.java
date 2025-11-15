package com.alpsbte.plotsystem.commands.review;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.menus.review.ReviewMenu;
import com.alpsbte.plotsystem.core.menus.review.ReviewPlotMenu;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class CMD_Review extends BaseCommand {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        Player player = getPlayer(sender);
        if (player == null) {
            Bukkit.getConsoleSender().sendMessage(Component.text("This command can only be used as a player!", RED));
            return true;
        }

        CompletableFuture.runAsync(() -> {
            if (!DataProvider.BUILD_TEAM.isAnyReviewer(player.getUniqueId()) && !sender.hasPermission("plotsystem.admin")) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.PLAYER_HAS_NO_PERMISSIONS)));
                return;
            }

            Builder builder = DataProvider.BUILDER.getBuilderByUUID(player.getUniqueId());
            AbstractPlot currentPlot = PlotUtils.getCurrentPlot(builder);

            if (args.length == 0 && !(currentPlot instanceof Plot)) {
                Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> new ReviewMenu(player));
                return;
            }

            Plot plotToReview;
            if (args.length == 0) plotToReview = (Plot) currentPlot;
            else {
                Integer id = AlpsUtils.tryParseInt(args[0]);
                if (id == null) {
                    sendInfo(sender);
                    return;
                }

                plotToReview = DataProvider.PLOT.getPlotById(id);
                if (plotToReview == null || plotToReview.getStatus() != Status.unreviewed) {
                    player.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(player, LangPaths.Message.Error.PLOT_DOES_NOT_EXIST)));
                    return;
                }
            }

            if (plotToReview.getVersion() <= AbstractPlot.LEGACY_VERSION_THRESHOLD) {
                player.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(player, LangPaths.Message.Error.CANNOT_LOAD_LEGACY_PLOT)));
                return;
            }

            if (DataProvider.BUILDER.canNotReviewPlot(builder.getUUID(), plotToReview) && !sender.hasPermission("plotsystem.admin")) {
                sender.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(sender, LangPaths.Message.Error.PLAYER_HAS_NO_PERMISSIONS)));
                return;
            }

            // Players cannot review their own plots
            boolean isParticipant = plotToReview.getPlotOwner().getUUID() == player.getUniqueId() || plotToReview.getPlotMembers().stream().anyMatch(b -> b.getUUID() == player.getUniqueId());
            if (!PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.DEV_MODE) && isParticipant) {
                player.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(player, LangPaths.Message.Error.CANNOT_REVIEW_OWN_PLOT)));
                return;
            }

            // Check if the reviewer is on the plot
            boolean teleportPlayer = false;
            if (currentPlot instanceof Plot currentPlotCast) {
                if (plotToReview.getId() != currentPlotCast.getId()) {
                    teleportPlayer = true;
                    if (PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.DEV_MODE)) {
                        PlotSystem.getPlugin().getComponentLogger().info(text("Review: Player on different plot, will teleport. Current: " + currentPlotCast.getId() + ", Target: " + plotToReview.getId()));
                    }
                } else {
                    if (PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.DEV_MODE)) {
                        PlotSystem.getPlugin().getComponentLogger().info(text("Review: Player on target plot " + plotToReview.getId() + ", opening menu directly"));
                    }
                }
            } else {
                teleportPlayer = true;
                if (PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.DEV_MODE)) {
                    PlotSystem.getPlugin().getComponentLogger().info(text("Review: Player not on any plot, will teleport to plot " + plotToReview.getId()));
                }
            }

            Plot finalPlotToReview = plotToReview;

            // If the reviewer is not on the plot, teleport the player first
            if (teleportPlayer) {
                Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
                    plotToReview.getWorld().teleportPlayer(player);
                    if (PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.DEV_MODE)) {
                        PlotSystem.getPlugin().getComponentLogger().info(text("Review: Teleported player, scheduling menu open in 20 ticks"));
                    }
                });
                return;
            }

            // Player is already on the plot, open menu on main thread
            Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
                if (PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.DEV_MODE)) {
                    PlotSystem.getPlugin().getComponentLogger().info(text("Review: Opening ReviewPlotMenu for plot " + finalPlotToReview.getId() + " (no teleport needed)"));
                }
                new ReviewPlotMenu(player, finalPlotToReview);
            });
        });
        return true;
    }

    @Override
    public String[] getNames() {
        return new String[]{"review"};
    }

    @Override
    public String getDescription() {
        return "Opens the review menu or review plot menu.";
    }

    @Override
    public String[] getParameter() {
        return new String[]{"ID"};
    }

    @Override
    public String getPermission() {
        return "";
    }
}
