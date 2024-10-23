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

import com.alpsbte.plotsystem.PlotSystem;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.event.HologramClickEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Collections;

import static net.kyori.adventure.text.Component.text;

/**
 * Extended class to create paged hologram display
 */
public abstract class DecentHologramPagedDisplay extends DecentHologramDisplay {
    public BukkitTask changePageTask = null;
    private final Plugin plugin;
    private int pageCount = 1;
    private int currentPage = 0;
    private int changeState = 0;
    private long changeDelay = 0;
    protected boolean automaticallySkipPage;

    /**
     * @param id        Hologram identifier for creating DecentHologram name, this will later be concatenated as "${player.uuid}-${id}"
     * @param location  The location in a world to create hologram.
     * @param isEnabled Force enable or disable this hologram on create, this will not register new hologram in the hashmap.
     * @param plugin    Assign a plugin reference and this hologram pages will be automatically turns by a fixed interval.
     */
    public DecentHologramPagedDisplay(@NotNull String id, Location location, boolean isEnabled, @NotNull Plugin plugin) {
        super(id, location, isEnabled);
        this.plugin = plugin;
        this.automaticallySkipPage = true;
    }

    /**
     * @param id        Hologram identifier for creating DecentHologram name, this will later be concatenated as "${player.uuid}-${id}"
     * @param location  The location in a world to create hologram.
     * @param isEnabled Force enable or disable this hologram on create, this will not register new hologram in the hashmap.
     */
    public DecentHologramPagedDisplay(@NotNull String id, Location location, boolean isEnabled) {
        super(id, location, isEnabled);
        this.plugin = null;
        this.automaticallySkipPage = false;
    }

    /**
     * Set a number of pages in this hologram.
     *
     * @param pageCount Pages size, this should be in sync with how many member page contents has.
     */
    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    @Override
    public void create(Player player) {
        if (!super.isEnabled() | !this.hasViewPermission(player.getUniqueId())) return;
        if (this.holograms.containsKey(player.getUniqueId())) this.reload(player.getUniqueId());
        else {
            Hologram hologram = DHAPI.createHologram(player.getUniqueId() + "-" + super.getId(), super.getLocation());
            for (int i = 1; i <= pageCount; i++) hologram.addPage();

            // Allow only player to see
            hologram.setDefaultVisibleState(false);
            hologram.setShowPlayer(player);

            // Put new hologram to hashmap
            this.holograms.put(player.getUniqueId(), hologram);

            // Write hologram data-lines and assign click listener
            this.reload(player.getUniqueId());
            if (!automaticallySkipPage) super.setClickListener(this::assignClickListener);
            else startChangePageTask(player, hologram);
        }

    }


    @Override
    public void reload(UUID playerUUID) {
        if (!holograms.containsKey(playerUUID)) return;

        List<List<DataLine<?>>> header = getPagedHeader(playerUUID);
        List<List<DataLine<?>>> content = getPagedContent(playerUUID);
        List<List<DataLine<?>>> footer = getPagedFooter(playerUUID);

        for (int i = 0; i < pageCount; i++) {

            List<DataLine<?>> dataLines = new ArrayList<>();

            if (header != null && header.size() > 1) dataLines.addAll(header.get(i));
            else if (header != null && header.size() == 1) dataLines.addAll(header.get(0));

            if (content != null && content.size() > 1) dataLines.addAll(content.get(i));
            else if (content != null && content.size() == 1) dataLines.addAll(content.get(0));

            if (footer != null && footer.size() > 1) dataLines.addAll(footer.get(i));
            else if (footer != null && footer.size() == 1) dataLines.addAll(footer.get(0));

            updateDataLines(holograms.get(playerUUID).getPage(i), 0, dataLines);
        }

    }

    /**
     * getHeader as paged list, the list indexing will be mapped to hologram page one by one.<br/>
     * If the parent list only have one index, will use the index for every pageCount.
     *
     * @param playerUUID Focused player.
     * @return Lists of DataLine, with a parent List being a page indexing.
     */
    public List<List<DataLine<?>>> getPagedHeader(UUID playerUUID) {
        return new ArrayList<>(Collections.singletonList(getHeader(playerUUID)));
    }

