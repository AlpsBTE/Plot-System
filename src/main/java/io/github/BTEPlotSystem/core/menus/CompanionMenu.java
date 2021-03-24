package github.BTEPlotSystem.core.menus;

import github.BTEPlotSystem.core.plots.Plot;
import github.BTEPlotSystem.core.plots.PlotGenerator;
import github.BTEPlotSystem.core.plots.PlotManager;
import github.BTEPlotSystem.utils.*;
import github.BTEPlotSystem.utils.enums.Difficulty;
import github.BTEPlotSystem.utils.enums.Slot;
import github.BTEPlotSystem.utils.enums.Status;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import org.ipvp.canvas.type.ChestMenu;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class CompanionMenu {
    private final Menu menu = ChestMenu.builder(6).title("Companion").build();
    private final Player player;

    private final HeadDatabaseAPI headDB = Utils.headDatabaseAPI;

    public CompanionMenu(Player player) throws SQLException {
        this.player = player;

        Mask mask = BinaryMask.builder(menu)
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("111101111")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("100010001")
                .build();
        mask.apply(menu);

        setMenuItems();

        menu.open(player);
    }

    public void setMenuItems() throws SQLException {

        // Set Navigator Menu Item
        menu.getSlot(4)
                .setItem(new ItemBuilder(Material.COMPASS,1)
                .setName("§6§lNavigator").setLore(new LoreBuilder()
                                .description("§7", "Open the navigator menu.")
                                .build())
                .build());
        menu.getSlot(4).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.performCommand("navigator");
            clickPlayer.closeInventory();
        });

        // Set Player Slots Items
        for (int i = 0;i<3;i++){
            try {
                Plot plot = new Builder(player.getUniqueId()).getPlot(Slot.values()[i]);

                menu.getSlot(46+i)
                        .setItem(new ItemBuilder(Material.MAP,1 + i)
                                .setName("§b§lSLOT " + (i + 1))
                                .setLore(new LoreBuilder()
                                        .description(
                                                     "§7",
                                                     "ID: §f" + plot.getID(),
                                                     "§7City: §f" + plot.getCity().getName(),
                                                     "§7Difficulty: §f" +  plot.getDifficulty().name().charAt(0) + plot.getDifficulty().name().substring(1).toLowerCase(),
                                                     "",
                                                     "§6§lStatus: §7§l" + plot.getStatus().name().substring(0, 1).toUpperCase() + plot.getStatus().name().substring(1))
                                        .build())
                                .build());

                menu.getSlot(46 + i).setClickHandler((clickPlayer, clickInformation) -> {
                    clickPlayer.closeInventory();
                    try {
                        new PlotActionsMenu(plot, player);
                    } catch (Exception ex ) {
                        clickPlayer.sendMessage(Utils.getErrorMessageFormat("Something went wrong... please message a Manager or Developer."));
                        clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound,1,1);
                        Bukkit.getLogger().log(Level.SEVERE, "An error occurred when opening plot actions menu.", ex);
                    }
                });
            } catch (Exception e) {
                menu.getSlot(46+i)
                        .setItem(new ItemBuilder(Material.EMPTY_MAP,1+i)
                                .setName("§b§lSLOT " + (i + 1))
                                .setLore(new LoreBuilder()
                                        .description("§7",
                                                "§7Click on a city project to create a new plot.",
                                                     "",
                                                     "§6§lStatus: §7§lUnassigned")
                                        .build())
                                .build());
            }
        }

        // Set City Projects Items
        List<CityProject> cities = CityProject.getCityProjects();

        for (int i = 0; i < cities.size(); i++){
            int cityID = cities.get(i).getID();

            menu.getSlot(9+i).setClickHandler((clickPlayer, clickInformation) -> {
                try {
                    clickPlayer.closeInventory();
                    Builder builder = new Builder(clickPlayer.getUniqueId());
                    if (builder.getFreeSlot() != null){
                        if (PlotManager.getPlots(cityID, Status.unclaimed).size() != 0){
                            if(PlotManager.getPlotDifficultyForBuilder(cityID, builder) != null) {
                                clickPlayer.sendMessage(Utils.getInfoMessageFormat("Creating a new plot..."));
                                clickPlayer.playSound(clickPlayer.getLocation(), Utils.CreatePlotSound, 1, 1);

                                new PlotGenerator(cityID, PlotManager.getPlotDifficultyForBuilder(cityID, builder), builder);
                            } else {
                                clickPlayer.sendMessage(Utils.getErrorMessageFormat("This city project has no open plots available for your difficulty level! Please select another project!"));
                                clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                            }
                        } else {
                            clickPlayer.sendMessage(Utils.getErrorMessageFormat("This city project doesn't have any more plots left. Please select another project."));
                            clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                        }
                    } else {
                        clickPlayer.sendMessage(Utils.getErrorMessageFormat("All your slots are occupied! Please finish your current plots before creating a new one."));
                        clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                    }
                } catch (SQLException ex) {
                    clickPlayer.sendMessage(Utils.getErrorMessageFormat("An internal error occurred while creating a new plot! Please try again or contact a staff member."));
                    clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                    Bukkit.getLogger().log(Level.SEVERE, "An database error occurred while generating a new plot!", ex);
                }
            });

            ItemStack cityProjectItem = null;
            switch (cities.get(i).getCountry()) {
                case AT:
                    cityProjectItem = getCityProjectItem("4397");
                    break;
                case CH:
                    cityProjectItem = getCityProjectItem("32348");
                    break;
                case LI:
                    cityProjectItem = getCityProjectItem("26174");
                    break;
                case IT:
                    cityProjectItem = getCityProjectItem("21903");
            }

            menu.getSlot(9+i)
                    .setItem(new ItemBuilder(cityProjectItem)
                            .setName("§b§l" + cities.get(i).getName())
                            .setLore(new LoreBuilder()
                                    .description(
                                            "§7",
                                            cities.get(i).getDescription(),
                                            "",
                                            "§6" + PlotManager.getPlots(cityID, Status.unclaimed).size() + " §7Plots Open",
                                            "§6" + PlotManager.getPlots(cityID, Status.unfinished, Status.unreviewed).size() + " §7Plots In Progress",
                                            "§6" + PlotManager.getPlots(cityID, Status.complete).size() + " §7Plots Completed",
                                            "",
                                            getCityDifficultyForBuilder(cityID, new Builder(player.getUniqueId())))
                                    .build())
                            .build());
        }

        // Set Builder Utilities Menu Item
        menu.getSlot(50)
                .setItem(new ItemBuilder(Material.GOLD_AXE)
                        .setName("§b§lBuilder Utilities")
                        .setLore(new LoreBuilder()
                                .description("§7", "Get access to custom heads, banners and special blocks.")
                                .build())
                        .build());
        menu.getSlot(50).setClickHandler(((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            new BuilderUtilitiesMenu(clickPlayer);
        }));

        // Set Show Player Plots Menu Item
        menu.getSlot(51)
                .setItem(new ItemBuilder(Utils.headDatabaseAPI.getItemHead("9282"))
                        .setName("§b§lShow Plots")
                        .setLore(new LoreBuilder()
                                .description("§7", "Show all your plots.")
                                .build())
                        .build());
        menu.getSlot(51).setClickHandler(((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            clickPlayer.performCommand("plots " + clickPlayer.getName());
        }));

        // Set Player Settings Menu Item
        menu.getSlot(52)
                .setItem(new ItemBuilder(Material.REDSTONE_COMPARATOR)
                        .setName("§b§lSettings")
                        .setLore(new LoreBuilder()
                                .description("§7", "Modify your user settings.")
                                .build())
                        .build());
        menu.getSlot(52).setClickHandler(((clickPlayer, clickInformation) -> {

        }));
    }

    private ItemStack getCityProjectItem(String HeadID) {
        return (headDB != null) ? headDB.getItemHead(HeadID) : new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
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

    public static ItemStack getItem(){
        return new ItemBuilder(Material.NETHER_STAR, 1)
                .setName("§b§lCompanion §7(Right Click)")
                .setEnchantment(Enchantment.ARROW_DAMAGE)
                .setItemFlag(ItemFlag.HIDE_ENCHANTS)
                .build();
    }
}