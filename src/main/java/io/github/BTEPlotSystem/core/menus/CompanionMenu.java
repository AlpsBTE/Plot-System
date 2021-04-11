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

import github.BTEPlotSystem.core.system.Builder;
import github.BTEPlotSystem.core.system.CityProject;
import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.core.system.plot.PlotGenerator;
import github.BTEPlotSystem.core.system.plot.PlotManager;
import github.BTEPlotSystem.utils.*;
import github.BTEPlotSystem.utils.enums.Slot;
import github.BTEPlotSystem.utils.enums.Status;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class CompanionMenu extends AbstractMenu {

    private final Plot[] slots = new Plot[3];
    private final List<CityProject> cityProjects = CityProject.getCityProjects();

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
            Builder builder = new Builder(getMenuPlayer().getUniqueId());
            for(int i = 0; i < 3; i++) {
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
        // Add Navigator Item
        getMenu().getSlot(4)
                .setItem(new ItemBuilder(Material.COMPASS,1)
                        .setName("§6§lNavigator").setLore(new LoreBuilder()
                                .addLine("Open the navigator menu").build())
                        .build());

        // Add "Player Plots Slots" Items
        for(int i = 0; i < cityProjects.size(); i++) {
            if(i <= 28) {
                ItemStack cityProjectItem;
                switch (cityProjects.get(i).getCountry()) {
                    case AT:
                        cityProjectItem = Utils.getItemHead("4397");
                        break;
                    case CH:
                        cityProjectItem = Utils.getItemHead("32348");
                        break;
                    case LI:
                        cityProjectItem = Utils.getItemHead("26174");
                        break;
                    case IT:
                        cityProjectItem = Utils.getItemHead("21903");
                    default:
                        // Set Error Item
                        cityProjectItem = errorItem();
                }

                try {
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
                                                    getCityDifficultyForBuilder(cityProjects.get(i).getID(), new Builder(getMenuPlayer().getUniqueId())))
                                            .build())
                                    .build());
                } catch (SQLException ex) {
                    getMenu().getSlot(9 + i).setItem(errorItem());
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                }
            }
        }

        // Add "City Projects" Items
        for (int i = 0; i < 3 ; i++) {
            try {
                getMenu().getSlot(46 + i)
                        .setItem(new ItemBuilder(Material.MAP,1 + i)
                                .setName("§b§lSLOT " + (i + 1))
                                .setLore(new LoreBuilder()
                                        .addLines("§7ID: §f" + slots[i].getID(),
                                                  "§7City: §f" + slots[i].getCity().getName(),
                                                  "§7Difficulty: §f" +  slots[i].getDifficulty().name().charAt(0) + slots[i].getDifficulty().name().substring(1).toLowerCase(),
                                                  "",
                                                  "§6§lStatus: §7§l" + slots[i].getStatus().name().substring(0, 1).toUpperCase() + slots[i].getStatus().name().substring(1)
                                        ).build())
                                .build());
            } catch (Exception ex) {
                getMenu().getSlot(46 + i)
                        .setItem(new ItemBuilder(Material.EMPTY_MAP,1+i)
                                .setName("§b§lSLOT " + (i + 1))
                                .setLore(new LoreBuilder()
                                        .addLines("§7Click on a city project to create a new plot",
                                                  "",
                                                  "§6§lStatus: §7§lUnassigned")
                                        .build())
                                .build());
            }
        }

        // Add "Builder Utilities Menu" Item
        getMenu().getSlot(50).setItem(BuilderUtilitiesMenu.getMenuItem());

        // Add "Show Plots Menu" Item
        getMenu().getSlot(51).setItem(PlayerPlotsMenu.getMenuItem());

        // Add "Player Settings Menu" Item
        getMenu().getSlot(52)
                .setItem(new ItemBuilder(Material.REDSTONE_COMPARATOR)
                        .setName("§b§lSettings")
                        .setLore(new LoreBuilder()
                                .addLine("Modify your user settings").build())
                        .build());
    }

    @Override
    protected void setItemClickEvents() {
        // Add Click Event for Navigator Item
        getMenu().getSlot(4).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            clickPlayer.performCommand("navigator");
        });

        // Add Click Event For City Projects
        for(int i = 0; i < cityProjects.size(); i++) {
            int itemSlot = i;
            getMenu().getSlot(9 + i).setClickHandler((clickPlayer, clickInformation) -> {
                if(!getMenu().getSlot(9 + itemSlot).getItem(clickPlayer).equals(errorItem())) {
                    try {
                        clickPlayer.closeInventory();
                        Builder builder = new Builder(clickPlayer.getUniqueId());
                        int cityID = cityProjects.get(itemSlot).getID();
                        if (builder.getFreeSlot() != null){
                            if (PlotManager.getPlots(cityID, Status.unclaimed).size() != 0){
                                    clickPlayer.sendMessage(Utils.getInfoMessageFormat("Creating a new plot..."));
                                    clickPlayer.playSound(clickPlayer.getLocation(), Utils.CreatePlotSound, 1, 1);

                                    new PlotGenerator(cityID, PlotManager.getPlotDifficultyForBuilder(cityID, builder), builder);
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

        // Add Click Event For Player Plot Slots
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

        // Add Click Event For Builder Utilities Menu
        getMenu().getSlot(50).setClickHandler(((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            new BuilderUtilitiesMenu(clickPlayer);
        }));

        // Add Click Event For Show Plots Menu
        getMenu().getSlot(51).setClickHandler(((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            clickPlayer.performCommand("plots " + clickPlayer.getName());
        }));

        // Add Click Event For Player Settings Menu
        getMenu().getSlot(52).setClickHandler(((clickPlayer, clickInformation) -> clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1)));
    }

   public static ItemStack getMenuItem() {
       return new ItemBuilder(Material.NETHER_STAR, 1)
               .setName("§b§lCompanion §7(Right Click)")
               .setEnchantment(true)
               .build();
   }

    private String getCityDifficultyForBuilder(int cityID, Builder builder) throws SQLException {
        int diff_ID = 0;
        if(PlotManager.getPlotDifficultyForBuilder(cityID, builder) != null) {
            diff_ID = PlotManager.getPlotDifficultyForBuilder(cityID, builder).ordinal() + 1;
        }

        switch (diff_ID) {
            case 1:
                return "§a§lEASY";
            case 2:
                return "§6§lMEDIUM";
            case 3:
                return "§c§lHARD";
            default:
                return "§f§lNo Plots Available";
        }
    }
}