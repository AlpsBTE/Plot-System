package github.BTEPlotSystem.core;

import github.BTEPlotSystem.core.plots.Plot;
import github.BTEPlotSystem.core.plots.PlotHandler;
import github.BTEPlotSystem.core.plots.PlotManager;
import github.BTEPlotSystem.utils.*;
import github.BTEPlotSystem.utils.enums.Slot;
import github.BTEPlotSystem.utils.enums.Status;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import org.ipvp.canvas.type.ChestMenu;

import java.sql.SQLException;
import java.util.List;

public class Companion {
    private Menu menu;
    private Mask mask;

    public Companion(Player player) throws SQLException {
        menu = createMenu();

        showItems(player);

        menu.open(player);
    }

    public Menu createMenu(){
        Menu menu = ChestMenu.builder(6).title("Companion").build();

        mask = BinaryMask.builder(menu)
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("111101111")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("100010001")
                .build();

        mask.apply(menu);
        return menu;
    }

    public void showItems(Player player) throws SQLException {
        // Set HUB
        //TODO: Set online player count
        menu.getSlot(4)
                .setItem(new ItemBuilder(Material.NETHER_STAR,1)
                .setName("§6§lHUB").setLore(new LoreBuilder().server(0,true).build()).build());
        menu.getSlot(4).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.performCommand("hub");
            //TODO: Add hub command
            clickPlayer.closeInventory();
        });

        // Set Slots
        for (int i = 0;i<3;i++){
            try {
                Plot plot = new Builder(player).getPlot(Slot.values()[i]);

                menu.getSlot(46+i)
                        .setItem(new ItemBuilder(Material.MAP,1+i)
                                .setName("§l§6Slot " + (i+1) + " | " + plot.getCity().getName() + " #" + plot.getID())
                                .setLore(new LoreBuilder().description("click to teleport...").build())
                                .build());
                menu.getSlot(46+i).setClickHandler((clickPlayer, clickInformation) -> {
                    clickPlayer.closeInventory();
                    try {
                        showPlotActionsMenu(clickPlayer, plot);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                        clickPlayer.sendMessage("§4Something went wrong... please contact a manager or developer");
                    }
                });
            } catch (Exception e) {
                menu.getSlot(46+i)
                        .setItem(new ItemBuilder(Material.EMPTY_MAP,1+i)
                                .setName("§l§aSlot " + (i+1) + " | Unasigned")
                                .setLore(new LoreBuilder().description("click on any of the cities to create a new plot").build())
                                .build());
            }
        }

        // Set Custom Heads
        menu.getSlot(50)
                .setItem(new ItemBuilder(Material.SKULL_ITEM, 1, (byte) 3)
                .setName("§b§lCUSTOM HEADS")
                .setLore(new LoreBuilder().description("Open the head menu to get a variety of custom heads.").build())
                .build());
        menu.getSlot(50).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            clickPlayer.performCommand("hdb");
        });

        // Set Custom Banners
        menu.getSlot(51)
                .setItem(new ItemBuilder(Material.BANNER, 1, (byte) 14)
                .setName("§b§lBANNER MAKER")
                .setLore(new LoreBuilder().description("Open the banner maker menu to create your own custom banners.").build())
                .build());
        menu.getSlot(51).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.performCommand("bm");
            clickPlayer.closeInventory();
        });

        // Set Custom Blocks
        menu.getSlot(52)
                .setItem(new ItemBuilder(Material.GOLD_BLOCK ,1)
                .setName("§b§lSPECIAL BLOCKS")
                .setLore(new LoreBuilder().description("Open the special blocks menu to get a variety of inaccessible blocks.").build())
                .build());
        menu.getSlot(52).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            new SpecialBlocksMenu().getUI().open(clickPlayer);
        });

        // List CityProjects
        List<CityProject> cities = CityProject.getCityProjects();
        HeadDatabaseAPI api = new HeadDatabaseAPI();
        for (int i = 0; i < cities.size(); i++){
            int cityID = cities.get(i).getID();
            menu.getSlot(9+i).setClickHandler((clickPlayer, clickInformation) -> {
                try {
                    if (new Builder(clickPlayer).getFreeSlot() != null){
                        if (PlotManager.getPlots(cityID, Status.unclaimed).size() != 0){
                            clickPlayer.sendMessage("creating new plot...");
                            PlotManager.ClaimPlot(cityID, new Builder(clickPlayer));
                        } else {
                            clickPlayer.sendMessage("§4This city doesn't have any open plots left... Please choose a different one or check again later!");
                        }
                    } else {
                        clickPlayer.sendMessage("§4All slots are occupied! Finish an active plot before starting a new project!");
                    }
                } catch (SQLException throwables) {
                    clickPlayer.sendMessage("§4SQL Error! :pepehands:");
                    throwables.printStackTrace();
                }
                clickPlayer.closeInventory();
            });

            switch (cities.get(i).getCountry()){
                case AT:
                    menu.getSlot(9+i)
                            .setItem(new ItemBuilder(api.getItemHead("4397"))
                                    .setName("§l§b" + cities.get(i).getName())
                                    .setLore(new LoreBuilder()
                                            .description(cities.get(i).getDescription(),"", "§6" + PlotManager.getPlots(cityID, Status.unclaimed).size() + "§7 open plots", "§6" + PlotManager.getPlots(cityID, Status.complete).size() + "§7 completed plots")
                                            .build())
                                    .build());
                    break;
                case CH:
                    menu.getSlot(9+i)
                            .setItem(new ItemBuilder(api.getItemHead("32348"))
                                    .setName("§l§b" + cities.get(i).getName())
                                    .setLore(new LoreBuilder()
                                            .description(cities.get(i).getDescription(),"", "§6" + PlotManager.getPlots(cityID, Status.unclaimed).size() + "§7 open plots", "§6" + PlotManager.getPlots(cityID, Status.complete).size() + "§7 completed plots")
                                            .build())
                                    .build());
                    break;
                case LI:
                    menu.getSlot(9+i)
                            .setItem(new ItemBuilder(api.getItemHead("26174"))
                                    .setName("§l§b" + cities.get(i).getName())
                                    .setLore(new LoreBuilder()
                                            .description(cities.get(i).getDescription(),"", "§6" + PlotManager.getPlots(cityID, Status.unclaimed).size() + "§7 open plots", "§6" + PlotManager.getPlots(cityID, Status.complete).size() + "§7 completed plots")
                                            .build())
                                    .build());
                    break;

            }
        }
    }

    public void showPlotActionsMenu(Player player, Plot plot) throws SQLException {
        //create menu
        Menu menu = ChestMenu.builder(3).title(plot.getCity().getName() + " #" + plot.getID() + " | " + plot.getStatus().name()).build();

        mask = BinaryMask.builder(menu)
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("111111111")
                .pattern("000000000")
                .pattern("111111111")
                .build();
        mask.apply(menu);

        //set items
        menu.getSlot(10)
                .setItem(new ItemBuilder(Material.NAME_TAG,1)
                        .setName("§a§lFinish").setLore(new LoreBuilder().description("Click to finish the selected plot and submit it to be reviewed...","§cNote: You wont be able to continue building on your Plot!").build()).build());
        menu.getSlot(10).setClickHandler((clickPlayer, clickInformation) -> {
            //TODO: Finish Plot
            player.sendMessage("finish plot soontm");
            clickPlayer.closeInventory();
        });

        menu.getSlot(13)
                .setItem(new ItemBuilder(Material.COMPASS,1)
                        .setName("§6§lTeleport").setLore(new LoreBuilder().description("click to teleport to your plot...").build()).build());
        menu.getSlot(13).setClickHandler((clickPlayer, clickInformation) -> {
            //TODO: Teleport to Plot
            PlotHandler.TeleportPlayer(plot,player);
            clickPlayer.closeInventory();
        });

        menu.getSlot(16)
                .setItem(new ItemBuilder(Material.BARRIER,1)
                        .setName("§c§lAbandon").setLore(new LoreBuilder().description("click to abandon your plot...","§cNote: You wont be able to continue building on your Plot!").build()).build());
        menu.getSlot(16).setClickHandler((clickPlayer, clickInformation) -> {
            //TODO: Abandon Plot
            player.sendMessage("abandon plot soontm");
            clickPlayer.closeInventory();
        });

        menu.open(player);
    }
}