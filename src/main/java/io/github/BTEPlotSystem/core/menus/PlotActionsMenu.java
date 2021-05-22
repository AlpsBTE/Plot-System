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

import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.core.system.plot.PlotHandler;
import github.BTEPlotSystem.utils.ItemBuilder;
import github.BTEPlotSystem.utils.LoreBuilder;
import github.BTEPlotSystem.utils.MenuItems;
import github.BTEPlotSystem.utils.enums.Status;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.sql.SQLException;
import java.util.logging.Level;

public class PlotActionsMenu extends AbstractMenu {

    private final Plot plot;

    private final boolean hasFeedback;

    public PlotActionsMenu(Player menuPlayer, Plot plot) throws SQLException {
        super(3, "Plot #" + plot.getID() + " | " + plot.getStatus().name().substring(0, 1).toUpperCase() + plot.getStatus().name().substring(1), menuPlayer);
        this.plot = plot;

        Mask mask = BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("111111111")
                .pattern("000000000")
                .pattern("111111111")
                .build();
        mask.apply(getMenu());

        hasFeedback = plot.isReviewed() || plot.isRejected();

        addMenuItems();
        setItemClickEvents();

        getMenu().open(getMenuPlayer());
    }

    @Override
    protected void addMenuItems() {
        // Add submit/undo submit plot button
        try {
            if (plot.getStatus().equals(Status.unreviewed)) {
                getMenu().getSlot(10)
                        .setItem(new ItemBuilder(Material.FIREBALL, 1)
                                .setName("§c§lUndo Submit").setLore(new LoreBuilder()
                                        .addLine("Click to undo your submission").build())
                                .build());
            } else {
                getMenu().getSlot(10)
                        .setItem(new ItemBuilder(Material.NAME_TAG, 1)
                                .setName("§a§lSubmit").setLore(new LoreBuilder()
                                        .addLines("Click to complete the selected plot and submit it to be reviewed.",
                                                  "",
                                                  "§c§lNote: §7You won't be able to continue building on your plot!")
                                        .build())
                                .build());
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            getMenu().getSlot(10).setItem(MenuItems.errorItem());
        }

        // Add teleport to plot button
        getMenu().getSlot(hasFeedback ? 12 : 13)
                .setItem(new ItemBuilder(Material.COMPASS, 1)
                        .setName("§6§lTeleport").setLore(new LoreBuilder()
                                .addLine("Click to teleport to the plot").build())
                        .build());

        // Add abandon plot button
        getMenu().getSlot(hasFeedback ? 14 : 16)
                .setItem(new ItemBuilder(Material.BARRIER, 1)
                        .setName("§c§lAbandon").setLore(new LoreBuilder()
                                .addLines("Click to reset your plot and give it to someone else.",
                                          "",
                                          "§c§lNote: §7You won't be able to continue building on your plot!")
                                .build())
                        .build());

        // Add feedback menu button
        if (hasFeedback) {
            getMenu().getSlot(16)
                    .setItem(new ItemBuilder(Material.BOOK_AND_QUILL)
                            .setName("§b§lFeedback").setLore(new LoreBuilder()
                                    .addLine("Click to view your plot review feedback").build())
                            .build());
        }
    }

    @Override
    protected void setItemClickEvents() {
        // Set click event for submit/undo submit button
        getMenu().getSlot(10).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            try {
                clickPlayer.performCommand((plot.getStatus().equals(Status.unreviewed) ? "undosubmit " : "submit ") + plot.getID());
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        });

        // Set click event for teleport to plot button
        getMenu().getSlot(hasFeedback ? 12 : 13).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            try {
                PlotHandler.teleportPlayer(plot, clickPlayer);
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        });

        // Set click event for abandon plot button
        getMenu().getSlot(hasFeedback ? 14 : 16).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            clickPlayer.performCommand("abandon " + plot.getID());
        });

        // Set click event for feedback menu button
        if(hasFeedback) {
            getMenu().getSlot(16).setClickHandler((clickPlayer, clickInformation) -> {
                clickPlayer.closeInventory();
                clickPlayer.performCommand("feedback " + plot.getID());
            });
        }
    }
}
