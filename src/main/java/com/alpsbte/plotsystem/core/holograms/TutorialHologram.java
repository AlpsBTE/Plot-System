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

import com.alpsbte.alpslib.hologram.HolographicDisplay;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.plot.world.PlotWorld;
import me.filoghost.holographicdisplays.api.hologram.line.TextHologramLine;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TutorialHologram extends HolographicDisplay {
    @FunctionalInterface
    public interface FooterClickAction {
        void onClick(Player player);
    }

    private Material materialItem = Material.GRASS;
    private String title = "";
    private List<String> content = new ArrayList<>();
    private int hologramHeight = PlotWorld.MIN_WORLD_HEIGHT;
    private boolean showFooter;
    private int currentTask;
    private int totalTasks;

    public TutorialHologram(@NotNull String id) {
        super(id);
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
    public List<DataLine<?>> getHeader() {
        return Arrays.asList(
                new ItemLine(this.getItem()),
                new TextLine(this.getTitle()),
                new TextLine("{empty}")
        );
    }

    @Override
    public List<DataLine<?>> getContent() {
        List<DataLine<?>> lines = new ArrayList<>();
        content.forEach(line -> lines.add(new TextLine(line)));
        return lines;
    }

    @Override
    public List<DataLine<?>> getFooter() {
        return Arrays.asList(
               new TextLine("{empty}"),
               new TextLine(showFooter ? "§8§l[" + (currentTask >= totalTasks ?
                       "§e§lClick To Continue" : "§a§l" + currentTask + " §8§l/ §a§l" + totalTasks + " Tasks") + "§8§l]" : "{empty}")
        );
        // TODO: Add multi-language support
    }

    public void onFooterClickEvent(FooterClickAction action) {
        updateFooter(0, 0);
        updateFooter(true);
        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
            TextHologramLine line = (TextHologramLine) getHologram().getLines().get(getHologram().getLines().size() - 2);
            line.setClickListener((clickEvent) -> action.onClick(clickEvent.getPlayer()));
        });
    }

    private void moveHologram(int amountLines) {
        amountLines += getHeader().size();
        amountLines += getFooter().size();
        double height = 0;
        for (int i = 0; i < amountLines; i++) height += 0.25;

        Location location = getHologram().getPosition().toLocation();
        location.setY(hologramHeight + height);
        getHologram().setPosition(location);
    }

    public void setDefaultHologramHeight(int height) {
        hologramHeight = height;
    }

    public void updateHeader(Material materialItem, String title) {
        this.materialItem = materialItem;
        this.title = title;
        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> updateDataLines(0, getHeader()));
    }

    public void updateContent(List<String> content) {
        this.content = content;
        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
            moveHologram(content.size());
            reload();
        });
    }

    public void updateFooter(boolean showFooter) {
        this.showFooter = showFooter;
        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> updateDataLines(getHologram().getLines().size() - 2, getFooter()));
    }

    public void updateFooter(int currentTask, int totalTasks) {
        this.currentTask = currentTask;
        this.totalTasks = totalTasks;
        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> updateDataLines(getHologram().getLines().size() - 2, getFooter()));
    }
}
