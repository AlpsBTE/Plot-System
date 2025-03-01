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

package com.alpsbte.plotsystem.core.menus;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LegacyLoreBuilder;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.BaseItems;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReviewMenu extends AbstractPaginatedMenu {
    private List<CityProject> cityProjects = new ArrayList<>();
    private CityProject filteredCityProject = null;

    public ReviewMenu(Player player) {
        super(6, 4, LangUtil.getInstance().get(player, LangPaths.Review.MANAGE_AND_REVIEW_PLOTS), player);
    }

    @Override
    protected List<?> getSource() {
        List<Plot> plots = new ArrayList<>();
        cityProjects = DataProvider.BUILD_TEAM.getReviewerCities(Builder.byUUID(getMenuPlayer().getUniqueId()));
        plots.addAll(DataProvider.PLOT.getPlots(cityProjects, Status.unreviewed));
        plots.addAll(DataProvider.PLOT.getPlots(cityProjects, Status.unfinished));
        return plots;
    }

    @Override
    protected void setPreviewItems() {
        // Set close item
        getMenu().getSlot(49).setItem(MenuItems.closeMenuItem(getMenuPlayer()));

        super.setPreviewItems();
    }

    @Override
    protected void setPaginatedMenuItemsAsync(List<?> source) {
        // Set filter item
        getMenu().getSlot(7).setItem(getFilterItem(getMenuPlayer()));

        // Set unreviewed and unfinished plot items
        List<Plot> plots = getFilteredPlots(source);
        for (int i = 0; i < plots.size(); i++) {
            Plot plot = plots.get(i);
            List<String> lines = new ArrayList<>();
            lines.add("§7" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.ID) + ": §f" + plot.getID());
            lines.add("");
            lines.add("§7" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.OWNER) + ": §f" + plot.getPlotOwner().getName());
            if (!plot.getPlotMembers().isEmpty()) {
                lines.add("§7" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.MEMBERS) + ": §f" + plot.getPlotMembers().stream().map(Builder::getName).collect(Collectors.joining(", ")));
            }
            lines.add("§7" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.CITY) + ": §f" + plot.getCityProject().getName(getMenuPlayer()));
            lines.add("§7" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.COUNTRY) + ": §f" + plot.getCityProject().getCountry().getName(getMenuPlayer()));
            lines.add("§7" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.DIFFICULTY) + ": §f" + plot.getDifficulty().name().charAt(0) + plot.getDifficulty().name().substring(1).toLowerCase());

            getMenu().getSlot(i + 9).setItem(new ItemBuilder(plot.getStatus() == Status.unfinished ? Material.MAP : Material.FILLED_MAP, 1)
                    .setName("§b§l" + LangUtil.getInstance().get(getMenuPlayer(), plot.getStatus() == Status.unfinished ? LangPaths.Review.MANAGE_PLOT : LangPaths.Review.REVIEW_PLOT))
                    .setLore(lines)
                    .build());
        }
    }

    @Override
    protected void setPaginatedItemClickEventsAsync(List<?> source) {
        // Set click event for unreviewed and unfinished plot items
        List<Plot> plots = getFilteredPlots(source);
        for (int i = 0; i < plots.size(); i++) {
            Plot plot = plots.get(i);
            getMenu().getSlot(i + 9).setClickHandler((player, info) -> {
                if (plot.getStatus() == Status.unfinished) {
                    new PlotActionsMenu(getMenuPlayer(), plot);
                    return;
                }

                player.performCommand("review " + plot.getID());
            });
        }
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set previous page item
        if (hasPreviousPage()) getMenu().getSlot(46).setItem(MenuItems.previousPageItem(getMenuPlayer()));

        // Set next page item
        if (hasNextPage()) getMenu().getSlot(52).setItem(MenuItems.nextPageItem(getMenuPlayer()));
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click event for filter item
        getMenu().getSlot(7).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.INVENTORY_CLICK_SOUND, 1, 1);
            if (cityProjects.isEmpty()) return;

            if (filteredCityProject == null) {
                filteredCityProject = cityProjects.getFirst();
            } else {
                int index = cityProjects.indexOf(filteredCityProject);
                filteredCityProject = index + 1 >= cityProjects.size() ? null : cityProjects.get(index + 1);
            }

            reloadMenuAsync(false);
        });

        // Set click event for previous page item
        getMenu().getSlot(46).setClickHandler((clickPlayer, clickInformation) -> {
            if (hasPreviousPage()) {
                previousPage();
                clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.INVENTORY_CLICK_SOUND, 1, 1);
            }
        });

        // Set click event for close item
        getMenu().getSlot(49).setClickHandler((clickPlayer, clickInformation) -> clickPlayer.closeInventory());

        // Set click event for next page item
        getMenu().getSlot(52).setClickHandler((clickPlayer, clickInformation) -> {
            if (hasNextPage()) {
                nextPage();
                clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.INVENTORY_CLICK_SOUND, 1, 1);
            }
        });
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1).setName(Component.empty()).build())
                .pattern("111111111")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("111101111")
                .build();
    }

    private List<Plot> getFilteredPlots(List<?> plots) {
        List<Plot> filteredPlots = plots.stream().map(p -> (Plot) p).collect(Collectors.toList());
        if (filteredCityProject != null)
            filteredPlots = filteredPlots.stream().filter(p -> p.getCityProject().getID().equals(filteredCityProject.getID())).collect(Collectors.toList());
        return filteredPlots;
    }

    private ItemStack getFilterItem(Player langPlayer) {
        LegacyLoreBuilder LegacyLoreBuilder = new LegacyLoreBuilder();
        LegacyLoreBuilder.addLine((filteredCityProject == null ? "§b§l> §f§l" : "§7") + LangUtil.getInstance().get(langPlayer, LangPaths.MenuDescription.FILTER));
        LegacyLoreBuilder.emptyLine();

        cityProjects.forEach(c -> {
            if (filteredCityProject != null && filteredCityProject.getID().equals(c.getID())) {
                LegacyLoreBuilder.addLine("§b§l> §f§l" + filteredCityProject.getName(langPlayer));
            } else LegacyLoreBuilder.addLine("§7" + c.getName(langPlayer));
        });

        return new ItemBuilder(MenuItems.filterItem(getMenuPlayer()))
                .setLore(LegacyLoreBuilder.build())
                .setEnchanted(filteredCityProject != null)
                .build();
    }

    /**
     * @return Menu item
     */
    public static ItemStack getMenuItem(Player player) {
        return new ItemBuilder(BaseItems.REVIEW_ITEM.getItem())
                .setName("§b§l" + LangUtil.getInstance().get(player, LangPaths.MenuTitle.REVIEW_PLOTS) + " §7(" + LangUtil.getInstance().get(player, LangPaths.Note.Action.RIGHT_CLICK) + ")")
                .setEnchanted(true)
                .build();
    }
}
