package com.alpsbte.plotsystem.core.menus.companion;

import com.alpsbte.plotsystem.core.menus.AbstractPaginatedMenu;
import com.alpsbte.plotsystem.core.system.CityProject;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import com.alpsbte.plotsystem.utils.items.builder.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class CityProjectMenu extends AbstractPaginatedMenu {
    Country country;
    List<CityProject> projects;

    CityProjectMenu(Player player, Country country) {
        super(6, 4, "TO BE TRANSLATED", player);
        this.country = country;
    }

    @Override
    protected void setPreviewItems() {
        // add difficulty switcher

        getMenu().getSlot(4).setItem(new ItemBuilder(Utils.getItemHead(new Utils.CustomHead("37793"))).setName("Back to countries").build());

        for (Map.Entry<Integer, CompanionMenu.FooterItem> entry : CompanionMenu.getFooterItems(45, getMenuPlayer(), player -> {
            player.closeInventory();
            new CompanionMenu(player, country.getContinent());
        }).entrySet()) {
            getMenu().getSlot(entry.getKey()).setItem(entry.getValue().item);
        }

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
    }

    @Override
    protected void setItemClickEvents() {
        getMenu().getSlot(4).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            new CompanionMenu(clickPlayer, country.getContinent());
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
            new CompanionMenu(player, country.getContinent());
        }).entrySet()) {
            getMenu().getSlot(entry.getKey()).setClickHandler(entry.getValue().clickHandler);
        }
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
        // proceed to assign plot
    }
}
