package github.BTEPlotSystem.core.menus;

import github.BTEPlotSystem.core.plots.Plot;
import github.BTEPlotSystem.core.plots.PlotGenerator;
import github.BTEPlotSystem.core.plots.PlotHandler;
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

        menu.getSlot(4)
                .setItem(new ItemBuilder(Material.COMPASS,1)
                .setName("§6§lNavigator").setLore(new LoreBuilder()
                                .description("Open the navigator menu.")
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
                                        .description("§6ID: §7" + plot.getID(),
                                                     "§6City: §7" + plot.getCity().getName(),
                                                     "§6Difficulty: §7" + plot.getCity().getDifficulty().name(),
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
                                        .description("§7Click on a city project to create a new plot.",
                                                     "",
                                                     "§6§lStatus: §7§lUnassigned")
                                        .build())
                                .build());
            }
        }

        // Set Custom Heads Item
        menu.getSlot(50)
                .setItem(new ItemBuilder(Material.SKULL_ITEM, 1, (byte) 3)
                .setName("§b§lCUSTOM HEADS")
                .setLore(new LoreBuilder().description("Open the head menu to get a variety of custom heads.").build())
                .build());
        menu.getSlot(50).setClickHandler((clickPlayer, clickInformation) -> {
            if (clickPlayer.getWorld().getName().startsWith("P-")){
                clickPlayer.performCommand("hdb");
            } else {
                clickPlayer.sendMessage(Utils.getErrorMessageFormat("You need to be on a plot in order to use this"));
            }
        });

        // Set Banner Maker Item
        menu.getSlot(51)
                .setItem(new ItemBuilder(Material.BANNER, 1, (byte) 14)
                .setName("§b§lBANNER MAKER")
                .setLore(new LoreBuilder().description("Open the banner maker menu to create your own custom banners.").build())
                .build());
        menu.getSlot(51).setClickHandler((clickPlayer, clickInformation) -> {
            if (clickPlayer.getWorld().getName().startsWith("P-")){
                clickPlayer.performCommand("bm");
            } else {
                clickPlayer.sendMessage(Utils.getErrorMessageFormat("You need to be on a plot in order to use this"));
            }
        });

        // Set Custom Blocks Item
        menu.getSlot(52)
                .setItem(new ItemBuilder(Material.GOLD_BLOCK ,1)
                .setName("§b§lSPECIAL BLOCKS")
                .setLore(new LoreBuilder().description("Open the special blocks menu to get a variety of inaccessible blocks.").build())
                .build());
        menu.getSlot(52).setClickHandler((clickPlayer, clickInformation) -> {
            if(clickPlayer.getWorld().getName().startsWith("P-")){
                new SpecialBlocksMenu().getUI().open(clickPlayer);
            } else {
                clickPlayer.sendMessage(Utils.getErrorMessageFormat("You need to be on a plot in order to use this"));
            }
        });

        // Set City Projects Items
        List<CityProject> cities = CityProject.getCityProjects();

        for (int i = 0; i < cities.size(); i++){
            int cityID = cities.get(i).getID();

            menu.getSlot(9+i).setClickHandler((clickPlayer, clickInformation) -> {
                try {
                    clickPlayer.closeInventory();
                    if (new Builder(clickPlayer.getUniqueId()).getFreeSlot() != null){
                        if (PlotManager.getPlots(cityID, Status.unclaimed).size() != 0){
                            clickPlayer.sendMessage(Utils.getInfoMessageFormat("Creating a new plot..."));
                            clickPlayer.playSound(clickPlayer.getLocation(), Utils.CreatePlotSound, 1, 1);

                            new PlotGenerator(cityID, new Builder(clickPlayer.getUniqueId()));
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
                                            cities.get(i).getDescription(),
                                            "",
                                            "§6" + PlotManager.getPlots(cityID, Status.unclaimed).size() + "§7 plots open",
                                            "§6" + PlotManager.getPlots(cityID, Status.unfinished).size() + "§7 plots in progress",
                                            "§6" + PlotManager.getPlots(cityID, Status.complete).size() + "§7 plots completed",
                                            "",
                                            cities.get(i).getDifficulty() == Difficulty.EASY ?
                                                "§a§lEasy" : cities.get(i).getDifficulty() == Difficulty.MEDIUM ?
                                                "§6§lMedium" : "§c§lHard")
                                    .build())
                            .build());
        }
    }

    private ItemStack getCityProjectItem(String HeadID) {
        return (headDB != null) ? headDB.getItemHead(HeadID) : new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
    }

    public static ItemStack getItem(){
        return new ItemBuilder(Material.NETHER_STAR, 1)
                .setName("§b§lCompanion §7(Right Click)")
                .setEnchantment(Enchantment.ARROW_DAMAGE)
                .setItemFlag(ItemFlag.HIDE_ENCHANTS)
                .build();
    }
}