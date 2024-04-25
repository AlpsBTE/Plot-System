package com.alpsbte.plotsystem.core.holograms;

import com.aseanbte.aseanlib.hologram.DecentHologramPagedDisplay;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder.DatabaseEntry;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;

import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import static com.alpsbte.plotsystem.core.system.Builder.getPlotsSorted;
import static com.alpsbte.plotsystem.core.system.Builder.getProjectsSorted;

public class CountryBoard extends DecentHologramPagedDisplay implements HologramConfiguration {
    private ArrayList<DatabaseEntry<
                DatabaseEntry<Integer, String>,
                DatabaseEntry<Integer, String>>> projectsData = null;
    private ArrayList<DatabaseEntry<Integer,
            ArrayList<DatabaseEntry<Integer,
            DatabaseEntry<String, String>>>>> plotByCities = null;
    private static final String contentSeparator = "§7------------------------";
    private static final String id = "country-board";

    protected CountryBoard() {
        super(id, null, false);
        setEnabled(PlotSystem.getPlugin().getConfig().getBoolean(getEnablePath()));
        setLocation(HologramManager.getLocation(this));

        try {
            this.projectsData = getProjectsSorted();
            this.plotByCities = getPlotsSorted();
        }
        catch (SQLException ex) {
            PlotSystem.getPlugin().getLogger().log(Level.SEVERE, "An error occurred while reading country board content", ex);
        }
    }

