package github.BTEPlotSystem.core;

import github.BTEPlotSystem.core.plots.Plot;
import github.BTEPlotSystem.core.plots.PlotHandler;
import github.BTEPlotSystem.core.plots.PlotManager;
import github.BTEPlotSystem.utils.ItemBuilder;
import github.BTEPlotSystem.utils.LoreBuilder;
import github.BTEPlotSystem.utils.enums.Status;
import javafx.application.Application;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Review implements Listener {
    private final Inventory reviewMenu;
    private Inventory reviewPlotMenu;

    //Review Inventory
    private ItemStack itemArrowLeft;
    private ItemStack itemArrowRight;
    private ItemStack itemClose;

    //Review Plot Inventory
    private ItemStack itemMap;
    private final ItemStack[] itemCategory = new ItemStack[4];
    private final ItemStack[] itemPointZero = new ItemStack[4];
    private final ItemStack[] itemPointOne = new ItemStack[4];
    private final ItemStack[] itemPointTwo = new ItemStack[4];
    private final ItemStack[] itemPointThree = new ItemStack[4];
    private final ItemStack[] itemPointFour = new ItemStack[4];
    private final ItemStack[] itemPointFive = new ItemStack[4];

    private final HashMap<Plot, ItemStack> plotItems = new HashMap<>();
    private Plot selectedPlot;

    private final Player player;

    public Review(Player player) throws SQLException {
        //Opens Review Menu, showing all plots in the given round.
        reviewMenu = Bukkit.createInventory(player,54,"Review Plots");
        this.player = player;

        ItemStack plotLoadError = new ItemBuilder(Material.BARRIER, 1)
                .setName("§cCould not load plot")
                .setLore(new LoreBuilder()
                        .description("Please contact a Manager or Developer!")
                        .build())
                .build();

        List<Plot> plots = PlotManager.getPlots(Status.unreviewed);
        plots.addAll(PlotManager.getPlots(Status.unfinished));

        int counter = 0;
        for(Plot plot : plots) {
            try {
                switch (plot.getStatus()){
                    case unfinished:
                        System.out.println(plot.getBuilder());
                        plotItems.put(plot, new ItemBuilder(Material.WOOL, 1, (byte) 1)
                                .setName("§6Manage Plot")
                                .setLore(new LoreBuilder()
                                        .description("§bID: §7" + plot.getID(),
                                                     "§bBuilder: §7" + plot.getBuilder().getName(),
                                                     "§bCity: §7" + plot.getCity().getName())
                                        .build())
                                .build());
                        break;
                    case unreviewed:
                         plotItems.put(plot, new ItemBuilder(Material.MAP, 1)
                                .setName("§6Review Plot")
                                .setLore(new LoreBuilder()
                                        .description("§bID: §7" + plot.getID(),
                                                     "§bBuilder: §7" + plot.getBuilder().getName(),
                                                     "§bCity: §7" + plot.getCity().getName())
                                        .build())
                                .build());
                        break;
                    default:
                        reviewMenu.setItem(counter, plotLoadError);
                        return;
                }
                reviewMenu.setItem(counter, plotItems.get(plot));
            } catch (Exception e) {
                reviewMenu.setItem(counter, plotLoadError);
                e.printStackTrace();
            }
            counter++;
        }

        for(int i = 45; i <= 52; i++) {
            switch (i) {
                case 46:
                    itemArrowLeft = new ItemBuilder(Material.ARROW, 1)
                            .setName("§l§6Previous Page")
                            .setLore(new LoreBuilder()
                                    .description("§7Show previous page")
                                    .build())
                            .build();
                    reviewMenu.setItem(i, itemArrowLeft);
                    break;
                case 49:
                    itemClose = new ItemBuilder(Material.BARRIER, 1)
                            .setName("§c§lCLOSE")
                            .setLore(new LoreBuilder()
                                    .description("§7Close the review menu")
                                    .build())
                            .build();
                    reviewMenu.setItem(i, itemClose);
                    break;
                case 52:
                    itemArrowRight = new ItemBuilder(Material.ARROW, 1)
                            .setName("§6§lNext Page")
                            .setLore(new LoreBuilder()
                                    .description("§7Show next page")
                                    .build())
                            .build();
                    reviewMenu.setItem(i, itemArrowRight);
                    break;
                default:
                    reviewMenu.setItem(i, new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build());
            }
        }
        player.openInventory(reviewMenu);
    }

    private void ReviewPlot(Plot plot){
        //TODO: Add Submit button
        reviewPlotMenu = Bukkit.createInventory(player,54,"Review Plot #" + plot.getID());

        for (int i = 0; i < 54; i++){
            reviewPlotMenu.setItem(i, new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build());

            switch (i){
                case 4:
                    itemMap = new ItemBuilder(Material.MAP, 1)
                            .setName("§l§6Plot #"+plot.getID()+" | ")
                            .setLore(new LoreBuilder()
                                    .description("§7Show previous page")
                                    .build())
                            .build();
                    reviewPlotMenu.setItem(i,itemMap);
                    break;
                case 10:
                    itemCategory[0] = new ItemBuilder(Material.ARROW, 1)
                            .setName("§a§lACCURACY")
                            .setLore(new LoreBuilder()
                                    .description("§7How accurate is the building?")
                                    .build())
                            .build();
                    reviewPlotMenu.setItem(i,itemCategory[0]);
                    break;
                case 19:
                    itemCategory[1] = new ItemBuilder(Material.PAINTING, 1)
                            .setName("§a§lBLOCK PALETTE")
                            .setLore(new LoreBuilder()
                                    .description("§7How many different blocks are used and how creative are they?")
                                    .build())
                            .build();
                    reviewPlotMenu.setItem(i,itemCategory[1]);
                    break;
                case 28:
                    itemCategory[2] = new ItemBuilder(Material.EYE_OF_ENDER, 1)
                            .setName("§a§lDETAILING")
                            .setLore(new LoreBuilder()
                                    .description("§7How much detail does the building have?")
                                    .build())
                            .build();
                    reviewPlotMenu.setItem(i,itemCategory[2]);
                    break;
                case 37:
                    itemCategory[3] = new ItemBuilder(Material.WOOD_AXE, 1)
                            .setName("§a§lTECHNIQUE")
                            .setLore(new LoreBuilder()
                                    .description("§7What building techniques have been used and how creative are they?")
                                    .build())
                            .build();
                    reviewPlotMenu.setItem(i,itemCategory[3]);
                    break;
                case 49:
                    itemClose = new ItemBuilder(Material.BARRIER, 1)
                            .setName("§c§lCLOSE")
                            .setLore(new LoreBuilder()
                                    .description("§7Close the review menu")
                                    .build())
                            .build();
                    reviewPlotMenu.setItem(i,itemClose);
                    break;
                default:
                    int column = (i%9)+1;
                    int row = (i - (i%9))/9+1;
                    if (column > 2 && column < 9 && row > 1 && row < 6) {
                        if ((i+1)%9 == 3){
                            itemPointZero[((i+1)-(i+1)%9)/54] = new ItemBuilder(Material.WOOL, 1,(byte) 8)
                                    .setName("§l§70 Points")
                                    .setLore(new LoreBuilder()
                                            .description("§7Click to select")
                                            .build())
                                    .build();
                            reviewPlotMenu.setItem(i,itemPointZero[(i-(i+1)%9)/54]);
                        } else if((i+1)%9 == 4) {
                            itemPointOne[((i + 1) - (i + 1) % 9) / 54] = new ItemBuilder(Material.WOOL, 1, (byte) 14)
                                    .setName("§l§c1 Point")
                                    .setLore(new LoreBuilder()
                                            .description("§7Click to select")
                                            .build())
                                    .build();
                            reviewPlotMenu.setItem(i,itemPointOne[(i-(i+1)%9)/54]);
                        } else if ((i+1)%9 == 5) {
                            itemPointTwo[((i + 1) - (i + 1) % 9) / 54] = new ItemBuilder(Material.WOOL, 2, (byte) 1)
                                    .setName("§l§62 Points")
                                    .setLore(new LoreBuilder()
                                            .description("§7Click to select")
                                            .build())
                                    .build();
                            reviewPlotMenu.setItem(i,itemPointTwo[(i-(i+1)%9)/54]);
                        } else if ((i+1)%9 == 6) {
                            itemPointThree[((i + 1) - (i + 1) % 9) / 54] = new ItemBuilder(Material.WOOL, 3, (byte) 4)
                                    .setName("§l§e3 Points")
                                    .setLore(new LoreBuilder()
                                            .description("§7Click to select")
                                            .build())
                                    .build();
                            reviewPlotMenu.setItem(i,itemPointThree[(i-(i+1)%9)/54]);
                        } else if ((i+1)%9 == 7) {
                            itemPointFour[((i + 1) - (i + 1) % 9) / 54] = new ItemBuilder(Material.WOOL, 4, (byte) 13)
                                    .setName("§l§24 Points")
                                    .setLore(new LoreBuilder()
                                            .description("§7Click to select")
                                            .build())
                                    .build();
                            reviewPlotMenu.setItem(i,itemPointFour[(i-(i+1)%9)/54]);
                        } else if ((i+1)%9 == 8) {
                            itemPointFive[((i + 1) - (i + 1) % 9) / 54] = new ItemBuilder(Material.WOOL, 5, (byte) 5)
                                    .setName("§l§c5 Points")
                                    .setLore(new LoreBuilder()
                                            .description("§7Click to select")
                                            .build())
                                    .build();
                            reviewPlotMenu.setItem(i,itemPointFive[(i-(i+1)%9)/54]);
                        }
                    }
                    break;
            }
        }
        player.openInventory(reviewPlotMenu);
    }

    @EventHandler
    public void onPlayerInventoryClickEvent(InventoryClickEvent event){
        try{
            if (event.getWhoClicked().equals(player)){
                if (event.getClickedInventory().equals(reviewMenu)){

                    if (event.getCurrentItem().equals(itemClose)) {
                        event.getWhoClicked().closeInventory();

                    } else if (event.getCurrentItem().equals(itemArrowLeft)){
                        player.sendMessage("§7>> :yellcat:");
                        event.getWhoClicked().closeInventory();

                    } else if (event.getCurrentItem().equals(itemArrowRight)){
                        player.sendMessage("§7>> :yellcat:");
                        event.getWhoClicked().closeInventory();
                    }

                    for (ItemStack plotItem : plotItems.values()) {
                        if (event.getCurrentItem().equals(plotItem)) {
                            selectedPlot = getPlotByValue(plotItem);
                            event.getWhoClicked().closeInventory();
                            ReviewPlot(selectedPlot);
                        }
                    }
                    event.setCancelled(true);
                } else if (event.getClickedInventory().equals(reviewPlotMenu)){
                    if (event.getCurrentItem().equals(itemClose)) {
                        event.getWhoClicked().closeInventory();
                    } else if (event.getCurrentItem().equals(itemMap)){
                        PlotHandler.TeleportPlayer(selectedPlot,player);
                        event.getWhoClicked().closeInventory();
                    } else {
                        int slot = event.getSlot();
                        int column = (slot%9)+1;
                        int row = (slot - (slot%9))/9+1;

                        if (column > 2 && column < 9 && row > 1 && row < 6){
                            if (event.getCurrentItem().containsEnchantment(Enchantment.ARROW_DAMAGE)){
                                event.getCurrentItem().getItemMeta().removeEnchant(Enchantment.ARROW_DAMAGE);
                            } else {
                                event.getCurrentItem().getItemMeta().addEnchant(Enchantment.ARROW_DAMAGE,1,true);
                            }
                        }
                        //TODO: Check and Set points
                    }
                    event.setCancelled(true);
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private Plot getPlotByValue(ItemStack item) {
        for(Map.Entry entry : plotItems.entrySet()) {
            if(entry.getValue().equals(item)) {
                return (Plot) entry.getKey();
            }
        }
        return null;
    }
}
