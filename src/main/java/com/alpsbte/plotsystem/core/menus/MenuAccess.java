package com.alpsbte.plotsystem.core.menus;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.plot.CMD_Plot_Members;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.utils.DependencyManager;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Centralized visibility checks for menu buttons so players only see actions they can use.
 */
public final class MenuAccess {
    private MenuAccess() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isPlotOwner(@NotNull Player player, @NotNull Plot plot) {
        return plot.getPlotOwner().getUUID().equals(player.getUniqueId());
    }

    public static boolean isPlotMember(@NotNull Player player, @NotNull Plot plot) {
        return plot.getPlotMembers().stream().anyMatch(member -> member.getUUID().equals(player.getUniqueId()));
    }

    public static boolean isOwnerOrReviewer(@NotNull Player player, @NotNull Plot plot) {
        return player.hasPermission("plotsystem.review") || isPlotOwner(player, plot);
    }

    public static boolean canSubmit(@NotNull Player player, @NotNull Plot plot) {
        return plot.getStatus() == Status.unfinished
                && player.hasPermission("plotsystem.plot.submit")
                && isOwnerOrReviewer(player, plot);
    }

    public static boolean canUndoSubmit(@NotNull Player player, @NotNull Plot plot) {
        return plot.getStatus() == Status.unreviewed
                && player.hasPermission("plotsystem.plot.undosubmit")
                && isOwnerOrReviewer(player, plot);
    }

    public static boolean canAbandon(@NotNull Player player, @NotNull Plot plot) {
        return plot.getStatus() == Status.unfinished
                && player.hasPermission("plotsystem.plot.abandon")
                && isOwnerOrReviewer(player, plot);
    }

    public static boolean canViewFeedback(@NotNull Player player, @NotNull Plot plot) {
        return plot.getLatestReview().isPresent()
                && player.hasPermission("plotsystem.plot.feedback")
                && (isPlotOwner(player, plot)
                || isPlotMember(player, plot)
                || player.hasPermission("plotsystem.plot.review")
                || player.hasPermission("plotsystem.review")
                || player.hasPermission("plotsystem.admin"));
    }

    public static boolean canManageMembers(@NotNull Player player, @NotNull Plot plot) {
        return !plot.isReviewed()
                && plot.getStatus() == Status.unfinished
                && PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.ENABLE_GROUP_SUPPORT)
                && (player.hasPermission("plotsystem.admin")
                || (isPlotOwner(player, plot) && player.hasPermission(CMD_Plot_Members.PERMISSION)));
    }

    public static boolean canLeavePlot(@NotNull Player player, @NotNull Plot plot) {
        return !plot.isReviewed()
                && PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.ENABLE_GROUP_SUPPORT)
                && isPlotMember(player, plot);
    }

    public static boolean canOpenTutorials(@NotNull Player player) {
        return PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.TUTORIAL_ENABLE)
                && player.hasPermission("plotsystem.tutorial");
    }

    public static boolean canOpenBuilderUtilities(@NotNull Player player) {
        return PlotUtils.isPlotWorld(player.getWorld());
    }

    public static boolean canOpenCustomHeads() {
        return DependencyManager.isHeadDatabaseEnabled();
    }

    public static boolean canOpenBannerMaker() {
        return DependencyManager.isBannerMakerEnabled();
    }

    /**
     * Evenly spaced chest slots for a centered row of action buttons (slots 10–16).
     */
    public static int[] centeredActionSlots(int count) {
        return switch (count) {
            case 1 -> new int[]{13};
            case 2 -> new int[]{11, 15};
            case 3 -> new int[]{11, 13, 15};
            case 4 -> new int[]{10, 12, 14, 16};
            case 5 -> new int[]{10, 11, 13, 15, 16};
            default -> {
                int[] slots = new int[Math.max(count, 0)];
                for (int i = 0; i < slots.length; i++) {
                    slots[i] = 10 + i;
                }
                yield slots;
            }
        };
    }
}
