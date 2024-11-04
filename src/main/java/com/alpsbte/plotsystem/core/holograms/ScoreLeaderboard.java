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

import com.alpsbte.alpslib.hologram.DecentHologramPagedDisplay;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.Payout;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.BaseItems;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ScoreLeaderboard extends DecentHologramPagedDisplay implements HologramConfiguration {
    private final DecimalFormat df = new DecimalFormat("#.##");
    private LeaderboardTimeframe sortByLeaderboard;

    protected ScoreLeaderboard() {
        super("score-leaderboard", null, false, PlotSystem.getPlugin());
        setEnabled(PlotSystem.getPlugin().getConfig().getBoolean(getEnablePath()));
        setLocation(HologramRegister.getLocation(this));

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : getPlayersInRadiusForRanking()) {
                    if (AbstractTutorial.getActiveTutorial(player.getUniqueId()) != null) continue;
                    player.sendActionBar(getRankingString(player));
                }
            }
        }.runTaskTimerAsynchronously(PlotSystem.getPlugin(), 0L, 20L);
    }

    @Override
    public List<String> getPages() {
        if (ConfigUtil.getInstance() == null) return new ArrayList<>();
        FileConfiguration config = PlotSystem.getPlugin().getConfig();
        return Arrays.stream(LeaderboardTimeframe.values())
                .filter(p -> config.getBoolean(p.configPath)).map(LeaderboardTimeframe::toString).collect(Collectors.toList());
    }

    @Override
    public void create(Player player) {
        if (getPages().isEmpty()) {
            PlotSystem.getPlugin().getLogger().log(Level.WARNING, "Unable to initialize Score-Leaderboard - No display pages enabled! Check config for display-options.");
            return;
        }

        super.create(player);
    }

    @Override
    public boolean hasViewPermission(UUID uuid) {
        return true;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(BaseItems.LEADERBOARD_SCORE.getItem());
    }

    @Override
    public String getTitle(UUID playerUUID) {
        return "§b§lTOP SCORE §6§l[" + (sortByLeaderboard.toString().charAt(0) +
                sortByLeaderboard.toString().toLowerCase().substring(1)) + "]";
    }

    @Override
    public List<DataLine<?>> getHeader(UUID playerUUID) {
        sortByLeaderboard = LeaderboardTimeframe.valueOf(currentPage);
        return super.getHeader(playerUUID);
    }

    @Override
    public List<DataLine<?>> getContent(UUID playerUUID) {
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

    private Component getRankingString(Player player) {
        int position, rows, myScore;
        try {
            position = Builder.getBuilderScorePosition(player.getUniqueId(), sortByLeaderboard);
            rows = Builder.getBuildersInSort(sortByLeaderboard);
            myScore = Builder.getBuilderScore(player.getUniqueId(), sortByLeaderboard);
        } catch (SQLException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(Component.text("A SQL error occurred!"), ex);
            return Component.empty();
        }

        // Start building the component
        TextComponent.Builder builder = Component.text()
                .append(Component.text("  " + LangUtil.getInstance().get(player, sortByLeaderboard.langPath))
                        .color(NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD)
                )
                .append(Component.text(" ➜ ")
                        .color(NamedTextColor.DARK_GRAY)
                        .decorate(TextDecoration.BOLD)
                );

        if (position == -1) {
            builder.append(Component.text(LangUtil.getInstance().get(player, LangPaths.Leaderboards.NOT_ON_LEADERBOARD))
                    .color(NamedTextColor.RED)
                    .decoration(TextDecoration.BOLD, false)
            );
        } else if (position < 50) {
            builder.append(Component.text(
                            LangUtil.getInstance().get(player, LangPaths.Leaderboards.ACTIONBAR_POSITION, String.valueOf(position)))
                    .color(NamedTextColor.GREEN)
                    .decoration(TextDecoration.BOLD, false)
            );
        } else {
            String topPercentage = df.format(position * 1.0 / rows);
            builder.append(Component.text(
                            LangUtil.getInstance().get(player, LangPaths.Leaderboards.ACTIONBAR_PERCENTAGE, topPercentage))
                    .decoration(TextDecoration.BOLD, false)
            );
        }

        if (myScore != -1) {
            builder.append(
                    Component.text(" (", NamedTextColor.DARK_GRAY)
                            .append(Component.text(myScore + " points", NamedTextColor.AQUA))
                            .append(Component.text(")", NamedTextColor.DARK_GRAY))
            );
        }

        return builder.build();
    }

    private List<Player> getPlayersInRadiusForRanking() {
        FileConfiguration config = PlotSystem.getPlugin().getConfig();
        boolean actionBarEnabled = config.getBoolean(ConfigPaths.DISPLAY_OPTIONS_ACTION_BAR_ENABLE, true);
        int actionBarRadius = config.getInt(ConfigPaths.DISPLAY_OPTIONS_ACTION_BAR_RADIUS, 30);
        List<Player> players = new ArrayList<>();
        if (!actionBarEnabled) return players;
        for (Player player : Bukkit.getOnlinePlayers()) {
            Hologram holo = DHAPI.getHologram(player.getUniqueId() + "-" + getId());
            if (holo == null) continue;
            if (player.getWorld().getName().equals(holo.getLocation().getWorld().getName()) &&
                    holo.getLocation().distance(player.getLocation()) <= actionBarRadius) {
                players.add(player);
            }
        }
        return players;
    }

    @Override
    public long getInterval() {
        return PlotSystem.getPlugin().getConfig().getInt(ConfigPaths.DISPLAY_OPTIONS_INTERVAL) * 20L;
    }

    @Override
    public String getEnablePath() {
        return ConfigPaths.SCORE_LEADERBOARD_ENABLE;
    }

    @Override
    public String getXPath() {
        return ConfigPaths.SCORE_LEADERBOARD_X;
    }

    @Override
    public String getYPath() {
        return ConfigPaths.SCORE_LEADERBOARD_Y;
    }

    @Override
    public String getZPath() {
        return ConfigPaths.SCORE_LEADERBOARD_Z;
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

    private class LeaderboardPositionLineWithPayout extends HologramRegister.LeaderboardPositionLine {
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
                if (payout == null) return line;
                String payoutAmount = payout.getPayoutAmount();
                try {
                    // if payout amount can be number, prefix with dollar sign
                    Integer.valueOf(payoutAmount);
                    payoutAmount = "$" + payoutAmount;
                } catch (NumberFormatException ignored) {}

                return line + " §7- §e§l" + payoutAmount;
            } catch (SQLException e) {
                return super.getLine() + " §7- §cSQL ERR";
            }
        }
    }
}