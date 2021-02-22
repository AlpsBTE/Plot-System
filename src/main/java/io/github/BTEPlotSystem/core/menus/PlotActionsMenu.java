package github.BTEPlotSystem.core.menus;

import github.BTEPlotSystem.core.plots.Plot;
import github.BTEPlotSystem.core.plots.PlotHandler;
import github.BTEPlotSystem.utils.ItemBuilder;
import github.BTEPlotSystem.utils.LoreBuilder;
import github.BTEPlotSystem.utils.Utils;
import github.BTEPlotSystem.utils.enums.Status;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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

        this.menu = ChestMenu.builder(3).title(plot.getCity().getName() + " #" + plot.getID() + " | " + plot.getStatus().name().substring(0, 1).toUpperCase() + plot.getStatus().name().substring(1)).build();

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

    private void setMenuItems() {

        boolean additionalSlot = player.hasPermission("alpsbte.removePlot");

        // Set Finish Plot Item
        menu.getSlot(10)
                .setItem(new ItemBuilder(Material.NAME_TAG,1)
                        .setName("§a§lFinish").setLore(new LoreBuilder()
                                .description("Click to complete the selected plot and submit it to be reviewed.",
                                             "",
                                             "§c§lNote: §7You won't be able to continue building on your plot!")
                                .build())
                        .build());
        menu.getSlot(10).setClickHandler((clickPlayer, clickInformation) -> {
            try {
                if(plot.getStatus().equals(Status.unreviewed) || plot.getStatus().equals(Status.complete)) {
                    clickPlayer.sendMessage(Utils.getErrorMessageFormat("Selected plot is already finished!"));
                    clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                } else {
                    PlotHandler.finishPlot(plot);
                    clickPlayer.sendMessage(Utils.getInfoMessageFormat("Finished plot with the ID §6#" + plot.getID()));
                    clickPlayer.playSound(clickPlayer.getLocation(), Utils.FinishPlotSound, 1, 1);
                }
            } catch (Exception ex) {
                clickPlayer.sendMessage(Utils.getErrorMessageFormat("An internal error occurred while completing the selected plot! Please try again or contact a staff member."));
                clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                Bukkit.getLogger().log(Level.SEVERE, "An database error occurred while completing a plot (ID: " + plot.getID() + ")!", ex);
            }
            clickPlayer.closeInventory();
        });

        // Set Teleport To Plot Item
        menu.getSlot(additionalSlot ? 12 : 13)
                .setItem(new ItemBuilder(Material.COMPASS,1)
                        .setName("§6§lTeleport").setLore(new LoreBuilder()
                                .description("Click to teleport to the plot.")
                                .build())
                        .build());
        menu.getSlot(additionalSlot ? 12 : 13).setClickHandler((clickPlayer, clickInformation) -> {
            PlotHandler.teleportPlayer(plot, player);
            clickPlayer.closeInventory();
        });

        // Set Abandon Plot Item
        menu.getSlot(additionalSlot ? 14 : 16)
                .setItem(new ItemBuilder(Material.BARRIER,1)
                        .setName("§c§lAbandon").setLore(new LoreBuilder()
                                .description("Click to reset your plot and give it to someone else.",
                                             "",
                                             "§c§lNote: §7You won't be able to continue building on your plot!")
                                .build())
                        .build());
        menu.getSlot(additionalSlot ? 14 : 16).setClickHandler((clickPlayer, clickInformation) -> {
            try {
                if(plot.getStatus().equals(Status.unreviewed) || plot.getStatus().equals(Status.complete)) {
                    clickPlayer.sendMessage(Utils.getErrorMessageFormat("Selected plot is already finished!"));
                    clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                } else {
                    PlotHandler.abandonPlot(plot);
                    clickPlayer.sendMessage(Utils.getInfoMessageFormat("Abandoned plot with the ID §6#" + plot.getID()));
                    clickPlayer.playSound(clickPlayer.getLocation(), Utils.AbandonPlotSound, 1, 1);
                }
            } catch (Exception ex) {
                clickPlayer.sendMessage(Utils.getErrorMessageFormat("An internal error occurred while abandoning the selected plot! Please try again or contact a staff member."));
                clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                Bukkit.getLogger().log(Level.SEVERE, "An database error occurred while abandoning a plot (ID: " + plot.getID() + ")!", ex);
            }
            clickPlayer.closeInventory();
        });

        // Set Delete Plot Item
        if(additionalSlot) {
            menu.getSlot(16)
                    .setItem(new ItemBuilder(Utils.headDatabaseAPI != null ? Utils.headDatabaseAPI.getItemHead("25108") : new ItemStack(Material.SKULL_ITEM, 1, (byte) 3))
                        .setName("§c§lDelete").setLore(new LoreBuilder()
                            .description("Click to delete the plot from the system.",
                                         "",
                                         "§c§lNote: §7You won't be able to restore the plot!")
                                    .build())
                            .build());
            menu.getSlot(16).setClickHandler((clickPlayer, clickInformation) -> {
                try {
                    PlotHandler.deletePlot(plot);
                    clickPlayer.sendMessage(Utils.getInfoMessageFormat("Deleted plot with the ID §6#" + plot.getID()));
                    clickPlayer.playSound(clickPlayer.getLocation(), Utils.AbandonPlotSound, 1, 1);
                } catch (Exception ex) {
                    clickPlayer.sendMessage(Utils.getErrorMessageFormat("An internal error occurred while deleting the selected plot! Please try again or contact a staff member."));
                    clickPlayer.playSound(clickPlayer.getLocation(), Utils.ErrorSound, 1, 1);
                    Bukkit.getLogger().log(Level.SEVERE, "An database error occurred while deleting a plot from the system (ID: " + plot.getID() + ")!", ex);
                }
                clickPlayer.closeInventory();
            });
        }
    }
}
