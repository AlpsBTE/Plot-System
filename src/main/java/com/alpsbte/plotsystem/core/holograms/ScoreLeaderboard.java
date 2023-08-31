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

package com.alpsbte.plotsystem.core.holograms;

import com.alpsbte.alpslib.hologram.HolographicPagedDisplay;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.Payout;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import me.filoghost.holographicdisplays.api.Position;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ScoreLeaderboard extends HolographicPagedDisplay {
    private final DecimalFormat df = new DecimalFormat("#.##");
    private LeaderboardTimeframe sortByLeaderboard = LeaderboardTimeframe.DAILY;
    private BukkitTask actionbarTask = null;

    protected ScoreLeaderboard(@NotNull String id) {
        super(id, PlotSystem.getPlugin());
    }

    @Override
    public void create(Position position) {
        if (!PlotSystem.getPlugin().isEnabled()) return;
        if (getPages().size() < 1) {
            PlotSystem.getPlugin().getLogger().log(Level.WARNING, "Unable to initialize Score-Leaderboard - No display pages enabled! Check config for display-options.");
            return;
        }

        super.create(position);

        actionbarTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : showToPlayers()) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, rankingString(player));
                }
            }
        }.runTaskTimerAsynchronously(PlotSystem.getPlugin(), 0L, 20L);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Material.NETHER_STAR);
    }

    @Override
    public String getTitle() {
        return "§b§lTOP SCORE §6§l[" + (sortByLeaderboard.toString().charAt(0) + sortByLeaderboard.toString().toLowerCase().substring(1)) + "]";
    }

    @Override
    public List<DataLine<?>> getHeader() {
        sortByLeaderboard = LeaderboardTimeframe.valueOf(sortByPage);
        return super.getHeader();
    }

    @Override
    public List<DataLine<?>> getContent() {
        try {
            ArrayList<DataLine<?>> lines = new ArrayList<>();

            for (int index = 0; index < 10; index++) {
                lines.add(new LeaderboardPositionLineWithPayout(index + 1, null, 0));
            }

            int index = 0;
            for (Builder.DatabaseEntry<String, Integer> entry : Builder.getBuildersByScore(sortByLeaderboard)) {
                lines.set(index, new LeaderboardPositionLineWithPayout(index + 1, entry.getKey(), entry.getValue()));
                index++;
            }

            return lines;
        } catch (SQLException ex) {
            PlotSystem.getPlugin().getLogger().log(Level.SEVERE, "An error occurred while reading leaderboard content", ex);
        }
        return new ArrayList<>();
    }

    private BaseComponent[] rankingString(Player player) {
        int position;
        int rows;
        int myScore;
        try {
            position = Builder.getBuilderScorePosition(player.getUniqueId(), sortByLeaderboard);
            rows = Builder.getBuildersInSort(sortByLeaderboard);
            myScore = Builder.getBuilderScore(player.getUniqueId(), sortByLeaderboard);
        } catch (SQLException e) {
            e.printStackTrace();
            return TextComponent.fromLegacyText("§cSQL Exception");
        }

        ComponentBuilder builder = new ComponentBuilder("");
        builder.append(
                new ComponentBuilder("  " + LangUtil.getInstance().get(player, sortByLeaderboard.langPath))
                        .color(ChatColor.GOLD)
                        .bold(true)
                        .create()
        );

        builder.append(
                new ComponentBuilder(" ➜ ")
                        .color(ChatColor.DARK_GRAY)
                        .bold(true)
                        .create()
        );

        if (position == -1) {
            builder.append(
                    new ComponentBuilder(LangUtil.getInstance().get(player, LangPaths.Leaderboards.NOT_ON_LEADERBOARD))
                            .color(ChatColor.RED)
                            .bold(false)
                            .create()
            );
        } else if (position < 50) {
            builder.append(
                    new ComponentBuilder(
                            LangUtil.getInstance().get(player, LangPaths.Leaderboards.ACTIONBAR_POSITION, String.valueOf(position))
                    ).color(ChatColor.GREEN).bold(false).create()
            );
        } else {
            String topPercentage = df.format(position * 1.0 / rows);
            builder.append(
                    new ComponentBuilder(
                            LangUtil.getInstance().get(player, LangPaths.Leaderboards.ACTIONBAR_PERCENTAGE, topPercentage)
                    ).bold(false).create()
            );
        }

        if (myScore != -1) {
            builder.append(TextComponent.fromLegacyText("§8 (§b" + myScore + " points§8)"));
        }

        return builder.bold(false).create();
    }

    private List<Player> showToPlayers() {
        FileConfiguration config = PlotSystem.getPlugin().getConfig();
        boolean actionBarEnabled = config.getBoolean(ConfigPaths.DISPLAY_OPTIONS_ACTION_BAR_ENABLED, true);
        int actionBarRadius = config.getInt(ConfigPaths.DISPLAY_OPTIONS_ACTION_BAR_RADIUS, 30);
        List<Player> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Position position = getHologram().getPosition();
            if (player.getWorld().getName().equals(position.getWorldName()) && (!actionBarEnabled || position.distance(player.getLocation()) <= actionBarRadius)) {
                players.add(player);
            }
        }
        return players;
    }

    @Override
    public List<String> getPages() {
        if (ConfigUtil.getInstance() == null) return new ArrayList<>();
        FileConfiguration config = PlotSystem.getPlugin().getConfig();
        return Arrays.stream(LeaderboardTimeframe.values())
                .filter(p -> config.getBoolean(p.configPath)).map(LeaderboardTimeframe::toString).collect(Collectors.toList());
    }

    @Override
    public long getInterval() {
        return PlotSystem.getPlugin().getConfig().getInt(ConfigPaths.DISPLAY_OPTIONS_INTERVAL) * 20L;
    }

    @Override
    public void remove() {
        if (actionbarTask != null) {
            actionbarTask.cancel();
            actionbarTask = null;
        }
        super.remove();
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

    private class LeaderboardPositionLineWithPayout extends LeaderboardManager.LeaderboardPositionLine {
        private final int position;

        public LeaderboardPositionLineWithPayout(int position, String username, int score) {
            super(position, username, score);
            this.position = position;
        }

        @Override
        public String getLine() {
            try {
                String line = super.getLine();
                Payout payout = sortByLeaderboard != LeaderboardTimeframe.LIFETIME ? Payout.getPayout(sortByLeaderboard, position) : null;
                if (payout == null) {
                    return line;
                } else {
                    String payoutAmount = payout.getPayoutAmount();
                    try {
                        // if payout amount can be number, prefix with dollar sign
                        Integer.valueOf(payoutAmount);
                        payoutAmount = "$" + payoutAmount;
                    } catch (NumberFormatException ignored) {}

                    return line + " §7- §e§l" + payoutAmount;
                }
            } catch (SQLException e) {
                return super.getLine() + " §7- §cSQL ERR";
            }
        }
    }
}