    @Override
    public void create(Player player) {
        if (!PlotSystem.getPlugin().isEnabled()) return;
        super.setPageCount(projectsData != null? projectsData.size() : 1);
        super.create(player);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Material.NETHER_STAR);
    }

    @Override
    public String getTitle(UUID playerUUID) {
        String id = "§7[§e" + LangUtil.getInstance().get(playerUUID, LangPaths.CountryBoard.HEADER_ID);
        String status = "[§b" + LangUtil.getInstance().get(playerUUID, LangPaths.CountryBoard.HEADER_STATUS);
        String difficulty = "[§d" + LangUtil.getInstance().get(playerUUID, LangPaths.CountryBoard.HEADER_DIFFICULTY);
        return String.join("§7] - ", id, status, difficulty + "§7]");
    }

    @Override
    public List<DataLine<?>> getContent(UUID var1) {
        return null;
    }

    @Override
    public List<List<DataLine<?>>> getPagedHeader(UUID playerUUID) {
        List<List<DataLine<?>>> header = new ArrayList<>();
        if(projectsData == null) return super.getPagedHeader(playerUUID);
        String title = LangUtil.getInstance().get(playerUUID, LangPaths.CountryBoard.TITLE);

        for (DatabaseEntry<
            DatabaseEntry<Integer, String>,
            DatabaseEntry<Integer, String>> cityProject
            : projectsData
        ) {
            header.add(Arrays.asList(
                new ItemLine(this.getItem()),
                new TextLine("§b§l" + title),
                new TextLine(contentSeparator),
                new TextLine("§6§l[§c§l" + cityProject.getKey().getValue() + "§7§l: §r§3" + cityProject.getValue().getValue() + "§6§l]"),
                new TextLine(contentSeparator),
                new TextLine(this.getTitle(playerUUID))
            ));
        }

        return header;
    }

    @Override
    public List<List<DataLine<?>>> getPagedFooter(UUID playerUUID) {
        String footer = LangUtil.getInstance().get(playerUUID, LangPaths.CountryBoard.FOOTER);
        return Collections.singletonList(Arrays.asList(
                new TextLine(contentSeparator),
                new TextLine(String.join(" ", "§6<<<", footer, ">>>")),
                new TextLine(contentSeparator)
        ));
    }

    @Override
    public List<List<DataLine<?>>> getPagedContent(UUID playerUUID) {
        List<List<DataLine<?>>> content = new ArrayList<>();
        if(projectsData == null) return super.getPagedContent(playerUUID);

        for (DatabaseEntry<
                DatabaseEntry<Integer, String>,
                DatabaseEntry<Integer, String>> cityProject
                : projectsData
        ) { // For each registered city projects
            ArrayList<DataLine<?>> lines = new ArrayList<>();
            int index = 0;
            for (int i = 0; i < 10; i++)
                lines.add(new HologramRegister.CountryBoardPositionLine("&8#0",null, null));

            plotEntry: // For each plotEntries in the city project
            for (DatabaseEntry<Integer,
                    ArrayList<DatabaseEntry<Integer,
                    DatabaseEntry<String, String>>>> plotEntries
                    : plotByCities)
            {
                if(!cityProject.getValue().getKey().equals(plotEntries.getKey())) continue;

                ArrayList<DatabaseEntry<Integer,
                    DatabaseEntry<String, String>>> sortedPlotEntries = plotStatusSorter(plotEntries.getValue());

                for(DatabaseEntry<Integer, DatabaseEntry<String, String>> plot : sortedPlotEntries) {
                    if(index >= 10) break plotEntry;

                    // Convert string value from database back to enum
                    Status status = Status.valueOf(plot.getValue().getKey());
                    PlotDifficulty difficulty = PlotDifficulty.valueOf(plot.getValue().getValue());

                    // Calculate ID digits spacing, ex. "#7 " and "#17"
                    int maxDigit = sortedPlotEntries.get(sortedPlotEntries.size() - 1).getKey().toString().length();
                    int digits = maxDigit - plot.getKey().toString().length();
                    String digitsSpacing = digits > 0 ? String.join("", Collections.nCopies(digits, " ")) : "";

                    // Assign display color
                    String idText = "&e#" + plot.getKey() + digitsSpacing;
                    String statusText = plot.getValue().getKey();
                    String difficultyText = plot.getValue().getValue();

                    switch (status) {
                        case unclaimed: statusText = "&7" + statusText + " "; break;
                        case unfinished: statusText = "&6" + statusText; break;
                        case unreviewed: statusText = "&3" + statusText; break;
                        case completed: statusText = "&a" + statusText + " "; break;
                    }
                    switch (difficulty) {
                        case EASY: difficultyText = "&a[" + difficultyText + "]"; break;
                        case MEDIUM: difficultyText = "&6" + difficultyText; break;
                        case HARD: difficultyText = "&c[" + difficultyText + "]"; break;
                    }

                    // Push everything to our builder
                    HologramRegister.CountryBoardPositionLine innerLine = new HologramRegister
                        .CountryBoardPositionLine(idText, statusText, difficultyText);
                    lines.set(index, new TextLine(innerLine.getLine()));

                    index++;
                }
            }
            content.add(lines);
        }

        return content;
    }

    /**
     * Sort plot entries from database so that unclaimed plot will always be listed first
     * @param plotEntries entries list for every plot ID
     * @return Sorted entries by Status ENUM (unclaimed, unfinished, unreviewed, completed)
     */
    private static ArrayList<DatabaseEntry<Integer, DatabaseEntry<String, String>>>
            plotStatusSorter(ArrayList<DatabaseEntry<Integer,
            DatabaseEntry<String, String>>> plotEntries)
    {
         plotEntries.sort((o1, o2) -> {
            Status status1 = Status.valueOf(o1.getValue().getKey());
            Status status2 = Status.valueOf(o2.getValue().getKey());

            // If the status state is the same, sort with plot id
            if (status1.ordinal() == status2.ordinal())
                return o1.getKey().compareTo(o2.getKey());

            // Else sort by status state
            return Integer.compare(status1.ordinal(), status2.ordinal());
        });
        return plotEntries;
    }

    @Override
    public long getInterval() {
        return PlotSystem.getPlugin().getConfig().getInt(ConfigPaths.DISPLAY_OPTIONS_INTERVAL) * 20L;
    }
    @Override
    public String getEnablePath() { return ConfigPaths.COUNTRY_BOARD_ENABLE; }
    @Override
    public String getXPath() { return ConfigPaths.COUNTRY_BOARD_X; }
    @Override
    public String getYPath() { return ConfigPaths.COUNTRY_BOARD_Y; }
    @Override
    public String getZPath() { return ConfigPaths.COUNTRY_BOARD_Z; }
    @Override
    public boolean hasViewPermission(UUID uuid) { return true; }
}

