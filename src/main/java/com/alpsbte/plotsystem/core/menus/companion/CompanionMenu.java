package com.alpsbte.plotsystem.core.menus.companion;

import com.alpsbte.plotsystem.core.menus.BuilderUtilitiesMenu;
import com.alpsbte.plotsystem.core.menus.PlayerPlotsMenu;
import com.alpsbte.plotsystem.core.menus.PlotActionsMenu;
import com.alpsbte.plotsystem.core.menus.SettingsMenu;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.Country;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Continent;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Slot;
import com.alpsbte.plotsystem.utils.io.language.LangPaths;
import com.alpsbte.plotsystem.utils.io.language.LangUtil;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import com.alpsbte.plotsystem.utils.items.builder.ItemBuilder;
import com.alpsbte.plotsystem.utils.items.builder.LoreBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;

public class CompanionMenu {
    public static boolean hasContinentView() {
        return Arrays.stream(Continent.values()).map(continent -> Country.getCountries(continent).size()).filter(count -> count > 0).count() > 1;
    }

    /**
     * Determine what menu to open for the player
     *
     * @param player player to open the menu for
     */
    public static void open(Player player) {
        if (hasContinentView()) {
            new ContinentMenu(player);
        } else {
            Optional<Continent> continent = Arrays.stream(Continent.values()).filter(c -> Country.getCountries(c).size() > 0).findFirst();

            if (!continent.isPresent()) {
                player.sendMessage(Utils.getErrorMessageFormat(LangUtil.get(player, LangPaths.Message.Error.ERROR_OCCURRED)));
                return;
            }

            new CountryMenu(player, continent.get());
        }
    }

    /**
     * Get common footer items between all companion menus
     *
     * @param startingSlot slot to start drawing items at
     * @param player       player that is viewing this (translation purposes)
     * @param returnToMenu a lambda to call when needing to return to current menu
     * @return FooterItems indexed by slot number
     */
    public static HashMap<Integer, FooterItem> getFooterItems(int startingSlot, Player player, Consumer<Player> returnToMenu) {
        HashMap<Integer, FooterItem> items = new HashMap<>();
        // Set builder utilities menu item
        items.put(startingSlot + 5, new FooterItem(BuilderUtilitiesMenu.getMenuItem(player), (clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            new BuilderUtilitiesMenu(clickPlayer);
        }));

        // Set player plots menu item
        items.put(startingSlot + 6, new FooterItem(PlayerPlotsMenu.getMenuItem(player), (clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            clickPlayer.performCommand("plots " + clickPlayer.getName());
        }));

        // Set player settings menu item
        items.put(startingSlot + 7, new FooterItem(new ItemBuilder(Material.REDSTONE_COMPARATOR)
                .setName("§b§l" + LangUtil.get(player, LangPaths.MenuTitle.SETTINGS))
                .setLore(new LoreBuilder()
                        .addLine(LangUtil.get(player, LangPaths.MenuDescription.SETTINGS)).build())
                .build(), (clickPlayer, clickInformation) -> new SettingsMenu(clickPlayer, returnToMenu)));

        for (int i = 0; i < 3; i++) {
            try {
                Builder builder = Builder.byUUID(player.getUniqueId());

                final int i_ = i;

                items.put(startingSlot + 1 + i, new FooterItem(builder.getPlotMenuItem(Slot.values()[i], player), (clickPlayer, clickInformation) -> {
                    clickPlayer.closeInventory();
                    try {
                        new PlotActionsMenu(clickPlayer, builder.getPlot(Slot.values()[i_]));
                    } catch (SQLException ex) {
                        clickPlayer.sendMessage(Utils.getErrorMessageFormat(LangUtil.get(clickPlayer, LangPaths.Message.Error.ERROR_OCCURRED)));
                        clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                        Bukkit.getLogger().log(Level.SEVERE, "An error occurred while opening the plot actions menu!", ex);
                    }
                }));
            } catch (NullPointerException | SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "An error occurred while placing player slot items!", ex);
                items.put(startingSlot + 1 + i, new FooterItem(MenuItems.errorItem(player)));
            }
        }

        return items;
    }

    public static ItemStack getDifficultyItem(Player player, PlotDifficulty selectedPlotDifficulty) {
        ItemStack item = Utils.getItemHead(Utils.CustomHead.WHITE_CONCRETE);

        if (selectedPlotDifficulty != null) {
            if (selectedPlotDifficulty == PlotDifficulty.EASY) {
                item = Utils.getItemHead(Utils.CustomHead.GREEN_CONCRETE);
            } else if (selectedPlotDifficulty == PlotDifficulty.MEDIUM) {
                item = Utils.getItemHead(Utils.CustomHead.YELLOW_CONCRETE);
            } else if (selectedPlotDifficulty == PlotDifficulty.HARD) {
                item = Utils.getItemHead(Utils.CustomHead.RED_CONCRETE);
            }
        }

        try {
            return new ItemBuilder(item)
                    .setName("§b§l" + LangUtil.get(player, LangPaths.MenuTitle.PLOT_DIFFICULTY).toUpperCase())
                    .setLore(new LoreBuilder()
                            .addLines("",
                                    selectedPlotDifficulty != null ? Utils.getFormattedDifficulty(selectedPlotDifficulty) : "§f§l" + LangUtil.get(player, LangPaths.Difficulty.AUTOMATIC),
                                    selectedPlotDifficulty != null ? "§7" + LangUtil.get(player, LangPaths.Difficulty.SCORE_MULTIPLIER) + ": §fx" + PlotManager.getMultiplierByDifficulty(selectedPlotDifficulty) : "",
                                    "",
                                    "§7" + LangUtil.get(player, LangPaths.MenuDescription.PLOT_DIFFICULTY))
                            .build())
                    .build();
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            return MenuItems.errorItem(player);
        }
    }

    /**
     * @return Menu item
     */
    public static ItemStack getMenuItem(Player player) {
        return new ItemBuilder(Material.NETHER_STAR, 1)
                .setName("§b§l" + LangUtil.get(player, LangPaths.MenuTitle.COMPANION) + " §7(" + LangUtil.get(player, LangPaths.Note.Action.RIGHT_CLICK) + ")")
                .setEnchanted(true)
                .build();
    }

    static class FooterItem {
        public ItemStack item;
        public org.ipvp.canvas.slot.Slot.ClickHandler clickHandler = null;

        FooterItem(ItemStack item, org.ipvp.canvas.slot.Slot.ClickHandler clickHandler) {
            this.item = item;
            this.clickHandler = clickHandler;
        }

        FooterItem(ItemStack item) {
            this.item = item;
        }
    }
}