    /**
     * getContent as paged list, the list indexing will be mapped to hologram page one by one.<br/>
     * If the parent list only have one index, will use the index for every pageCount.
     *
     * @param playerUUID Focused player.
     * @return Lists of DataLine, with a parent List being a page indexing.
     */
    public List<List<DataLine<?>>> getPagedContent(UUID playerUUID) {
        return new ArrayList<>(Collections.singletonList(getContent(playerUUID)));
    }

    /**
     * getFooter as paged list, the list indexing will be mapped to hologram page one by one.<br/>
     * If the parent list only have one index, will use the index for every pageCount.
     *
     * @param playerUUID Focused player.
     * @return Lists of DataLine, with a parent List being a page indexing.
     */
    public List<List<DataLine<?>>> getPagedFooter(UUID playerUUID) {
        return new ArrayList<>(Collections.singletonList(getFooter(playerUUID)));
    }

    /**
     * Override this function to implement click action of the hologram,
     * defaulted to calling next page.
     *
     * @param event Click event callback
     * @see HologramClickEvent
     */
    public void assignClickListener(HologramClickEvent event) {
        nextPage(event.getPlayer(), event.getHologram());
    }

    /**
     * Recursively change pages of this hologram by a fixed interval forever.
     *
     * @param player   Focused player.
     * @param hologram Focused hologram.
     */
    private void startChangePageTask(Player player, Hologram hologram) {
        // Fixed interval value
        final long interval = getInterval();
        changeDelay = interval / contentSeparator.length();
        changeState = 0;

        // Cancel if no page, and cancel previous task to start anew
        if (pageCount <= 1 | plugin == null) return;
        if (changePageTask != null) changePageTask.cancel();

        // Count every 1 seconds, if reaches a fixed interval, recursive call a new task.
        changePageTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (interval == 0) return;
                if (changeState >= changeDelay) {
                    if (automaticallySkipPage) {
                        nextPage(player, hologram);
                        startChangePageTask(player, hologram);
                    } else changePageTask.cancel();
                } else {
                    onTick(player, hologram);
                    changeState++;
                }

            }
        }.runTaskTimer(plugin, 0, changeDelay);
    }

    /**
     * If provided plugin reference, override this method to set interval value in milliseconds.
     *
     * @return Plugin's check interval for when turning to next page automatically.
     */
    public long getInterval() {return 15 * 20L;}

    /**
     * If provided plugin reference, override this method to create any animation to hologram each tick of one second.
     * This is defaulted to footer color changing animation as a tick increased.
     *
     * @param player   Focused player.
     * @param hologram Focused hologram.
     */
    public void onTick(Player player, Hologram hologram) {
        int footerLength = contentSeparator.length();
        int highlightCount = (int) (((float) changeState / changeDelay) * footerLength);

        StringBuilder highlighted = new StringBuilder();
        for (int i = 0; i < highlightCount; i++) {
            highlighted.append("-");
        }

        StringBuilder notH = new StringBuilder();
        for (int i = 0; i < footerLength - highlightCount; i++) {
            notH.append("-");
        }

        replaceLine(hologram.getPage(player),
                hologram.getPage(player).size() - 1,
                "§e" + highlighted + "§7" + notH);
    }

    /**
     * Turn a new page to player.<br/>
     * Note that currently there's no previous page function,
     * calling this beyond page size will loop back to the first page.
     *
     * @param player   Focused player.
     * @param hologram Focused hologram.
     * @see Hologram#show(Player, int)
     */
    public void nextPage(Player player, Hologram hologram) {
        try {
            int nextViewPage = currentPage + 1;
            if (nextViewPage == pageCount) nextViewPage = 0;
            hologram.show(player, nextViewPage);
            currentPage = nextViewPage;
        } catch (IndexOutOfBoundsException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("[DHAPI] Hologram page indexing out of bounds"), ex);
        }
    }
}