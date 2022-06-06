package com.alpsbte.plotsystem.core.leaderboards;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.holograms.HologramManager;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.config.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.language.LangPaths;
import com.alpsbte.plotsystem.utils.io.language.LangUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ScoreLeaderboard {
    private LeaderboardTimeframe sortBy = null;
    private BukkitTask changeSortTask = null;
    private BukkitTask actionbarTask = null;

    /**
     * Get all pages that are enabled
     *
     * @return Enabled pages based on config values
     */
    private List<LeaderboardTimeframe> getPages() {
        FileConfiguration config = PlotSystem.getPlugin().getConfigManager().getConfig();
        return Arrays.stream(LeaderboardTimeframe.values()).filter(p -> config.getBoolean(p.configPath)).collect(Collectors.toList());
    }

    public void setSortBy(LeaderboardTimeframe sortBy) {
        this.sortBy = sortBy;

        HologramManager.<com.alpsbte.plotsystem.core.holograms.ScoreLeaderboard>getHologram("score-leaderboard").setSortBy(sortBy);
    }

    public void initialize() {
        if (getPages().size() < 1) {
            PlotSystem.getPlugin().getLogger().warning("Unable to initialize leaderboards; no display pages enabled! Check config for display-options.");
            return;
        }

        PlotSystem.getPlugin().getLogger().info(getPages().size() + "");
        for (LeaderboardTimeframe s : getPages()) {
            PlotSystem.getPlugin().getLogger().info(s.toString());
        }

        setSortBy(getPages().get(0));

        FileConfiguration config = PlotSystem.getPlugin().getConfigManager().getConfig();
        long changeDelay = config.getInt(ConfigPaths.DISPLAY_OPTIONS_INTERVAL, 5) * 20L;

        changeSortTask = new BukkitRunnable() {
            @Override
            public void run() {
                // skip this check as there isn't any other pages to change to
                if(getPages().size() == 1) return;

                Enum<LeaderboardTimeframe> next = Utils.getNextListItem(getPages(), sortBy);
                if (next == null) {
                    setSortBy(getPages().get(0));
                } else {
                    setSortBy((LeaderboardTimeframe) next);
                }
            }
        }.runTaskTimer(PlotSystem.getPlugin(), changeDelay, changeDelay);

        actionbarTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : showToPlayers()) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, rankingString(player));
                }
            }
        }.runTaskTimer(PlotSystem.getPlugin(), 0L, 20L);
    }

    public void shutdown() {
        if (changeSortTask != null) {
            changeSortTask.cancel();
            changeSortTask = null;
        }

        if (actionbarTask != null) {
            actionbarTask.cancel();
            actionbarTask = null;
        }
    }

    private final DecimalFormat df = new DecimalFormat("#.##");

    private BaseComponent[] rankingString(Player player) {
        int position = -1;
        int rows = 0;
        try {
            position = Builder.getBuilderScorePosition(player.getUniqueId(), sortBy);
            rows = Builder.getBuildersInSort(sortBy);
        } catch (SQLException e) {
            e.printStackTrace();
            return TextComponent.fromLegacyText("§cSQL Exception");
        }

        ComponentBuilder builder = new ComponentBuilder("");
        builder.append(
                new ComponentBuilder(LangUtil.get(player, sortBy.langPath) + " ")
                        .color(ChatColor.GREEN)
                        .create()
        );

        if (position == -1) {
            builder.append(
                    new ComponentBuilder(LangUtil.get(player, LangPaths.Leaderboards.NOT_ON_LEADERBOARD))
                            .color(ChatColor.RED)
                            .create()
            );
        } else if (position < 50) {
            builder.append(
                    new ComponentBuilder(
                            LangUtil.get(player, LangPaths.Leaderboards.ACTIONBAR_POSITION).replace("{integer}", position + "")
                    ).create()
            );
        } else {
            String topPercentage = df.format(position * 1.0 / rows);
            builder.append(
                    new ComponentBuilder(
                            LangUtil.get(player, LangPaths.Leaderboards.ACTIONBAR_PERCENTAGE).replace("{percentage}", topPercentage)
                    ).create()
            );
        }

        return builder.create();
    }

    private List<Player> showToPlayers() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    public enum LeaderboardTimeframe {
        DAILY(ConfigPaths.DISPLAY_OPTIONS_SHOW_DAILY),
        WEEKLY(ConfigPaths.DISPLAY_OPTIONS_SHOW_WEEKLY),
        MONTHLY(ConfigPaths.DISPLAY_OPTIONS_SHOW_MONTHLY),
        YEARLY(ConfigPaths.DISPLAY_OPTIONS_SHOW_YEARLY),
        LIFETIME(ConfigPaths.DISPLAY_OPTIONS_SHOW_LIFETIME);

        public final String configPath;
        public final String langPath;
        LeaderboardTimeframe(String configPath) {
            this.configPath = configPath;
            this.langPath = LangPaths.Leaderboards.PAGES + name();
        }
    }
}
