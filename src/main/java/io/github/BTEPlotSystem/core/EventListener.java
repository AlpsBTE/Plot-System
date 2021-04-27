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

package github.BTEPlotSystem.core;

import github.BTEPlotSystem.core.menus.CompanionMenu;
import github.BTEPlotSystem.core.menus.ReviewMenu;
import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.core.system.plot.PlotHandler;
import github.BTEPlotSystem.core.system.plot.PlotManager;
import github.BTEPlotSystem.core.system.Builder;
import github.BTEPlotSystem.utils.SpecialBlocks;
import github.BTEPlotSystem.utils.Utils;
import github.BTEPlotSystem.utils.enums.Status;
import me.arcaniax.hdb.api.DatabaseLoadEvent;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class EventListener extends SpecialBlocks implements Listener {

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event){
        // Remove Default Join Message
        event.setJoinMessage(null);
        // Teleport Player to the spawn
        event.getPlayer().teleport(Utils.getSpawnPoint());

        // Add Items
        if (!event.getPlayer().getInventory().contains(CompanionMenu.getMenuItem())){
            event.getPlayer().getInventory().setItem(8, CompanionMenu.getMenuItem());
        }
        if (event.getPlayer().hasPermission("alpsbte.review")){
            if (!event.getPlayer().getInventory().contains(ReviewMenu.getMenuItem())){
                event.getPlayer().getInventory().setItem(7, ReviewMenu.getMenuItem());
            }
        }

        // User has joined for the first time
        // Adding user to the database
        if(!event.getPlayer().hasPlayedBefore()) {
            try {
                PreparedStatement statement = DatabaseConnection.prepareStatement("INSERT INTO players (uuid, name) VALUES (?, ?)");
                statement.setString(1, event.getPlayer().getUniqueId().toString());
                statement.setString(2, event.getPlayer().getName());
                statement.execute();
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Could not add player [" + event.getPlayer().getName() + "] to database!", ex);
            }
        }

        // Informing player about new feedback
        try {
            List<Plot> plots = PlotManager.getPlots(new Builder(event.getPlayer().getUniqueId()), Status.complete, Status.unfinished);
            List<Plot> reviewedPlots = new ArrayList<>();

            for(Plot plot : plots) {
                if(plot.isReviewed() && !plot.getReview().isFeedbackSent()) {
                    reviewedPlots.add(plot);
                }
            }

            if(reviewedPlots.size() >= 1) {
                PlotHandler.sendFeedbackMessage(reviewedPlots, event.getPlayer());
                event.getPlayer().sendTitle("","§6§l" + reviewedPlots.size() + " §a§lPlot" + (reviewedPlots.size() == 1 ? " " : "s ") + (reviewedPlots.size() == 1 ? "has" : "have") + " been reviewed!", 20, 150, 20);
            }
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while trying to inform the player about his plot feedback!", ex);
        }

        // Informing player about unfinished plots
        try {
            List<Plot> plots = PlotManager.getPlots(new Builder(event.getPlayer().getUniqueId()), Status.unfinished);
            if(plots.size() >= 1) {
                PlotHandler.sendUnfinishedPlotReminderMessage(plots, event.getPlayer());
                event.getPlayer().sendMessage("");
            }
        } catch (Exception ex){
            Bukkit.getLogger().log(Level.SEVERE,"An error occurred while trying to inform the player about his unfinished plots!", ex);
        }

        // Informing reviewer about new reviews
        if(event.getPlayer().hasPermission("alpsbte.review")) {
            try {
                List<Plot> unreviewedPlots = PlotManager.getPlots(Status.unreviewed);

                if(unreviewedPlots.size() != 0) {
                    PlotHandler.sendUnreviewedPlotsReminderMessage(unreviewedPlots, event.getPlayer());
                }
            } catch (Exception ex) {
                Bukkit.getLogger().log(Level.SEVERE,"An error occurred while trying to inform the player about unreviewed plots!", ex);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)){
            if (event.getItem() != null && event.getItem().equals(CompanionMenu.getMenuItem())){
                new CompanionMenu(event.getPlayer());
            } else if (event.getItem() != null && event.getItem().equals(ReviewMenu.getMenuItem())){
                event.getPlayer().performCommand("review");
            }
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) throws SQLException {
        if (event.getRightClicked().getType().equals(EntityType.PLAYER)) {
            event.getPlayer().performCommand("plots " + new Builder(event.getRightClicked().getUniqueId()).getName());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuitEvent(PlayerQuitEvent event) throws SQLException {
        event.setQuitMessage(null);
        
        if(PlotManager.isPlotWorld(event.getPlayer().getWorld())) {
            PlotHandler.unloadPlot(PlotManager.getPlotByWorld(event.getPlayer().getWorld()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) throws SQLException {
        if(PlotManager.isPlotWorld(event.getPlayer().getWorld())) {
            PlotHandler.unloadPlot(PlotManager.getPlotByWorld(event.getPlayer().getWorld()));
        }
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event){
        if (event.getCurrentItem() != null && event.getCurrentItem().equals(CompanionMenu.getMenuItem())){
            event.setCancelled(true);
        }
        if (event.getCurrentItem() != null && event.getCurrentItem().equals(ReviewMenu.getMenuItem())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onlPlayerItemDropEvent(PlayerDropItemEvent event){
        if(event.getItemDrop() != null && event.getItemDrop().getItemStack().equals(CompanionMenu.getMenuItem())) {
            event.setCancelled(true);
        }
        if(event.getItemDrop() != null && event.getItemDrop().getItemStack().equals(ReviewMenu.getMenuItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBlockPlaceEvent(BlockPlaceEvent event) {
        if(event.canBuild()) {
            ItemStack item = event.getItemInHand();

            if(item.isSimilar(SeamlessSandstone)) {
                event.getBlockPlaced().setTypeIdAndData(43, (byte) 9, true);
            } else if(item.isSimilar(SeamlessStone)) {
                event.getBlockPlaced().setTypeIdAndData(43, (byte) 8, true);
            } else if(item.isSimilar(MushroomStem)) {
                event.getBlockPlaced().setTypeIdAndData(99, (byte) 10, true);
            } else if(item.isSimilar(LightBrownMushroom)) {
                event.getBlockPlaced().setTypeIdAndData(99, (byte) 0, true);
            } else if(item.isSimilar(BarkOakLog)) {
                event.getBlockPlaced().setTypeIdAndData(17, (byte) 12, true);
            } else if(item.isSimilar(BarkSpruceLog)) {
                event.getBlockPlaced().setTypeIdAndData(17, (byte) 13, true);
            } else if(item.isSimilar(BarkBirchLog)) {
                event.getBlockPlaced().setTypeIdAndData(17, (byte) 14, true);
            } else if(item.isSimilar(BarkJungleLog)) {
                event.getBlockPlaced().setTypeIdAndData(17, (byte) 15, true);
            } else if(item.isSimilar(BarkAcaciaLog)) {
                event.getBlockPlaced().setTypeIdAndData(162, (byte) 12, true);
            } else if(item.isSimilar(BarkDarkOakLog)) {
                event.getBlockPlaced().setTypeIdAndData(162, (byte) 13, true);
            }
        }
    }

    @EventHandler
    public void onDatabaseLoad(DatabaseLoadEvent event) {
       Utils.headDatabaseAPI = new HeadDatabaseAPI();
    }
}
