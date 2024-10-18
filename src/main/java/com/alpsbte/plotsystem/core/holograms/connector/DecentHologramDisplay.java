/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, ASEAN Build The Earth <bteasean@gmail.com>
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

package com.alpsbte.plotsystem.core.holograms.connector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import eu.decentsoftware.holograms.event.HologramClickEvent;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapper class to use DecentHologram API, this creates and manage hologram per player.
 * Meaning that individual player can see the hologram personally in multiplayer world.
 */
public abstract class DecentHologramDisplay implements DecentHologramContent {
    public static List<DecentHologramDisplay> activeDisplays = new ArrayList<>();
    protected final HashMap<UUID, Hologram> holograms = new HashMap<>();
    public static final String contentSeparator = "§7---------------";
    private ClickAction clickListener;
    private final String id;
    private Location location;
    private boolean isEnabled;

    /**
     * Click action which is executed when the player clicks on the hologram on the hologram.
     */
    @FunctionalInterface
    public interface ClickAction {
        void onClick(@NotNull HologramClickEvent clickEvent);
    }

    /**
     * Register hologram listener as DecentHologramListener Class.
     *
     * @param plugin Plugin in use of this library.
     * @see DecentHologramDisplay#registerPlugin(Plugin)
     */
    public static void registerPlugin(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new DecentHologramListener(), plugin);
    }

    /**
     * @param id        Hologram identifier for creating DecentHologram name, this will later be concatenated as "${player.uuid}-${id}"
     * @param location  The location in a world to create hologram.
     * @param isEnabled Force enable or disable this hologram on create, this will not register new hologram in the hashmap.
     */
    public DecentHologramDisplay(@NotNull String id, Location location, boolean isEnabled) {
        this.id = id;
        this.location = location;
        this.isEnabled = isEnabled;
        activeDisplays.add(this);
    }

    /**
     * Create hologram for player to see.
     * If the player has correct ViewPermission and hologram is configured enabled,
     * the hologram will be created
     *
     * @param player The player that will be able to view this hologram
     */
    public void create(Player player) {
        if (!isEnabled | !this.hasViewPermission(player.getUniqueId())) return;
        if (this.holograms.containsKey(player.getUniqueId())) {
            this.reload(player.getUniqueId());
        } else {
            Bukkit.getConsoleSender().sendMessage("[DHAPI] Created display ID: " + id + " For player: " + player.getUniqueId());
            Hologram hologram = DHAPI.createHologram(player.getUniqueId() + "-" + id, location);

            // Exclude all player to view this then allow only player to see
            hologram.setDefaultVisibleState(false);
            hologram.setShowPlayer(player);

            this.holograms.put(player.getUniqueId(), hologram);
            this.reload(player.getUniqueId());
        }
    }

    /**
     * Abstract method to add functionality to whether a player creating this hologram can view it or not.
     *
     * @param playerUUID Focused player.
     * @return If true, the hologram will be created for player. Else not.
     */
    public abstract boolean hasViewPermission(UUID playerUUID);

    /**
     * Check if a player can view this hologram.
     *
     * @param playerUUID Focused player.
     * @return True if hologram is visible to player.
     */
    public boolean isVisible(UUID playerUUID) {
        return this.holograms.containsKey(playerUUID);
    }

    public List<DataLine<?>> getHeader(UUID playerUUID) {
        return Arrays.asList(new ItemLine(this.getItem()), new TextLine(this.getTitle(playerUUID)), new TextLine(contentSeparator));
    }

    public List<DataLine<?>> getFooter(UUID playerUUID) {
        return Collections.singletonList(new TextLine(contentSeparator));
    }

    /**
     * Re-Write hologram dataLines received by getHeader(), getContent() and getFooter() in order.
     *
     * @param playerUUID Focused player.
     */
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

    /**
     * Call the reload method on all holograms.
     */
    public void reloadAll() {
        for (UUID playerUUID : holograms.keySet()) reload(playerUUID);
    }

    /**
     * Remove a hologram assigned to this display from player access.
     *
     * @param playerUUID Focused player.
     */
    public void remove(UUID playerUUID) {
        if (this.holograms.containsKey(playerUUID)) {
            DHAPI.removeHologram(playerUUID + "-" + id);
            this.holograms.get(playerUUID).delete();
        }

        this.holograms.remove(playerUUID);
    }

    /**
     * Remove all assigned hologram this display has,
     * this still does not remove the display itself
     */
    public void removeAll() {
        List<UUID> playerUUIDs = new ArrayList<>(holograms.keySet());
        for (UUID playerUUID : playerUUIDs) remove(playerUUID);
    }

    /**
     * Delete this display entirely
     */
    public void delete() {
        this.removeAll();
        this.holograms.clear();
        activeDisplays.remove(this);
    }

    /**
     * Get the identifier from when this hologram is first constructed.
     *
     * @return ID as String.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the hologram location.
     *
     * @return hologram location.
     */
    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location newPosition) {
        this.location = newPosition;
        for (UUID playerUUID : holograms.keySet()) holograms.get(playerUUID).setLocation(newPosition);

    }

    /**
     * Is this hologram enabled.
     *
     * @return Is enabled.
     */
    public boolean isEnabled() {
        return this.isEnabled;
    }

    /**
     * Force set the hologram enabled or disabled.
     *
     * @param isEnabled If false, the hologram will not be created in any way.
     */
    public void setEnabled(boolean isEnabled) {this.isEnabled = isEnabled;}

    /**
     * Get hologram instance mapped in this display.
     *
     * @param playerUUID Focused player.
     * @return Hologram object from DecentHologram plugin.
     */
    public Hologram getHologram(UUID playerUUID) {
        return this.holograms.get(playerUUID);
    }

    /**
     * Get all the mapped hologram in this display.
     *
     * @return HashMap of Hologram by a player UUID.
     */
    public HashMap<UUID, Hologram> getHolograms() {
        return this.holograms;
    }

    /**
     * Write a hologram page by any DataLine of type
     *
     * @param page       The hologram page to write
     * @param startIndex Start line to update
     * @param dataLines  The data item to be write on the page
     */
    protected static void updateDataLines(HologramPage page, int startIndex, List<DataLine<?>> dataLines) {
        int index = startIndex;
        if (index == 0 && page.getLines().size() > dataLines.size()) {
            int removeCount = page.getLines().size() - dataLines.size();

            for (int i = 0; i < removeCount; ++i) {
                int lineIndex = page.getLines().size() - 1;
                if (lineIndex >= 0) {
                    DHAPI.removeHologramLine(page, lineIndex);
                }
            }
        }

        for (DataLine<?> data : dataLines) {
            if (data instanceof TextLine) replaceLine(page, index, ((TextLine) data).getLine());
            else if (data instanceof ItemLine) replaceLine(page, index, ((ItemLine) data).getLine());
            index++;
        }

    }

    /**
     * Write an index line of a hologram page
     *
     * @param page The hologram page to write
     * @param line The index line to write to
     * @param item any minecraft item as an ItemStack to be inserted
     */
    protected static void replaceLine(HologramPage page, int line, ItemStack item) {
        try {
            if (page.getLines().size() < line + 1) {
                DHAPI.addHologramLine(page, item);
            } else {
                HologramLine hologramLine = page.getLines().get(line);
                DHAPI.setHologramLine(hologramLine, item);
            }
        } catch (IllegalArgumentException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "[DHAPI] Trying to set invalid HologramLine ", ex);
        }
    }

    /**
     * Write an index line of a hologram page
     *
     * @param page The hologram page to write
     * @param line The index line to write to
     * @param text The text to be written on
     */
    protected static void replaceLine(HologramPage page, int line, String text) {
        try {
            if (page.getLines().size() < line + 1) {
                DHAPI.addHologramLine(page, text);
            } else {
                HologramLine hologramLine = page.getLines().get(line);
                DHAPI.setHologramLine(hologramLine, text);
            }
        } catch (IllegalArgumentException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "[DHAPI] Trying to set invalid HologramLine", ex);
        }
    }

    /**
     * Assign a click listener to this hologram, a ClickAction will be call when anyone clicks the hologram.
     * This should be called right when reloading the hologram
     * <pre>{@code
     * @Override
     * public void reload(UUID playerUUID) {
     *     super.reload(playerUUID);
     *     super.setClickListener((clickEvent) -> {});
     * }}
     * </pre>
     *
     * @param clickListener Action callback.
     */
    public void setClickListener(@Nullable ClickAction clickListener) {
        this.clickListener = clickListener;
    }

    public @Nullable ClickAction getClickListener() {
        return this.clickListener;
    }

    /**
     * Get activeDisplays in DecentHologramDisplay by ID
     *
     * @param id The id first used to construct a new DecentHologramDisplay
     * @return The hologram assigned to id.
     */
    public static DecentHologramDisplay getById(String id) {
        return activeDisplays.stream().filter(holo -> holo.getId().equals(id)).findFirst().orElse(null);
    }

    /**
     * DataLine of any type, acts as a text lines per hologram page.
     *
     * @param <T> DataLine type, this can only be ItemStack as ItemLine or String as TextLine
     */
    public interface DataLine<T> {
        T getLine();
    }

    /**
     * Minecraft ItemStack as DataLine
     */
    public static class ItemLine implements DataLine<ItemStack> {
        private final ItemStack line;

        public ItemLine(ItemStack line) {
            this.line = line;
        }

        public ItemStack getLine() {
            return this.line;
        }
    }

    /**
     * String as DataLine
     */
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