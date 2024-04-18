package com.alpsbte.plotsystem.core.holograms;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.holograms.connector.DecentHologramPagedDisplay;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import it.unimi.dsi.fastutil.Hash;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Array;
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
            ArrayList<Builder.DatabaseEntry<Integer, String>>>> plotEntries = null;
    private static final String contentSeparator = "§7---------------";
    private int currentPage = 0;

    protected CountryBoard() {
        super( "country-board", null, false, PlotSystem.getPlugin());
        setEnabled(PlotSystem.getPlugin().getConfig().getBoolean(getEnablePath()));
        setLocation(HologramManager.getLocation(this));

        try {
            this.projectsData = Builder.getProjectsSorted();
            this.plotEntries = Builder.getPlotsSorted();
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
        return "§b§lCOUNTRY BOARD §6§l";
    }

    @Override
    public List<DataLine<?>> getContent(UUID var1) {
        return null;
    }

    @Override
    public List<List<DataLine<?>>> getPagedHeader(UUID playerUUID) {
        List<List<DataLine<?>>> header = new ArrayList<>();
        if(projectsData == null) super.getPagedHeader(playerUUID);
        for (Builder.DatabaseEntry<
                Builder.DatabaseEntry<Integer, String>,
                Builder.DatabaseEntry<Integer, String>> cityProject
                : projectsData
        ) {
            header.add(Arrays.asList(
                new ItemLine(this.getItem()),
                new TextLine(this.getTitle(playerUUID)),
                new TextLine("----- " + cityProject.getKey().getValue() + " -----")
            ));
        }


        return header;
    }

    @Override
    public List<List<DataLine<?>>>  getPagedContent(UUID playerUUID) {
        List<List<DataLine<?>>> content = new ArrayList<>();
        if(projectsData == null) super.getPagedHeader(playerUUID);
        for (Builder.DatabaseEntry<
                Builder.DatabaseEntry<Integer, String>,
                Builder.DatabaseEntry<Integer, String>> cityProject
                : projectsData
        ) {
            for (Builder.DatabaseEntry<Integer,
                    ArrayList<Builder.DatabaseEntry<Integer, String>>> plots : plotEntries)
            {
                Bukkit.getLogger().log(Level.INFO, "Looking for " + cityProject.getValue().getKey() + " " + plots.getKey());
                ArrayList<DataLine<?>> lines = new ArrayList<>();

                for (int i = 0; i < 10; i++) {
                    lines.add(new HologramRegister.CountryBoardPositionLine("§8#" + 0,null, null));
                }

                int index = 0;
                if(cityProject.getValue().getKey().equals(plots.getKey()))
                {
                    HashMap<Integer, Status> sortedEntries = new HashMap<>();

                    for(Builder.DatabaseEntry<Integer, String> entry : plots.getValue()) {
                        sortedEntries.put(entry.getKey(), Status.valueOf(entry.getValue()));
                        Bukkit.getLogger().log(Level.INFO, "Found " + entry.getKey() + " " + entry.getValue());
                    }

                    for(Map.Entry<Integer, Status> plot : plotStatusSorter(sortedEntries)) {
                        String statusText = plot.getValue().toString();
                        switch (plot.getValue()) {
                            case unclaimed: statusText = "&8" + statusText;
                            case unfinished: statusText = "&6" + statusText;
                            case unreviewed: statusText = "&3" + statusText;
                            case completed: statusText = "&a" + statusText;
                        }
                        lines.set(index, new HologramRegister.CountryBoardPositionLine("§e#" + plot.getKey(),
                                cityProject.getValue().getValue(), // city name
                                statusText)); // city status
                        index++;
                    }
                }
                content.add(lines);
            }

        }


        return content;
    }

    private static ArrayList<Map.Entry<Integer, Status>> plotStatusSorter(HashMap<Integer, Status> entries) {
        // Create an ArrayList and insert all hashmap key-value pairs.
        ArrayList<Map.Entry<Integer, Status>> sortEntries = new ArrayList<>(entries.entrySet());

        // Sort the Arraylist using a custom comparator.
        sortEntries.sort(new Comparator<Map.Entry<Integer, Status>>() {
            @Override
            public int compare(Map.Entry<Integer, Status> o1, Map.Entry<Integer, Status> o2) {
                if (o1.getValue() == o2.getValue())
                    return o1.getKey().compareTo(o2.getKey());

                return Integer.compare(o1.getValue().ordinal(), o2.getValue().ordinal());
            }
        });

        return sortEntries;

        // Create a LinkedHashMap.
        // Map sortedMap = new LinkedHashMap<>();

        // Iterate over the ArrayList and insert the key-value pairs into LinkedHashMap.
//        for (int i = 0; i hashmap){
//            for (Map.Entry entry : hashmap.entrySet()) {
//                System.out.println("Key : " + entry.getKey() + " \t  value :  " + entry.getValue());
//            }
//        }
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
