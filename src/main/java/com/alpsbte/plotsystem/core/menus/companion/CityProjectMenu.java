package com.alpsbte.plotsystem.core.menus.companion;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DataProvider;
import com.alpsbte.plotsystem.core.menus.AbstractPaginatedMenu;
import com.alpsbte.plotsystem.core.menus.tutorial.TutorialsMenu;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotHandler;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.alpsbte.plotsystem.utils.io.ConfigPaths;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CityProjectMenu extends AbstractPaginatedMenu {
    final Country country;
    List<CityProject> projects;
    private PlotDifficulty selectedPlotDifficulty;

    CityProjectMenu(Player player, Country country, PlotDifficulty selectedPlotDifficulty) {
        super(6, 4, country.getName(player) + " → " + LangUtil.getInstance().get(player, LangPaths.MenuTitle.COMPANION_SELECT_CITY), player);
        this.country = country;
        this.selectedPlotDifficulty = selectedPlotDifficulty;
    }

    @Override
    protected void setPreviewItems() {
        getMenu().getSlot(0).setItem(MenuItems.getRandomItem(getMenuPlayer())); // Set random selection item
        getMenu().getSlot(1).setItem(MenuItems.backMenuItem(getMenuPlayer()));

        Map<Integer, FooterItem> footerItems = CompanionMenu.getFooterItems(45, getMenuPlayer(), player -> new CountryMenu(player, country.getContinent()));
        footerItems.forEach((index, footerItem) -> getMenu().getSlot(index).setItem(footerItem.item));

        // Set loading item for plots difficulty item
        getMenu().getSlot(6).setItem(CompanionMenu.getDifficultyItem(getMenuPlayer(), selectedPlotDifficulty));

        // Set tutorial item
        getMenu().getSlot(7).setItem(PlotSystem.getPlugin().getConfig().getBoolean(ConfigPaths.TUTORIAL_ENABLE) ?
                TutorialsMenu.getTutorialItem(getMenuPlayer()) : new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1).setName(Component.empty()).build());

        // Set previous page item
        if (hasPreviousPage())
            getMenu().getSlot(45).setItem(MenuItems.previousPageItem(getMenuPlayer()));

        // Set next page item
        if (hasNextPage())
            getMenu().getSlot(53).setItem(MenuItems.nextPageItem(getMenuPlayer()));

        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {}

    @Override
    protected void setItemClickEventsAsync() {
        getMenu().getSlot(0).setClickHandler((clickPlayer, clickInformation) ->
                generateRandomPlot(clickPlayer, getValidCityProjects(selectedPlotDifficulty, clickPlayer, country), selectedPlotDifficulty));

        getMenu().getSlot(1).setClickHandler((clickPlayer, clickInformation) -> new CountryMenu(clickPlayer, country.getContinent(), selectedPlotDifficulty));

        // Set click event for previous page item
        getMenu().getSlot(45).setClickHandler((clickPlayer, clickInformation) -> {
            if (hasPreviousPage()) {
                previousPage();
                clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.INVENTORY_CLICK_SOUND, 1, 1);
            }
        });

        // Set click event for next page item
        getMenu().getSlot(53).setClickHandler((clickPlayer, clickInformation) -> {
            if (!hasNextPage()) return;
            nextPage();
            clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.INVENTORY_CLICK_SOUND, 1, 1);
        });

        Map<Integer, FooterItem> footerItems = CompanionMenu.getFooterItems(45, getMenuPlayer(), player -> new CountryMenu(player, country.getContinent()));
        footerItems.forEach((index, footerItem) -> getMenu().getSlot(index).setClickHandler(footerItem.clickHandler));

        // Set click event for plots difficulty item
        getMenu().getSlot(6).setClickHandler(((clickPlayer, clickInformation) -> {
            selectedPlotDifficulty = CompanionMenu.clickEventPlotDifficulty(selectedPlotDifficulty, clickPlayer, getMenu());
            reloadMenuAsync(); // reload displayed cities
        }));

        CompanionMenu.clickEventTutorialItem(getMenu());
    }

    public void handleCityProjectClick(Player player, CityProject city) {
        Builder builder = Builder.byUUID(player.getUniqueId());

        PlotDifficulty plotDifficultyForCity;
        try {
            plotDifficultyForCity = selectedPlotDifficulty != null ? selectedPlotDifficulty : Plot.getPlotDifficultyForBuilder(city, builder).get();
        } catch (ExecutionException | InterruptedException ex) {
            sqlError(player, ex);
            return;
        }

        List<Plot> unclaimedPlots = DataProvider.PLOT.getPlots(city, plotDifficultyForCity, Status.unclaimed);
        if (unclaimedPlots.isEmpty()) {
            player.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(player, LangPaths.Message.Error.NO_PLOTS_LEFT)));
            Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> player.playSound(player.getLocation(), Utils.SoundUtils.ERROR_SOUND, 1, 1));
            return;
        }

        Plot plot = unclaimedPlots.get(Utils.getRandom().nextInt(unclaimedPlots.size()));
        PlotHandler.assignAndGeneratePlot(builder, plot);
    }

    public static boolean generateRandomPlot(Player player, @NotNull List<CityProject> items, PlotDifficulty selectedPlotDifficulty) {
        PlotDifficulty difficulty = selectedPlotDifficulty;
        if (items.isEmpty()) {
            player.playSound(player, Utils.SoundUtils.ERROR_SOUND, 1, 1);
            return false;
        }
        CityProject randomCity = items.get(Utils.getRandom().nextInt(items.size()));

        Builder builder = Builder.byUUID(player.getUniqueId());
        try {
            if (difficulty == null) difficulty = Plot.getPlotDifficultyForBuilder(randomCity, Builder.byUUID(player.getUniqueId())).get();
            if (difficulty == null) difficulty = PlotDifficulty.EASY;
        } catch (InterruptedException | ExecutionException e) {
            sqlError(player, e);
            return false;
        }
        player.closeInventory();
        boolean successful = PlotHandler.assignAndGenerateRandomPlot(builder, randomCity, difficulty);
        if (successful) player.playSound(player, Utils.SoundUtils.DONE_SOUND, 1, 1);
        return true;
    }

    public static List<CityProject> getValidCityProjects(PlotDifficulty selectedPlotDifficulty, Player player, Country country) {
        return DataProvider.CITY_PROJECT.getByCountryCode(country.getCode(), true).stream().filter(test -> {
            if (!(test instanceof CityProject project)) return false;
            var pd = selectedPlotDifficulty;
            try {
                if (pd == null) pd = Plot.getPlotDifficultyForBuilder(project, Builder.byUUID(player.getUniqueId())).get();
                if (pd == null) pd = PlotDifficulty.EASY;

                return project.isVisible() && !DataProvider.PLOT.getPlots(project, pd, Status.unclaimed).isEmpty();
            } catch (ExecutionException | InterruptedException e) {
                sqlError(player, e);
            }
            return false;
        }).toList();
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1).setName(Component.empty()).build())
                .pattern("001111001")
                .pattern(Utils.EMPTY_MASK)
                .pattern(Utils.EMPTY_MASK)
                .pattern(Utils.EMPTY_MASK)
                .pattern(Utils.EMPTY_MASK)
                .pattern("100010001")
                .build();
    }

    @Override
    protected List<?> getSource() {
        if (projects == null) projects = DataProvider.CITY_PROJECT.getByCountryCode(country.getCode(), true);
        return projects;
    }

    @Override
    protected void setPaginatedMenuItemsAsync(List<?> source) {
        List<CityProject> cities = source.stream().map(l -> (CityProject) l).toList();
        int slot = 9;
        for (CityProject city : cities) {
            getMenu().getSlot(slot).setItem(city.getItem(getMenuPlayer(), selectedPlotDifficulty));
            slot++;
        }
    }

    @Override
    protected void setPaginatedItemClickEventsAsync(List<?> source) {
        List<CityProject> cities = source.stream().map(l -> (CityProject) l).toList();
        int slot = 9;
        for (CityProject city : cities) {
            final int _slot = slot;
            getMenu().getSlot(_slot).setClickHandler((clickPlayer, clickInformation) -> {
                if (getMenu().getSlot(_slot).getItem(clickPlayer).equals(MenuItems.errorItem(getMenuPlayer()))) {
                    clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.ERROR_SOUND, 1, 1);
                    return;
                }
                clickPlayer.closeInventory();

                CompletableFuture.runAsync(() -> handleCityProjectClick(clickPlayer, city));
            });
            slot++;
        }
    }

    private static void sqlError(@NotNull Player clickPlayer, Exception ex) {
        Utils.logSqlException(ex);
        clickPlayer.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(clickPlayer, LangPaths.Message.Error.ERROR_OCCURRED)));
        clickPlayer.playSound(clickPlayer.getLocation(), Utils.SoundUtils.ERROR_SOUND, 1, 1);
        Thread.currentThread().interrupt();
    }
}
