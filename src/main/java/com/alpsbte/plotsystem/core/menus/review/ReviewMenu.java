package com.alpsbte.plotsystem.core.menus.review;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LegacyLoreBuilder;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.menus.AbstractPaginatedMenu;
import com.alpsbte.plotsystem.core.menus.PlotActionsMenu;
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
import org.jetbrains.annotations.NotNull;

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
        cityProjects = DataProvider.BUILD_TEAM.getReviewerCities(getMenuPlayer().getUniqueId());
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
                .pattern(Utils.FULL_MASK)
                .pattern(Utils.EMPTY_MASK)
                .pattern(Utils.EMPTY_MASK)
                .pattern(Utils.EMPTY_MASK)
                .pattern(Utils.EMPTY_MASK)
                .pattern("111101111")
                .build();
    }

    private List<Plot> getFilteredPlots(@NotNull List<?> plots) {
        List<Plot> filteredPlots = plots.stream().map(p -> (Plot) p).toList();
        if (filteredCityProject != null)
            filteredPlots = filteredPlots.stream().filter(p -> p.getCityProject().getID().equals(filteredCityProject.getID())).toList();
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
