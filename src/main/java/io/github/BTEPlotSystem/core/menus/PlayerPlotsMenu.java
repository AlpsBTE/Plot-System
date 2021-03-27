package github.BTEPlotSystem.core.menus;

import github.BTEPlotSystem.core.plots.Plot;
import github.BTEPlotSystem.core.plots.PlotManager;
import github.BTEPlotSystem.utils.Builder;
import github.BTEPlotSystem.utils.ItemBuilder;
import github.BTEPlotSystem.utils.LoreBuilder;
import github.BTEPlotSystem.utils.Utils;
import github.BTEPlotSystem.utils.enums.Category;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import org.ipvp.canvas.type.ChestMenu;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class PlayerPlotsMenu {
    private final Menu menu;
    private final Builder builder;

    public PlayerPlotsMenu(Builder builder) throws SQLException {
        this(builder,builder.getPlayer());
    }

    public PlayerPlotsMenu(Builder builder, Player openPlayer) throws SQLException {
        this.builder = builder;
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
        menu.open(openPlayer);
    }

    private void setMenuItems() throws SQLException {
        menu.getSlot(4)
                .setItem(new ItemBuilder(Material.SKULL_ITEM, 1, (byte) 3)
                        .setName("§6§l" + builder.getName()).setLore(new LoreBuilder()
                                .description("§7", "Points: §f" + builder.getScore(), "§7Completed Buildings: §f" + builder.getCompletedBuilds())
                                .build())
                        .build());

        menu.getSlot(49)
                .setItem(new ItemBuilder(Utils.headDatabaseAPI.getItemHead("9219"))
                        .setName("§6§lBACK")
                        .setLore(new LoreBuilder()
                                .description("§7", "Back to the Companion Menu")
                                .build())
                        .build());
        menu.getSlot(49).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
        });

        //Set all plot items
        List<Plot> plotList = PlotManager.getPlots(builder);
        int plotDisplayCount = Math.min(plotList.size(), 36);

        for (int i = 0; i < plotDisplayCount; i++) {
            switch (plotList.get(i).getStatus()){
                case unfinished:
                    menu.getSlot(i+9)
                            .setItem(new ItemBuilder(Material.WOOL,1, (byte) 1)
                                    .setName("§b§l" + plotList.get(i).getCity().getName() + " | Plot #" + plotList.get(i).getID()).setLore(getDescription(plotList.get(i)))
                                    .build());
                    break;
                case unreviewed:
                    menu.getSlot(i+9)
                            .setItem(new ItemBuilder(Material.MAP,1)
                                    .setName("§b§l" + plotList.get(i).getCity().getName() + " | Plot #" + plotList.get(i).getID()).setLore(getDescription(plotList.get(i)))
                                    .build());
                    break;
                case complete:
                    menu.getSlot(i+9)
                            .setItem(new ItemBuilder(Material.WOOL,1, (byte) 13)
                                    .setName("§b§l" + plotList.get(i).getCity().getName() + " | Plot #" + plotList.get(i).getID()).setLore(getDescription(plotList.get(i)))
                                    .build());
                    break;
            }

            menu.getSlot(i + 9).setClickHandler((clickPlayer, clickInformation) -> {
                try {
                    new PlotActionsMenu(plotList.get(clickInformation.getClickedSlot().getIndex()-9),clickPlayer);
                } catch (SQLException ex) {
                    clickPlayer.sendMessage(Utils.getErrorMessageFormat("An error occurred! Please try again!"));
                    Bukkit.getLogger().log(Level.SEVERE, "An SQL error occurred!", ex);
                }
            });
        }
    }

    private List<String> getDescription(Plot plot) throws SQLException {
        List<String> lines = new ArrayList<>();
        lines.add("§7Total Points: §f" + (plot.getScore() == -1 ? 0 : plot.getScore()));
        if (plot.isReviewed() || plot.wasRejected()){
            lines.add("");
            lines.add("§7Accuracy: " + Utils.getPointsByColor(plot.getReview().getRating(Category.ACCURACY)) + "§8/§a5");
            lines.add("§7Block Palette: " + Utils.getPointsByColor(plot.getReview().getRating(Category.BLOCKPALETTE)) + "§8/§a5");
            lines.add("§7Detailing: " + Utils.getPointsByColor(plot.getReview().getRating(Category.DETAILING)) + "§8/§a5");
            lines.add("§7Technique: " + Utils.getPointsByColor(plot.getReview().getRating(Category.TECHNIQUE)) + "§8/§a5");
            lines.add("");
            lines.add("§7Feedback:§f");
            lines.addAll(Utils.splitText(plot.getReview().getFeedback()));
        }
        lines.add("");
        lines.add("§6§lStatus: §7§l" + plot.getStatus().name().substring(0, 1).toUpperCase() + plot.getStatus().name().substring(1));
        return lines;
    }
}
