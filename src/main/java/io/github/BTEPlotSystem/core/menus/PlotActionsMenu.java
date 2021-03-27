package github.BTEPlotSystem.core.menus;

import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.core.system.plot.PlotHandler;
import github.BTEPlotSystem.utils.ItemBuilder;
import github.BTEPlotSystem.utils.LoreBuilder;
import github.BTEPlotSystem.utils.enums.Status;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import org.ipvp.canvas.type.ChestMenu;

import java.sql.SQLException;
import java.util.logging.Level;

public class PlotActionsMenu {
    private final Menu menu;

    private final Player player;
    private final Plot plot;

    public PlotActionsMenu(Plot plot, Player player) throws SQLException {
        this.plot = plot;
        this.player = player;

        this.menu = ChestMenu.builder(3).title("Plot #" + plot.getID() + " | " + plot.getStatus().name().substring(0, 1).toUpperCase() + plot.getStatus().name().substring(1)).build();

        Mask mask = BinaryMask.builder(menu)
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("111111111")
                .pattern("000000000")
                .pattern("111111111")
                .build();

        mask.apply(menu);

        setMenuItems();

        menu.open(player);
    }

    private void setMenuItems() throws SQLException {

        boolean additionalSlot = plot.isReviewed() || plot.wasRejected();

        try {
            if (plot.getStatus().equals(Status.unreviewed)) {
                // Set Undo Submit Item
                menu.getSlot(10)
                        .setItem(new ItemBuilder(Material.FIREBALL, 1)
                                .setName("§c§lUndo Submit").setLore(new LoreBuilder()
                                        .description("§7", "Click to undo your submission.")
                                        .build())
                                .build());
                menu.getSlot(10).setClickHandler((clickPlayer, clickInformation) -> {
                    clickPlayer.closeInventory();
                    clickPlayer.performCommand("undosubmit " + plot.getID());
                });
            } else {
                // Set Finish Plot Item
                menu.getSlot(10)
                        .setItem(new ItemBuilder(Material.NAME_TAG, 1)
                                .setName("§a§lSubmit").setLore(new LoreBuilder()
                                        .description("§7", "Click to complete the selected plot and submit it to be reviewed.",
                                                "",
                                                "§c§lNote: §7You won't be able to continue building on your plot!")
                                        .build())
                                .build());
                menu.getSlot(10).setClickHandler((clickPlayer, clickInformation) -> {
                    clickPlayer.closeInventory();
                    clickPlayer.performCommand("submit " + plot.getID());
                });
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An database error occurred while completing a plot (ID: " + plot.getID() + ")!", ex);
        }


        // Set Teleport To Plot Item
        menu.getSlot(additionalSlot ? 12 : 13)
                .setItem(new ItemBuilder(Material.COMPASS, 1)
                        .setName("§6§lTeleport").setLore(new LoreBuilder()
                                .description("§7", "Click to teleport to the plot.")
                                .build())
                        .build());
        menu.getSlot(additionalSlot ? 12 : 13).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            PlotHandler.teleportPlayer(plot, clickPlayer);
        });

        // Set Abandon Plot Item
        menu.getSlot(additionalSlot ? 14 : 16)
                .setItem(new ItemBuilder(Material.BARRIER, 1)
                        .setName("§c§lAbandon").setLore(new LoreBuilder()
                                .description("§7",
                                        "Click to reset your plot and give it to someone else.",
                                        "",
                                        "§c§lNote: §7You won't be able to continue building on your plot!")
                                .build())
                        .build());
        menu.getSlot(additionalSlot ? 14 : 16).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            clickPlayer.performCommand("abandon " + plot.getID());
        });

        // Set Feedback Item
        if (additionalSlot) {
            menu.getSlot(16)
                    .setItem(new ItemBuilder(Material.BOOK_AND_QUILL)
                            .setName("§b§lFeedback").setLore(new LoreBuilder()
                                    .description("§7", "Click to view your plot review feedback.")
                                    .build())
                            .build());
            menu.getSlot(16).setClickHandler((clickPlayer, clickInformation) -> {
                clickPlayer.closeInventory();
                clickPlayer.performCommand("feedback " + plot.getID());
            });
        }
    }
}
