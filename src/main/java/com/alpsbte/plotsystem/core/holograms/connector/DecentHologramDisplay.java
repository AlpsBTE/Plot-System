package com.alpsbte.plotsystem.core.holograms.connector;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

// import com.alpsbte.alpslib.hologram.HolographicEventListener;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorialHologram;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.DecentHolograms;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.DecentHologramsAPI;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import eu.decentsoftware.holograms.api.holograms.HologramManager;

import eu.decentsoftware.holograms.api.holograms.HologramPage;


//import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
// import me.filoghost.holographicdisplays.api.Position;
//import me.filoghost.holographicdisplays.api.hologram.PlaceholderSetting;
//import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings.Visibility;
//import me.filoghost.holographicdisplays.api.hologram.line.HologramLine;
//import me.filoghost.holographicdisplays.api.hologram.line.ItemHologramLine;
//import me.filoghost.holographicdisplays.api.hologram.line.TextHologramLine;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DecentHologramDisplay implements DecentHologramContent {
    public static List<DecentHologramDisplay> activeDisplays = new ArrayList<>();
    public static DecentHolograms decentHolograms;
    public static String contentSeparator = "§7---------------";
    public static final String EMPTY_TAG = "&f";
    private final String id;
    private Location position;
    private boolean isEnabled;
    protected final HashMap<UUID, Hologram> holograms = new HashMap<>();
    private AbstractTutorialHologram.ClickAction clickListener;

    public static void registerPlugin(Plugin plugin) {
        decentHolograms = DecentHologramsAPI.get();
        plugin.getServer().getPluginManager().registerEvents(new DecentHologramListener(), plugin);
    }

    public DecentHologramDisplay(@NotNull String id, Location position, boolean isEnabled) {
        this.id = id;
        this.position = position;
        this.isEnabled = isEnabled;
        activeDisplays.add(this);
    }

    public void create(Player player) {
        if(!isEnabled) return;
        if (this.hasViewPermission(player.getUniqueId())) {
            if (this.holograms.containsKey(player.getUniqueId())) {
                this.reload(player.getUniqueId());
            } else {
                HologramManager hologramManager = decentHolograms.getHologramManager();
                hologramManager.updateVisibility(player);

                Hologram hologram = DHAPI.createHologram(player.getUniqueId() + "-" + id, position);
                // Allow only player to see
                hologram.setDefaultVisibleState(false);
                hologram.setShowPlayer(player);
                Bukkit.getLogger().log(Level.INFO, "Creating a hologram: " + hologram + " of hologram: " + hologramManager.getHologram(player.getUniqueId().toString()));

                this.holograms.put(player.getUniqueId(), hologram);
                this.reload(player.getUniqueId());
            }
        }
    }

    public abstract boolean hasViewPermission(UUID var1);

    public boolean isVisible(UUID playerUUID) {
        return this.holograms.containsKey(playerUUID);
    }

    public List<DataLine<?>> getHeader(UUID playerUUID) {
        return Arrays.asList(new ItemLine(this.getItem()), new TextLine(this.getTitle(playerUUID)), new TextLine(contentSeparator));
    }

    public List<DataLine<?>> getFooter(UUID playerUUID) {
        return Collections.singletonList(new TextLine(contentSeparator));
    }

    public void reload(UUID playerUUID) {
        if (!holograms.containsKey(playerUUID)) return;
        List<DataLine<?>> dataLines = new ArrayList<>();

        List<DataLine<?>> header = getHeader(playerUUID);
        if (header != null) dataLines.addAll(header);

        List<DataLine<?>> content = getContent(playerUUID);
        if (content != null) dataLines.addAll(content);

        List<DataLine<?>> footer = getFooter(playerUUID);
        if (footer != null) dataLines.addAll(footer);

        updateDataLines(holograms.get(playerUUID).getPage(0), 0, dataLines);
    }

    public void reloadAll() {
        for (UUID playerUUID : holograms.keySet()) reload(playerUUID);
    }

    public void remove(UUID playerUUID) {
        if (this.holograms.containsKey(playerUUID)) {
            DHAPI.removeHologram(playerUUID + "-" + id);
            this.holograms.get(playerUUID).delete();
        }

        this.holograms.remove(playerUUID);
    }

    public void removeAll() {
        List<UUID> playerUUIDs = new ArrayList<>(holograms.keySet());
        for (UUID playerUUID : playerUUIDs) remove(playerUUID);
    }

    public void delete() {
        this.removeAll();
        this.holograms.clear();
        activeDisplays.remove(this);
    }

    public String getId() {
        return this.id;
    }

    public Location getLocation() {
        return this.position;
    }

    public void setLocation(Location newPosition) {
        this.position = newPosition;
        for (UUID playerUUID : holograms.keySet()) holograms.get(playerUUID).setLocation(newPosition);

    }

    public boolean isEnabled() {
        return this.isEnabled;
    }
    public void setEnabled(boolean isEnabled) { this.isEnabled = isEnabled; }

    public Hologram getHologram(UUID playerUUID) {
        return this.holograms.get(playerUUID);
    }

    public HashMap<UUID, Hologram> getHolograms() {
        return this.holograms;
    }

    protected static void updateDataLines(HologramPage page, int startIndex, List<DataLine<?>> dataLines) {
        int index = startIndex;
        Bukkit.getLogger().log(Level.INFO, "Updating dataline of: " + page + " with: " + dataLines);
        Bukkit.getLogger().log(Level.INFO, "Looking for pages: " + page + " of line: " + page.getLines());

        if (index == 0 && page.getLines().size() > dataLines.size()) {
            int removeCount = page.getLines().size() - dataLines.size();

            for(int i = 0; i < removeCount; ++i) {
                int lineIndex = page.getLines().size() - 1;
                if (lineIndex >= 0) {
                    page.getLines().remove(lineIndex);
                }
            }
        }

        for (DataLine<?> data : dataLines) {
            if (data instanceof TextLine) replaceLine(page, index, ((TextLine) data).getLine());
            else if (data instanceof ItemLine) replaceLine(page, index, ((ItemLine) data).getLine());
            index++;
        }

    }

    protected static void replaceLine(HologramPage page, int line, ItemStack item) {
        Bukkit.getLogger().log(Level.INFO, "Replacing data item of: " + page.getLines().size() + " with: " + item);

        if (page.getLines().size() < line + 1) {
            Bukkit.getLogger().log(Level.INFO, "Inserting item 1: " + line);
            DHAPI.addHologramLine(page, item);
        } else {
            Bukkit.getLogger().log(Level.INFO, "Inserting item 2: " + item);
            HologramLine hologramLine = page.getLines().get(line);
            if (hologramLine != null) {

                DHAPI.insertHologramLine(page, line, item);
                DHAPI.removeHologramLine(page, line + 1);
            } else {
                Bukkit.getLogger().log(Level.INFO, "Inserting item 3: " + line);
                DHAPI.setHologramLine(page, line,  item);

            }
        }
    }

    protected static void replaceLine(HologramPage page, int line, String text) {
        Bukkit.getLogger().log(Level.INFO, "Replacing dataline of: " + page.getLines().size() + " with: " + text);


        if (page.getLines().size() < line + 1) {
            Bukkit.getLogger().log(Level.INFO, "Inserting 1: " + line);
            DHAPI.addHologramLine(page, text);
        } else {
            Bukkit.getLogger().log(Level.INFO, "Inserting 2: " + line);
            HologramLine hologramLine = page.getLines().get(line);
            if (hologramLine != null) {

                DHAPI.insertHologramLine(page, line, text);
                DHAPI.removeHologramLine(page, line + 1);
            } else {
                Bukkit.getLogger().log(Level.INFO, "Inserting 3: " + line);
                DHAPI.setHologramLine(page, line,  text);
            }
        }

    }

    public void setClickListener(@Nullable AbstractTutorialHologram.ClickAction clickListener) {
        this.clickListener = clickListener;
    }

    public @Nullable AbstractTutorialHologram.ClickAction getClickListender() {
        return this.clickListener;
    }


    public static DecentHologramDisplay getById(String id) {
        return (DecentHologramDisplay) activeDisplays.stream().filter((holo) -> {
            return holo.getId().equals(id);
        }).findFirst().orElse((DecentHologramDisplay)null);
    }

    public interface DataLine<T> {
        T getLine();
    }

    public static class ItemLine implements DataLine<ItemStack> {
        private final ItemStack line;

        public ItemLine(ItemStack line) {
            this.line = line;
        }

        public ItemStack getLine() {
            return this.line;
        }
    }

    public static class TextLine implements DataLine<String> {
        private final String line;

        public TextLine(String line) {
            this.line = line;
        }

        public String getLine() {
            return this.line;
        }
    }
}

