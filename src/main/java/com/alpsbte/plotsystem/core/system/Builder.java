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

package com.alpsbte.plotsystem.core.system;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.data.DataException;
import com.alpsbte.plotsystem.core.holograms.ScoreLeaderboard;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotType;
import com.alpsbte.plotsystem.utils.enums.Slot;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.*;

public class Builder {
    public static final HashMap<UUID, Builder> builders = new HashMap<>();

    private final UUID uuid;
    public PlotType plotType;

    private Builder(UUID UUID) {
        this.uuid = UUID;
    }

    public static Builder byUUID(UUID uuid) {
        if (builders.containsKey(uuid))
            return builders.get(uuid);

        Builder builder = new Builder(uuid);
        builders.put(uuid, builder);

        return builders.get(uuid);
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public java.util.UUID getUUID() {
        return uuid;
    }

    public boolean isOnline() {return Bukkit.getPlayer(uuid) != null;}

    public String getName() throws DataException {
        return PlotSystem.getDataProvider().getBuilderProvider().getName(uuid);
    }

    public int getScore() throws DataException {
        return PlotSystem.getDataProvider().getBuilderProvider().getScore(uuid);
    }

    public int getCompletedBuilds() throws DataException {
        return PlotSystem.getDataProvider().getBuilderProvider().getCompletedBuildsCount(uuid);
    }

    public Slot getFreeSlot() throws DataException {
        return PlotSystem.getDataProvider().getBuilderProvider().getFreeSlot(uuid);
    }

    public Plot getPlot(Slot slot) throws DataException {
        return PlotSystem.getDataProvider().getBuilderProvider().getPlot(uuid, slot);
    }

    public ItemStack getPlotMenuItem(Plot plot, int slotIndex, Player langPlayer) throws SQLException {
        String nameText = LangUtil.getInstance().get(getPlayer(), LangPaths.MenuTitle.SLOT).toUpperCase() + " " + (slotIndex + 1);
        Component statusComp = Component.text(LangUtil.getInstance().get(langPlayer, LangPaths.Plot.STATUS), NamedTextColor.GOLD).decoration(TextDecoration.BOLD, true);
        Component slotDescriptionComp = Component.text(LangUtil.getInstance().get(langPlayer, LangPaths.MenuDescription.SLOT), NamedTextColor.GRAY);

        Material itemMaterial = Material.MAP;
        ArrayList<Component> lore = new LoreBuilder()
                .addLines(slotDescriptionComp,
                        Component.empty(),
                        statusComp.append(Component.text(": Unassigned", NamedTextColor.GRAY)).decoration(TextDecoration.BOLD, true))
                .build();

        if (plot != null) {
            itemMaterial = Material.FILLED_MAP;
            String plotIdText = LangUtil.getInstance().get(langPlayer, LangPaths.Plot.ID);
            String plotCityText = LangUtil.getInstance().get(langPlayer, LangPaths.Plot.CITY);
            String plotDifficultyText = LangUtil.getInstance().get(langPlayer, LangPaths.Plot.DIFFICULTY);
            lore = new LoreBuilder()
                    .addLines(Component.text(plotIdText + ": ", NamedTextColor.GRAY).append(Component.text(plot.getID(), NamedTextColor.WHITE)),
                            Component.text(plotCityText + ": ", NamedTextColor.GRAY).append(Component.text(plot.getCity().getName(), NamedTextColor.WHITE)),
                            Component.text(plotDifficultyText + ": ", NamedTextColor.GRAY).append(Component.text(plot.getDifficulty().name().charAt(0) + plot.getDifficulty().name().substring(1).toLowerCase(), NamedTextColor.WHITE)),
                            Component.empty(),
                            statusComp.append(Component.text(": Unassigned", NamedTextColor.GRAY)).decoration(TextDecoration.BOLD, true))
                    .build();
        }

        return new ItemBuilder(itemMaterial, 1 + slotIndex)
                .setName(Component.text(nameText, NamedTextColor.GOLD).decoration(TextDecoration.BOLD, true))
                .setLore(lore)
                .build();
    }

    public void addScore(int score) throws DataException {
        PlotSystem.getDataProvider().getBuilderProvider().addScore(uuid, score);
    }

    public void addCompletedBuild(int amount) throws DataException {
        PlotSystem.getDataProvider().getBuilderProvider().addCompletedBuild(uuid, amount);
    }

    public void setPlot(int plotID, Slot slot) throws DataException {
        PlotSystem.getDataProvider().getBuilderProvider().setPlot(uuid, plotID, slot);
    }

    public void removePlot(Slot slot) throws DataException {
        PlotSystem.getDataProvider().getBuilderProvider().removePlot(uuid, slot);
    }

    public static Builder getBuilderByName(String name) throws DataException {
        return PlotSystem.getDataProvider().getBuilderProvider().getBuilderByName(name);
    }

    public static int getBuilderScore(UUID uuid, ScoreLeaderboard.LeaderboardTimeframe sortBy) throws DataException {
        return PlotSystem.getDataProvider().getBuilderProvider().getLeaderboardScore(uuid, sortBy);
    }

    public static int getBuilderScorePosition(UUID uuid, ScoreLeaderboard.LeaderboardTimeframe sortBy) throws DataException {
        return PlotSystem.getDataProvider().getBuilderProvider().getLeaderboardPosition(uuid, sortBy);
    }

    public static int getBuildersInSort(ScoreLeaderboard.LeaderboardTimeframe sortBy) throws DataException {
        return PlotSystem.getDataProvider().getBuilderProvider().getLeaderboardEntryCount(sortBy);
    }

    public static HashMap<String, Integer> getBuildersByScore(ScoreLeaderboard.LeaderboardTimeframe sortBy) throws DataException {
        return PlotSystem.getDataProvider().getBuilderProvider().getLeaderboardEntriesByScore(sortBy);
    }

    public static HashMap<String, Integer> getBuildersByCompletedBuilds(int limit) throws DataException {
        return PlotSystem.getDataProvider().getBuilderProvider().getLeaderboardEntriesByCompletedBuilds(limit);
    }

    public Slot getSlot(Plot plot) throws DataException {
        for (Slot slot : Slot.values()) {
            Plot slotPlot = getPlot(slot);
            if (slotPlot != null && slotPlot.getID() == plot.getID()) {
                return slot;
            }
        }
        return null;
    }

    public PlotType getPlotTypeSetting() {
        if (plotType != null)
            return plotType;
        this.plotType = PlotSystem.getDataProvider().getBuilderProvider().getPlotTypeSetting(uuid);
        return plotType;
    }

    public void setPlotTypeSetting(PlotType plotType) {
        PlotSystem.getDataProvider().getBuilderProvider().setPlotTypeSetting(uuid, plotType);
        this.plotType = plotType;
    }

    public Reviewer getAsReviewer() throws SQLException {
        return new Reviewer(getUUID());
    }

    public boolean isReviewer() throws DataException {
        return PlotSystem.getDataProvider().getBuilderProvider().isReviewer(uuid);
    }

    public static class Reviewer {
        private final List<BuildTeam> buildTeams;
        public Reviewer(UUID reviewerUUID) throws SQLException {
            this.buildTeams = BuildTeam.getBuildTeamsByReviewer(reviewerUUID);
        }

        public List<Country> getCountries() {
            return PlotSystem.getDataProvider().getBuilderProvider().getReviewerCountries(buildTeams);
        }
    }
}
