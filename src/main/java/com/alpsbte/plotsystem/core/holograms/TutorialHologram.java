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
import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TutorialHologram extends HolographicPagedDisplay {
    private static final int MAX_LINES_PAGE = 48;
    private static final String LINE_BREAKER = "%newline%";

    private Material materialItem = Material.GRASS;
    private String title = "";
    private List<String> pages = new ArrayList<>();
    private long interval = 0;

    public TutorialHologram(@NotNull String id, @NotNull Plugin plugin) {
        super(id, plugin);
        automaticallySkipPage = false;
    }

    @Override
    public long getInterval() {
        return interval * 20L;
    }

    @Override
    public List<String> getPages() {
        return pages;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(materialItem);
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public List<DataLine<?>> getContent() {
        List<DataLine<?>> lines = new ArrayList<>();
        AlpsUtils.createMultilineFromString(sortByPage, MAX_LINES_PAGE, LINE_BREAKER).forEach(line -> lines.add(new TextLine(line)));
        return lines;
    }

    public void updateInterval(long interval) {
        this.interval = interval;
    }

    public void updateStage(Material materialItem, String title, List<String> pages) {
        this.materialItem = materialItem;
        this.title = title;
        this.pages = pages;
        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> updateDataLines(0, getHeader()));
    }
}
