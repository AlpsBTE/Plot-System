package com.alpsbte.plotsystem.core.menus.review;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.menus.AbstractPaginatedMenu;
import com.alpsbte.plotsystem.core.menus.PlotActionsMenu;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.BaseItems;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

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

        // Get plots based on city filter
        if (filteredCityProject != null) {
            plots.addAll(DataProvider.PLOT.getPlots(List.of(filteredCityProject), Status.unreviewed));
            plots.addAll(DataProvider.PLOT.getPlots(List.of(filteredCityProject), Status.unfinished));
        } else {
            plots.addAll(DataProvider.PLOT.getPlots(cityProjects, Status.unreviewed));
            plots.addAll(DataProvider.PLOT.getPlots(cityProjects, Status.unfinished));
        }

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
            LoreBuilder loreBuilder = new LoreBuilder()
                    .addLine(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.ID) + ": ", GRAY).append(text(plot.getId(), WHITE)))
                    .emptyLine()
                    .addLine(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.OWNER) + ": ", GRAY).append(text(plot.getPlotOwner().getName(), WHITE)));


            if (!plot.getPlotMembers().isEmpty()) {
                loreBuilder.addLine(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.MEMBERS) + ": ", GRAY)
                        .append(text(plot.getPlotMembers().stream().map(Builder::getName).collect(Collectors.joining(", ")), WHITE)));
            }

            loreBuilder
                    .addLine(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.CITY) + ": ", GRAY)
                            .append(text(plot.getCityProject().getName(getMenuPlayer()), WHITE)))
                    .addLine(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.COUNTRY) + ": ", GRAY)
                            .append(text(plot.getCityProject().getCountry().getName(getMenuPlayer()), WHITE)))
                    .addLine(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.DIFFICULTY) + ": ", GRAY)
                            .append(text(plot.getDifficulty().name().charAt(0) + plot.getDifficulty().name().substring(1).toLowerCase(), WHITE)));

            if (plot.getStatus() == Status.unfinished) {
                long inactivityIntervalDays = PlotSystem.getPlugin().getConfig().getLong(ConfigPaths.INACTIVITY_INTERVAL);
                long rejectedInactivityIntervalDays = (PlotSystem.getPlugin().getConfig().getLong(ConfigPaths.REJECTED_INACTIVITY_INTERVAL) != -1) ? PlotSystem.getPlugin().getConfig().getLong(ConfigPaths.REJECTED_INACTIVITY_INTERVAL) : inactivityIntervalDays;
                long interval = plot.isRejected() ? rejectedInactivityIntervalDays : inactivityIntervalDays;
                if (interval > -1) {
                    loreBuilder
                            .emptyLine()
                            .addLine(text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.ABANDONED_IN_DAYS, String.valueOf(DAYS.between(LocalDate.now(), plot.getLastActivity().plusDays(interval)))), GRAY));
                }

            }

            getMenu().getSlot(i + 9).setItem(new ItemBuilder(plot.getStatus() == Status.unfinished ? Material.MAP : Material.FILLED_MAP, 1)
                    .setName(text(LangUtil.getInstance().get(getMenuPlayer(), plot.getStatus() == Status.unfinished ? LangPaths.Review.MANAGE_PLOT : LangPaths.Review.REVIEW_PLOT), AQUA, BOLD))
                    .setLore(loreBuilder.build())
                    .build());
        }

        // Set previous page item
        getMenu().getSlot(46).setItem(hasPreviousPage() ? MenuItems.previousPageItem(getMenuPlayer()) : MenuItems.borderItem());

        // Set next page item
        getMenu().getSlot(52).setItem(hasNextPage() ? MenuItems.nextPageItem(getMenuPlayer()) : MenuItems.borderItem());
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

                player.performCommand("review " + plot.getId());
            });
        }

        // Set click event for previous page item
        if (hasPreviousPage()) {
            getMenu().getSlot(46).setClickHandler((clickPlayer, clickInformation) -> {
                previousPage();
                clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.INVENTORY_CLICK_SOUND, 1, 1);
            });
        }

        // Set click event for next page item
        if (hasNextPage()) {
            getMenu().getSlot(52).setClickHandler((clickPlayer, clickInformation) -> {
                nextPage();
                clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.INVENTORY_CLICK_SOUND, 1, 1);
            });
        }
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

        // Set click event for close item
        getMenu().getSlot(49).setClickHandler((clickPlayer, clickInformation) -> clickPlayer.closeInventory());
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(MenuItems.borderItem())
                .pattern(Utils.FULL_MASK)
                .pattern(Utils.EMPTY_MASK)
                .pattern(Utils.EMPTY_MASK)
                .pattern(Utils.EMPTY_MASK)
                .pattern(Utils.EMPTY_MASK)
                .pattern("111101111")
                .build();
    }

    private List<Plot> getFilteredPlots(@NotNull List<?> plots) {
        return plots.stream().map(p -> (Plot) p).toList();
    }

    private ItemStack getFilterItem(Player langPlayer) {
        LoreBuilder loreBuilder = new LoreBuilder();
        loreBuilder.addLine(filteredCityProject == null
                ? text("> ", AQUA).decoration(BOLD, true)
                  .append(text(LangUtil.getInstance().get(langPlayer, LangPaths.MenuDescription.FILTER), WHITE).decoration(BOLD, true))
                : text(LangUtil.getInstance().get(langPlayer, LangPaths.MenuDescription.FILTER), GRAY));
        loreBuilder.emptyLine();

        cityProjects.forEach(c -> {
            if (filteredCityProject != null && filteredCityProject.getId().equals(c.getId())) {
                loreBuilder.addLine(text("> ", AQUA).decoration(BOLD, true)
                        .append(text(filteredCityProject.getName(langPlayer), WHITE).decoration(BOLD, true)));
            } else {
                loreBuilder.addLine(text(c.getName(langPlayer), GRAY));
            }
        });

        return new ItemBuilder(MenuItems.filterItem(getMenuPlayer()))
                .setLore(loreBuilder.build())
                .setEnchanted(filteredCityProject != null)
                .build();
    }

    /**
     * @return Menu item
     */
    public static ItemStack getMenuItem(Player player) {
        return new ItemBuilder(BaseItems.REVIEW_ITEM.getItem())
                .setName(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.REVIEW_PLOTS), AQUA, BOLD)
                        .append(text(" (" + LangUtil.getInstance().get(player, LangPaths.Note.Action.RIGHT_CLICK) + ")", GRAY)))
                .setEnchanted(true)
                .build();
    }
}
