package github.BTEPlotSystem.core;

import github.BTEPlotSystem.core.plots.Plot;
import github.BTEPlotSystem.utils.*;
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
        //Netherstar
        //TODO: Set active player count
        menu.getSlot(4)
                .setItem(new ItemBuilder(Material.NETHER_STAR,1)
                .setName("§6§lHUB").setLore(new LoreBuilder().server(0,true).build()).build());

        //Slots
        for (int i = 0;i<3;i++){
            try {
                Plot plot = new Builder(player).getPlot(Slot.values()[i]);
                menu.getSlot(46+i)
                        .setItem(new ItemBuilder(Material.MAP,1+i)
                                .setName("Slot " + i+1 + " | " + plot.getCity().getName() + " #" + plot.getID())
                                .setLore(new LoreBuilder().description("click to teleport...").build())
                                .build());
            } catch (Exception e) {
                menu.getSlot(46+i)
                        .setItem(new ItemBuilder(Material.EMPTY_MAP,1+i)
                                .setName("Slot " + i+1 + " | Unasigned")
                                .setLore(new LoreBuilder().description("click on any of the cities to create a new plot").build())
                                .build());
            }
        }

        //Custom Heads
        menu.getSlot(50)
                .setItem(new ItemBuilder(Material.SKULL_ITEM, 1, (byte) 3)
                .setName("§b§lCUSTOM HEADS")
                .setLore(new LoreBuilder().description("Open the head menu to get a variety of custom heads.").build())
                .build());

        //Custom Banners
        menu.getSlot(51)
                .setItem(new ItemBuilder(Material.BANNER, 1, (byte) 14)
                .setName("§b§lBANNER MAKER")
                .setLore(new LoreBuilder().description("Open the banner maker menu to create your own custom banners.").build())
                .build());

        //Custom Blocks
        menu.getSlot(52)
                .setItem(new ItemBuilder(Material.GOLD_BLOCK ,1)
                .setName("§b§lSPECIAL BLOCKS")
                .setLore(new LoreBuilder().description("Open the special blocks menu to get a variety of inaccessible blocks.").build())
                .build());

        //Show CityProjects

        List<CityProject> cities = CityProject.getCityProjects();
        for (int i = 0; i<cities.size();i++){
            switch (cities.get(i).getCountry()){
                //TODO: set correct heads as icons
                //TODO: get total open plot count to description
                case AT:
                    menu.getSlot(9+i)
                            .setItem(new ItemBuilder(Material.PORK)
                            .setName(cities.get(i).getName())
                            .setLore(new LoreBuilder().description(cities.get(i).getDescription()).build()).build());
                    break;
                case CH:
                    menu.getSlot(9+i)
                            .setItem(new ItemBuilder(Material.COOKIE)
                                    .setName(cities.get(i).getName())
                                    .setLore(new LoreBuilder().description(cities.get(i).getDescription()).build()).build());
                    break;
                case LI:
                    menu.getSlot(9+i)
                            .setItem(new ItemBuilder(Material.GOLD_HELMET)
                                    .setName(cities.get(i).getName())
                                    .setLore(new LoreBuilder().description(cities.get(i).getDescription()).build()).build());
                    break;
            }
        }
    }
}
