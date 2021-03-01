package github.BTEPlotSystem.core.menus;

import github.BTEPlotSystem.core.plots.Plot;
import github.BTEPlotSystem.core.plots.PlotManager;
import github.BTEPlotSystem.utils.Builder;
import github.BTEPlotSystem.utils.ItemBuilder;
import github.BTEPlotSystem.utils.LoreBuilder;
import github.BTEPlotSystem.utils.Utils;
import github.BTEPlotSystem.utils.enums.Category;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import org.ipvp.canvas.type.ChestMenu;

import java.sql.SQLException;
import java.util.List;

public class PlayerPlotsMenu {
    private final Menu menu;
    private final Player player;
    private final Builder builder;

    public PlayerPlotsMenu(Player player) throws SQLException {
        this.player = player;
        this.builder = new Builder(player.getUniqueId());
        menu = ChestMenu.builder(6).title(builder.getName() + "'s Plots").build();

        Mask mask = BinaryMask.builder(menu)
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("111101111")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("111101111")
                .build();
        mask.apply(menu);

        setMenuItems();

        menu.open(player);
    }

    private void setMenuItems() throws SQLException {
        menu.getSlot(4)
                .setItem(new ItemBuilder(Material.SKULL_ITEM,1)
                        .setName("§l§6" + builder.getName()).setLore(new LoreBuilder()
                                .description("§6Points: §7"+builder.getScore(),"§6Completed builds: §7"+builder.getCompletedBuilds())
                                .build())
                        .build());
        menu.getSlot(49)
                .setItem(new ItemBuilder(Material.BARRIER, 1)
                        .setName("§c§lCLOSE")
                        .setLore(new LoreBuilder()
                                .description("§7Close the review menu")
                                .build())
                        .build());
        menu.getSlot(49).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
        });

        //Set all plot items
        List<Plot> plotList = PlotManager.getPlots(builder);
        int plotDisplayCount;
        if (plotList.size()>36){
            plotDisplayCount = 36;
        } else {
            plotDisplayCount = plotList.size();
        }
        for (int i = 0; i < plotDisplayCount; i++) {
            switch (plotList.get(i).getStatus()){
                case unfinished:
                    menu.getSlot(i+9)
                            .setItem(new ItemBuilder(Material.WOOL,1, (byte) 1)
                                    .setName("§l§6#"+ plotList.get(i).getID() + " | " + plotList.get(i).getCity().getName()).setLore(new LoreBuilder()
                                            .description("§bAccuracy: §7"+ plotList.get(i).getScore(Category.ACCURACY))
                                            .build())
                                    .build());
                    break;
                case unreviewed:
                    break;
            }

        }
    }
}
