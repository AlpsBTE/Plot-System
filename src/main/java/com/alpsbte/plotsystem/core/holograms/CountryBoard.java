package com.alpsbte.plotsystem.core.holograms;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.holograms.connector.DecentHologramPagedDisplay;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class CountryBoard extends DecentHologramPagedDisplay implements HologramConfiguration {
    private final DecimalFormat df = new DecimalFormat("#.##");
    private ArrayList<Builder.DatabaseEntry<
            Builder.DatabaseEntry<Integer, String>,
            Builder.DatabaseEntry<Integer, String>>> projectsData = null;
    private ArrayList<Builder.DatabaseEntry<Integer,
            ArrayList<Builder.DatabaseEntry<Integer,
            Builder.DatabaseEntry<String, String>>>>> plotByCities = null;
    private static final String contentSeparator = "§7------------------------";
    private int currentPage = 0;

    protected CountryBoard() {
        super( "country-board", null, false, PlotSystem.getPlugin());
        setEnabled(PlotSystem.getPlugin().getConfig().getBoolean(getEnablePath()));
        setLocation(HologramManager.getLocation(this));

        try {
            this.projectsData = Builder.getProjectsSorted();
            this.plotByCities = Builder.getPlotsSorted();
        }
        catch (SQLException ex) {
            PlotSystem.getPlugin().getLogger().log(Level.SEVERE, "An error occurred while reading country board content", ex);
        }
    }

    @Override
    public void create(Player player) {
        if (!PlotSystem.getPlugin().isEnabled()) return;
        if (getPages().isEmpty()) {
            PlotSystem.getPlugin().getLogger().log(Level.WARNING, "Unable to initialize Score-Leaderboard - No display pages enabled! Check config for display-options.");
            return;
        }
        super.setPageCount(projectsData != null? projectsData.size() : 1);
        super.create(player);
    }

    @Override
    public void reload(UUID playerUUID) {
        super.reload(playerUUID);

        Hologram holo = DHAPI.getHologram(playerUUID.toString() + "-" + "country-board");
        if (holo == null | projectsData == null) return;
        super.setClickListener((clickEvent) -> {
            int nextViewPage = currentPage + 1;
            if(nextViewPage == projectsData.size()) nextViewPage = 0;
            holo.show(holo.getViewerPlayers().get(0), nextViewPage);
            currentPage = nextViewPage;
            Bukkit.getLogger().log(Level.INFO, "Recieved Country Board Pages Event " + clickEvent.getClick() + "\nChanging to pahe " + nextViewPage);
        });
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Material.NETHER_STAR);
    }

    @Override
    public String getTitle(UUID playerUUID) {
        return "§7[§eID§7] - [§bStatus§7] - [§uDifficulty§7]";
    }

    @Override
    public List<DataLine<?>> getContent(UUID var1) {
        return null;
    }

    @Override
    public List<List<DataLine<?>>> getPagedHeader(UUID playerUUID) {
        List<List<DataLine<?>>> header = new ArrayList<>();
        if(projectsData == null) return super.getPagedHeader(playerUUID);

        for (Builder.DatabaseEntry<
                Builder.DatabaseEntry<Integer, String>,
                Builder.DatabaseEntry<Integer, String>> cityProject
                : projectsData
        ) {
            header.add(Arrays.asList(
                new ItemLine(this.getItem()),
                new TextLine("§b§lCOUNTRY BOARD §6§l"),
                new TextLine(contentSeparator),
                new TextLine("§6§l[" + cityProject.getKey().getValue() + "§7§l: §r§3" + cityProject.getValue().getValue() + "§6§l]"),
                new TextLine(contentSeparator),
                new TextLine(this.getTitle(playerUUID))
            ));
        }

        return header;
    }

    @Override
    public List<List<DataLine<?>>> getPagedFooter(UUID playerUUID) {
        return Collections.singletonList(Arrays.asList(
                new TextLine(contentSeparator),
                new TextLine("§6<<< click to swap pages >>>"),
                new TextLine(contentSeparator)
        ));
    }

    @Override
    public List<List<DataLine<?>>> getPagedContent(UUID playerUUID) {
        List<List<DataLine<?>>> content = new ArrayList<>();
        if(projectsData == null) return super.getPagedContent(playerUUID);

        for (Builder.DatabaseEntry<
                Builder.DatabaseEntry<Integer, String>,
                Builder.DatabaseEntry<Integer, String>> cityProject
                : projectsData
        ) { // For each registered city projects
            ArrayList<DataLine<?>> lines = new ArrayList<>();
            int index = 0;
            for (int i = 0; i < 10; i++) lines.add(new HologramRegister.CountryBoardPositionLine("&8#" + 0,null, null));

            plotEntry: // For each plotEntries in the city project
            for (Builder.DatabaseEntry<Integer,
                    ArrayList<Builder.DatabaseEntry<Integer,
                    Builder.DatabaseEntry<String, String>>>> plotEntries
                    : plotByCities)
            {
                if(!cityProject.getValue().getKey().equals(plotEntries.getKey())) continue;

                for(Map.Entry<Integer, Builder.DatabaseEntry<String, String>> plot : plotStatusSorter(plotEntries.getValue())) {
                    if(index >= 10) break plotEntry;

                    Status status = Status.valueOf(plot.getValue().getKey());
                    PlotDifficulty difficulty = PlotDifficulty.valueOf(plot.getValue().getValue());

                    String statusColor = plot.getValue().getKey();
                    switch (status) {
                        case unclaimed: statusColor = "&7" + statusColor + " "; break;
                        case unfinished: statusColor = "&6" + statusColor; break;
                        case unreviewed: statusColor = "&3" + statusColor; break;
                        case completed: statusColor = "&a" + statusColor + " "; break;
                    }
                    String difficultyColor = "";
                    switch (difficulty) {
                        case EASY: difficultyColor += "&a"; break;
                        case MEDIUM: difficultyColor += "&6"; break;
                        case HARD: difficultyColor += "&c"; break;
                    }
                    PositionLineWithSpacing innerLine = new PositionLineWithSpacing(this.getTitle(playerUUID),
                            "&e#" + plot.getKey(), // id
                            statusColor, // city status
                            difficultyColor + "[" + plot.getValue().getValue() + "]"); // difficulty

                    lines.set(index, new TextLine(innerLine.getLine())); // city difficulty
                    index++;
                }
            }
            content.add(lines);
        }


        return content;
    }

    private static ArrayList<Map.Entry<Integer, Builder.DatabaseEntry<String, String>>>
            plotStatusSorter(ArrayList<Builder.DatabaseEntry<Integer,
            Builder.DatabaseEntry<String, String>>> plotEntries)
    {
        // Prepare entries with Hashmap
        HashMap<Integer, Builder.DatabaseEntry<String, String>> entriesMap = new HashMap<>();
        for(Builder.DatabaseEntry<Integer, Builder.DatabaseEntry<String, String>> entry : plotEntries)
            entriesMap.put(entry.getKey(), entry.getValue());

        // Create an ArrayList and insert all hashmap key-value pairs.
        ArrayList<Map.Entry<Integer, Builder.DatabaseEntry<String, String>>> sortedEntries = new ArrayList<>(entriesMap.entrySet());

        // Sort the Arraylist using a custom comparator.
        sortedEntries.sort(new Comparator<Map.Entry<Integer, Builder.DatabaseEntry<String, String>>>() {
            @Override
            public int compare(Map.Entry<Integer, Builder.DatabaseEntry<String, String>> o1,
                               Map.Entry<Integer, Builder.DatabaseEntry<String, String>> o2) {
                Status status1 = Status.valueOf(o1.getValue().getKey());
                Status status2 = Status.valueOf(o2.getValue().getKey());

                if (o1.getValue() == o2.getValue())
                    return o1.getKey().compareTo(o2.getKey());

                return Integer.compare(status1.ordinal(), status2.ordinal());
            }
        });

        return sortedEntries;
    }

    private static class PositionLineWithSpacing extends HologramRegister.CountryBoardPositionLine {
        private final int width;

        public PositionLineWithSpacing(String spacing, String id, String status, String difficulty) {
            super(id, status, difficulty);
            String split = spacing.replaceAll("[§&][a-zA-Z0-9]", "");
            Bukkit.getLogger().log(Level.INFO, "Splited: >" + split);
            this.width = split.length();
        }

        public String getLine() {
            Bukkit.getLogger().log(Level.INFO, "Splited: " + super.getLine());
            String[] split = super.getLine().split("-");

            int spaceLeft = width - super.getLine().length();
            int difficultyText = split[split.length - 1].length();
            int difficultySpacing = 6 - difficultyText;
            if(difficultySpacing > 0) spaceLeft = spaceLeft - difficultySpacing;

            String result = "";
            for (String text : split) {
                if(text.equals(split[split.length - 2])) {
                    String frontSpacing = StringUtils.repeat(" ", spaceLeft);
                    String backSpacing = StringUtils.repeat(" ", difficultySpacing);
                    result = result.concat(text.concat(frontSpacing + "- " + backSpacing));
                }
                else if(text.equals(split[split.length - 1]))  result = result.concat(text);
                else result = result.concat(text.concat(" -"));
            }
            Bukkit.getLogger().log(Level.INFO, "Result: " + result);

            return result;
        }
    }

    @Override
    public List<String> getPages() {
        if (ConfigUtil.getInstance() == null) return new ArrayList<>();
        FileConfiguration config = PlotSystem.getPlugin().getConfig();
        return Arrays.stream(ScoreLeaderboard.LeaderboardTimeframe.values())
                .filter(p -> config.getBoolean(p.configPath)).map(ScoreLeaderboard.LeaderboardTimeframe::toString).collect(Collectors.toList());
    }

    @Override
    public long getInterval() {
        return PlotSystem.getPlugin().getConfig().getInt(ConfigPaths.DISPLAY_OPTIONS_INTERVAL) * 20L;
    }

    @Override
    public String getEnablePath() {
        return ConfigPaths.COUNTRY_BOARD_ENABLE;
    }

    @Override
    public String getXPath() {
        return ConfigPaths.COUNTRY_BOARD_X;
    }

    @Override
    public String getYPath() {
        return ConfigPaths.COUNTRY_BOARD_Y;
    }

    @Override
    public String getZPath() {
        return ConfigPaths.COUNTRY_BOARD_Z;
    }

    @Override
    public boolean hasViewPermission(UUID uuid) {
        return true;
    }
}

