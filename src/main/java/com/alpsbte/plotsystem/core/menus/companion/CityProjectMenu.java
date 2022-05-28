package com.alpsbte.plotsystem.core.menus.companion;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.menus.AbstractPaginatedMenu;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.core.system.plot.generator.DefaultPlotGenerator;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.config.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.language.LangPaths;
import com.alpsbte.plotsystem.utils.io.language.LangUtil;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import com.alpsbte.plotsystem.utils.items.builder.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class CityProjectMenu extends AbstractPaginatedMenu {
    Country country;
    List<CityProject> projects;
    private PlotDifficulty selectedPlotDifficulty = null;

    CityProjectMenu(Player player, Country country, PlotDifficulty selectedPlotDifficulty) {
        super(6, 4, country.getName() + " -> §nSelect City", player);
        this.country = country;
        this.selectedPlotDifficulty = selectedPlotDifficulty;
    }

    @Override
    protected void setPreviewItems() {
        // add difficulty switcher

        getMenu().getSlot(4).setItem(new ItemBuilder(Utils.getItemHead(new Utils.CustomHead("37793"))).setName("Back to countries").build());

        for (Map.Entry<Integer, CompanionMenu.FooterItem> entry : CompanionMenu.getFooterItems(45, getMenuPlayer(), player -> {
            player.closeInventory();
            new CountryMenu(player, country.getContinent());
        }).entrySet()) {
            getMenu().getSlot(entry.getKey()).setItem(entry.getValue().item);
        }

        // Set loading item for plots difficulty item
        getMenu().getSlot(7).setItem(MenuItems.loadingItem(Material.SKULL_ITEM, (byte) 3, getMenuPlayer()));

        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {
        // Set previous page item
        if (hasPreviousPage()) {
            getMenu().getSlot(45).setItem(MenuItems.previousPageItem(getMenuPlayer()));
        } else {
            getMenu().getSlot(45).setItem(new ItemBuilder(Utils.getItemHead(new Utils.CustomHead("9248"))).setName("No Previous Page").build());
        }

        // Set next page item
        if (hasNextPage()) {
            getMenu().getSlot(53).setItem(MenuItems.nextPageItem(getMenuPlayer()));
        } else {
            getMenu().getSlot(53).setItem(new ItemBuilder(Utils.getItemHead(new Utils.CustomHead("9248"))).setName("No Next Page").build());
        }

        // difficulty selector
        getMenu().getSlot(7).setItem(CompanionMenu.getDifficultyItem(getMenuPlayer(), selectedPlotDifficulty));
    }

    @Override
    protected void setItemClickEvents() {
        getMenu().getSlot(4).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            new CountryMenu(clickPlayer, country.getContinent(), selectedPlotDifficulty);
        });

        // Set click event for previous page item
        getMenu().getSlot(45).setClickHandler((clickPlayer, clickInformation) -> {
            if (hasPreviousPage()) {
                previousPage();
                clickPlayer.playSound(clickPlayer.getLocation(), Utils.INVENTORY_CLICK, 1, 1);
            }
        });

        // Set click event for next page item
        getMenu().getSlot(53).setClickHandler((clickPlayer, clickInformation) -> {
            if (hasNextPage()) {
                nextPage();
                clickPlayer.playSound(clickPlayer.getLocation(), Utils.INVENTORY_CLICK, 1, 1);
            }
        });

        for (Map.Entry<Integer, CompanionMenu.FooterItem> entry : CompanionMenu.getFooterItems(45, getMenuPlayer(), player -> {
            player.closeInventory();
            new CountryMenu(player, country.getContinent());
        }).entrySet()) {
            getMenu().getSlot(entry.getKey()).setClickHandler(entry.getValue().clickHandler);
        }

        // Set click event for plots difficulty item
        getMenu().getSlot(7).setClickHandler(((clickPlayer, clickInformation) -> {
            selectedPlotDifficulty = (selectedPlotDifficulty == null ?
                    PlotDifficulty.values()[0] : selectedPlotDifficulty.ordinal() != PlotDifficulty.values().length - 1 ?
                    PlotDifficulty.values()[selectedPlotDifficulty.ordinal() + 1] : null);

            getMenu().getSlot(7).setItem(CompanionMenu.getDifficultyItem(getMenuPlayer(), selectedPlotDifficulty));
            clickPlayer.playSound(clickPlayer.getLocation(), Utils.Done, 1, 1);

            // reload displayed cities
            reloadMenuAsync();
        }));
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("111101101")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000010000")
                .build();
    }

    @Override
    protected List<?> getSource() {
        if(projects == null) {
            projects = CityProject.getCityProjects(country, true);
        }
        return projects;
    }

    @Override
    protected void setPaginatedMenuItemsAsync(List<?> source) {
        List<CityProject> cities = source.stream().map(l -> (CityProject) l).collect(Collectors.toList());
        int slot = 9;
        for (CityProject city : cities) {
            try {
                getMenu().getSlot(slot).setItem(city.getItem(getMenuPlayer(), null));
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", e);
                getMenu().getSlot(slot).setItem(MenuItems.errorItem(getMenuPlayer()));
            }
            slot++;
        }
    }

    @Override
    protected void setPaginatedItemClickEventsAsync(List<?> source) {
        List<CityProject> cities = source.stream().map(l -> (CityProject) l).collect(Collectors.toList());
        int slot = 9;
        for(CityProject city : cities) {
            final int _slot = slot;
            getMenu().getSlot(_slot).setClickHandler((clickPlayer, clickInformation) -> {
                if (!getMenu().getSlot(_slot).getItem(clickPlayer).equals(MenuItems.errorItem(getMenuPlayer()))) {
                    try {
                        clickPlayer.closeInventory();
                        Builder builder = new Builder(clickPlayer.getUniqueId());
                        int cityID = city.getID();

                        PlotDifficulty plotDifficultyForCity = selectedPlotDifficulty != null ? selectedPlotDifficulty : PlotManager.getPlotDifficultyForBuilder(cityID, builder).get();
                        if (PlotManager.getPlots(cityID, plotDifficultyForCity, Status.unclaimed).size() != 0) {
                            if (selectedPlotDifficulty != null && PlotSystem.getPlugin().getConfigManager().getConfig().getBoolean(ConfigPaths.ENABLE_SCORE_REQUIREMENT) && !PlotManager.hasPlotDifficultyScoreRequirement(builder, selectedPlotDifficulty)) {
                                clickPlayer.sendMessage(Utils.getErrorMessageFormat(LangUtil.get(clickPlayer, LangPaths.Message.Error.PLAYER_NEEDS_HIGHER_SCORE)));
                                clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                                return;
                            }

                            new DefaultPlotGenerator(cityID, plotDifficultyForCity, builder);
                        } else {
                            clickPlayer.sendMessage(Utils.getErrorMessageFormat(LangUtil.get(clickPlayer, LangPaths.Message.Error.NO_PLOTS_LEFT)));
                            clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                        }
                    } catch (SQLException | ExecutionException | InterruptedException ex) {
                        Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                        clickPlayer.sendMessage(Utils.getErrorMessageFormat(LangUtil.get(clickPlayer, LangPaths.Message.Error.ERROR_OCCURRED)));
                        clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                    }
                } else {
                    clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                }
            });
            slot++;
        }
    }
}
