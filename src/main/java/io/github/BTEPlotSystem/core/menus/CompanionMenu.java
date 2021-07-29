/*
 * The MIT License (MIT)
 *
 *  Copyright © 2021, Alps BTE <bte.atchli@gmail.com>
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

package github.BTEPlotSystem.core.menus;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.system.Builder;
import github.BTEPlotSystem.core.system.CityProject;
import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.core.system.plot.PlotGenerator;
import github.BTEPlotSystem.core.system.plot.PlotManager;
import github.BTEPlotSystem.utils.*;
import github.BTEPlotSystem.utils.enums.PlotDifficulty;
import github.BTEPlotSystem.utils.enums.Slot;
import github.BTEPlotSystem.utils.enums.Status;
import github.BTEPlotSystem.utils.items.MenuItems;
import github.BTEPlotSystem.utils.items.builder.ItemBuilder;
import github.BTEPlotSystem.utils.items.builder.LoreBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;

public class CompanionMenu extends AbstractMenu {

    private final Plot[] slots = new Plot[3];
    private final List<CityProject> cityProjects = CityProject.getCityProjects();

    private PlotDifficulty selectedPlotDifficulty = null;

    public CompanionMenu(Player player) {
        super(6, "Companion", player);

        Mask mask = BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("111101111")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("100010001")
                .build();
        mask.apply(getMenu());

        try {
            // Get player slots of player
            Builder builder = new Builder(getMenuPlayer().getUniqueId());
            for (int i = 0; i < 3; i++) {
                slots[i] = builder.getPlot(Slot.values()[i]);
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }

        addMenuItems();
        setItemClickEvents();

        getMenu().open(getMenuPlayer());
    }

    @Override
    protected void addMenuItems() {
        // Set navigator itemS
        getMenu().getSlot(4)
                .setItem(new ItemBuilder(Material.valueOf(BTEPlotSystem.getPlugin().getConfig().getString("navigator.item")), 1)
                        .setName("§6§l"+ BTEPlotSystem.getPlugin().getConfig().getString("navigator.name")).setLore(new LoreBuilder()
                                .addLine(BTEPlotSystem.getPlugin().getConfig().getString("navigator.description")).build())
                        .build());

        // Set switch plots difficulty item
        try {
            getMenu().getSlot(7).setItem(getSelectedDifficultyItem());
        } catch (SQLException ex) {
            getMenu().getSlot(7).setItem(MenuItems.errorItem());
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }

        // Set city project items
        setCityProjectItems();

        // Set player slots items
        for (int i = 0; i < 3; i++) {
            try {
                getMenu().getSlot(46 + i)
                        .setItem(new ItemBuilder(Material.MAP, 1 + i)
                                .setName("§b§lSLOT " + (i + 1))
                                .setLore(new LoreBuilder()
                                        .addLines("§7ID: §f" + slots[i].getID(),
                                                "§7City: §f" + slots[i].getCity().getName(),
                                                "§7Difficulty: §f" + slots[i].getDifficulty().name().charAt(0) + slots[i].getDifficulty().name().substring(1).toLowerCase(),
                                                "",
                                                "§6§lStatus: §7§l" + slots[i].getStatus().name().substring(0, 1).toUpperCase() + slots[i].getStatus().name().substring(1)
                                        ).build())
                                .build());
            } catch (Exception ex) {
                getMenu().getSlot(46 + i)
                        .setItem(new ItemBuilder(Material.EMPTY_MAP, 1 + i)
                                .setName("§b§lSLOT " + (i + 1))
                                .setLore(new LoreBuilder()
                                        .addLines("§7Click on a city project to create a new plot",
                                                "",
                                                "§6§lStatus: §7§lUnassigned")
                                        .build())
                                .build());
            }
        }

        // Set builder utilities item
        getMenu().getSlot(50).setItem(BuilderUtilitiesMenu.getMenuItem());

        // Set show plots item
        getMenu().getSlot(51).setItem(PlayerPlotsMenu.getMenuItem());

        // Set player settings item
        getMenu().getSlot(52)
                .setItem(new ItemBuilder(Material.REDSTONE_COMPARATOR)
                        .setName("§b§lSettings")
                        .setLore(new LoreBuilder()
                                .addLine("Modify your user settings").build())
                        .build());
    }

    @Override
    protected void setItemClickEvents() {
        // Add click event for navigator item
        getMenu().getSlot(4).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            clickPlayer.performCommand(BTEPlotSystem.getPlugin().getConfig().getString("navigator.command"));
        });

        // Add click event for switch plots difficulty item
        getMenu().getSlot(7).setClickHandler((clickPlayer, clickInformation) -> {
            try {
                selectedPlotDifficulty = (selectedPlotDifficulty == null ?
                        PlotDifficulty.values()[0] : selectedPlotDifficulty.ordinal() != PlotDifficulty.values().length - 1 ?
                        PlotDifficulty.values()[selectedPlotDifficulty.ordinal() + 1] : null);
                getMenu().getSlot(7).setItem(getSelectedDifficultyItem());
                setCityProjectItems();

                clickPlayer.playSound(clickPlayer.getLocation(), Utils.Done, 1, 1);
            } catch (SQLException ex) {
                clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        });

        // Add click event for city projects items
        for(int i = 0; i < cityProjects.size(); i++) {
            int itemSlot = i;
            getMenu().getSlot(9 + i).setClickHandler((clickPlayer, clickInformation) -> {
                if(!getMenu().getSlot(9 + itemSlot).getItem(clickPlayer).equals(MenuItems.errorItem())) {
                    try {
                        clickPlayer.closeInventory();
                        Builder builder = new Builder(clickPlayer.getUniqueId());
                        int cityID = cityProjects.get(itemSlot).getID();

                        if (builder.getFreeSlot() != null){
                            PlotDifficulty plotDifficultyForCity = selectedPlotDifficulty != null ? selectedPlotDifficulty : PlotManager.getPlotDifficultyForBuilder(cityID, builder);
                            if (PlotManager.getPlots(cityID, plotDifficultyForCity, Status.unclaimed).size() != 0){
                                if(PlotGenerator.playerPlotGenerationHistory.containsKey(builder.getUUID())) {
                                    if(PlotGenerator.playerPlotGenerationHistory.get(builder.getUUID()).isBefore(LocalDateTime.now().minusSeconds(10))) {
                                        PlotGenerator.playerPlotGenerationHistory.remove(builder.getUUID());
                                    } else {
                                        clickPlayer.sendMessage(Utils.getErrorMessageFormat("Please wait few seconds before creating a new plot!"));
                                        clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                                        return;
                                    }
                                }

                                clickPlayer.sendMessage(Utils.getInfoMessageFormat("Creating a new plot..."));
                                clickPlayer.playSound(clickPlayer.getLocation(), Utils.CreatePlotSound, 1, 1);

                                new PlotGenerator(cityID, plotDifficultyForCity, builder);
                            } else {
                                clickPlayer.sendMessage(Utils.getErrorMessageFormat("This city project doesn't have any more plots left. Please select another project."));
                                clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                            }
                        } else {
                            clickPlayer.sendMessage(Utils.getErrorMessageFormat("All your slots are occupied! Please finish your current plots before creating a new one."));
                            clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                        }
                    } catch (SQLException ex) {
                        clickPlayer.sendMessage(Utils.getErrorMessageFormat("An internal error occurred! Please try again or contact a staff member."));
                        clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                        Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                    }
                } else {
                    clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                }
            });
        }

        // Add click event for player slot items
        for(int i = 0; i < 3; i++) {
            if(slots[i] != null) {
                int itemSlot = i;
                getMenu().getSlot(46 + i).setClickHandler((clickPlayer, clickInformation) -> {
                    clickPlayer.closeInventory();
                    try {
                        new PlotActionsMenu(clickPlayer, slots[itemSlot]);
                    } catch (Exception ex ) {
                        clickPlayer.sendMessage(Utils.getErrorMessageFormat("Something went wrong... please message a Manager or Developer."));
                        clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                        Bukkit.getLogger().log(Level.SEVERE, "An error occurred while opening the plot actions menu.", ex);
                    }
                });
            }
        }

        // Add click event for builder utilities item
        getMenu().getSlot(50).setClickHandler(((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            new BuilderUtilitiesMenu(clickPlayer);
        }));

        // Add click event for show plots item
        getMenu().getSlot(51).setClickHandler(((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            clickPlayer.performCommand("plots " + clickPlayer.getName());
        }));

        // Add click event for player settings item
        getMenu().getSlot(52).setClickHandler(((clickPlayer, clickInformation) -> clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1)));
    }

   public static ItemStack getMenuItem() {
       return new ItemBuilder(Material.NETHER_STAR, 1)
               .setName("§b§lCompanion §7(Right Click)")
               .setEnchantment(true)
               .build();
   }

    // Set city project items
    private void setCityProjectItems() {
        for(int i = 0; i < cityProjects.size(); i++) {
            if(i <= 28) {
                ItemStack cityProjectItem = MenuItems.errorItem();
                cityProjectItem = Utils.getItemHead(Integer.toString(cityProjects.get(i).getCountry().getHeadID()));
                try {
                    PlotDifficulty plotDifficultyForCity = selectedPlotDifficulty != null ?
                            selectedPlotDifficulty : PlotManager.getPlotDifficultyForBuilder(cityProjects.get(i).getID(), new Builder(getMenuPlayer().getUniqueId()));

                    getMenu().getSlot(9 + i)
                            .setItem(new ItemBuilder(cityProjectItem)
                                    .setName("§b§l" + cityProjects.get(i).getName())
                                    .setLore(new LoreBuilder()
                                            .addLines(cityProjects.get(i).getDescription(),
                                                    "",
                                                    "§6" + PlotManager.getPlots(cityProjects.get(i).getID(), Status.unclaimed).size() + " §7Plots Open",
                                                    "§f---------------------",
                                                    "§6" + PlotManager.getPlots(cityProjects.get(i).getID(), Status.unfinished, Status.unreviewed).size() + " §7Plots In Progress",
                                                    "§6" + PlotManager.getPlots(cityProjects.get(i).getID(), Status.complete).size() + " §7Plots Completed",
                                                    "",
                                                    PlotManager.getPlots(cityProjects.get(i).getID(), plotDifficultyForCity, Status.unclaimed).size() != 0 ?
                                                            Utils.getFormattedDifficulty(plotDifficultyForCity) : "§f§lNo Plots Available"
                                                    )
                                            .build())
                                    .build());
                } catch (SQLException ex) {
                    getMenu().getSlot(9 + i).setItem(MenuItems.errorItem());
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                }
            }
        }
    }

    // Get selected plot difficulty item of player
    private ItemStack getSelectedDifficultyItem() throws SQLException {
        ItemStack item = Utils.getItemHead("9248");

        if(selectedPlotDifficulty != null) {
            if(selectedPlotDifficulty == PlotDifficulty.EASY) {
                item = Utils.getItemHead("10220");
            } else if(selectedPlotDifficulty == PlotDifficulty.MEDIUM) {
                item = Utils.getItemHead("9680");
            } else if(selectedPlotDifficulty == PlotDifficulty.HARD){
                item = Utils.getItemHead("9356");
            }
        }

        return new ItemBuilder(item)
                .setName("§b§lPLOT DIFFICULTY")
                .setLore(new LoreBuilder()
                        .addLines("",
                                selectedPlotDifficulty != null ? Utils.getFormattedDifficulty(selectedPlotDifficulty) : "§f§lAutomatic",
                                selectedPlotDifficulty != null ? "§7Score Multiplier: §fx" + PlotManager.getMultiplierByDifficulty(selectedPlotDifficulty) : "",
                                "",
                                "§7Click To Switch...")
                        .build())
                .build();
    }
}