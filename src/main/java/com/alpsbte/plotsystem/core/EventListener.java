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

package com.alpsbte.plotsystem.core;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.utils.io.config.ConfigPaths;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.core.menus.CompanionMenu;
import com.alpsbte.plotsystem.core.menus.ReviewMenu;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotHandler;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.generator.DefaultPlotGenerator;
import com.alpsbte.plotsystem.utils.items.SpecialBlocks;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import me.arcaniax.hdb.api.DatabaseLoadEvent;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.TrapDoor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class EventListener extends SpecialBlocks implements Listener {

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        // Teleport Player to the spawn
        event.getPlayer().teleport(Utils.getSpawnLocation());

        // User has joined for the first time
        // Adding user to the database
        Bukkit.getScheduler().runTaskAsynchronously(PlotSystem.getPlugin(), () -> {
            // Add Items
            Utils.updatePlayerInventorySlots(event.getPlayer());

            // Check if player even exists in database.
            try (ResultSet rs = DatabaseConnection.createStatement("SELECT * FROM plotsystem_builders WHERE uuid = ?")
                    .setValue(event.getPlayer().getUniqueId().toString()).executeQuery()) {

                if(!rs.first()) {
                        DatabaseConnection.createStatement("INSERT INTO plotsystem_builders (uuid, name) VALUES (?, ?)")
                                .setValue(event.getPlayer().getUniqueId().toString())
                                .setValue(event.getPlayer().getName())
                                .executeUpdate();
                }

                DatabaseConnection.closeResultSet(rs);
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }

            // Inform player about update
            if (event.getPlayer().hasPermission("plotsystem.admin") && PlotSystem.UpdateChecker.updateAvailable() && PlotSystem.getPlugin().getConfigManager().getConfig().getBoolean(ConfigPaths.CHECK_FOR_UPDATES)) {
                event.getPlayer().sendMessage(Utils.getInfoMessageFormat("There is a new update for the Plot-System available. Check your console for more information!"));
                event.getPlayer().playSound(event.getPlayer().getLocation(), Utils.CreatePlotSound, 1f, 1f);
            }

            // Check if player has changed his name
            try {
                Builder builder = new Builder(event.getPlayer().getUniqueId());
                if (!builder.getName().equals(event.getPlayer().getName())) {
                    DatabaseConnection.createStatement("UPDATE plotsystem_builders SET name = ? WHERE uuid = ?")
                            .setValue(event.getPlayer().getName()).setValue(event.getPlayer().getUniqueId().toString()).executeUpdate();
                }
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }

            // Informing player about new feedback
            try {
                List<Plot> plots = PlotManager.getPlots(new Builder(event.getPlayer().getUniqueId()), Status.completed, Status.unfinished);
                List<Plot> reviewedPlots = new ArrayList<>();

                for(Plot plot : plots) {
                    if(plot.isReviewed() && !plot.getReview().isFeedbackSent() && plot.getPlotOwner().getPlayer().equals(event.getPlayer())) {
                        reviewedPlots.add(plot);
                        plot.getReview().setFeedbackSent(true);
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
            if(event.getPlayer().hasPermission("plotsystem.review")) {
                try {
                    List<Plot> unreviewedPlots = PlotManager.getPlots(Status.unreviewed);

                    if(unreviewedPlots.size() != 0) {
                        PlotHandler.sendUnreviewedPlotsReminderMessage(unreviewedPlots, event.getPlayer());
                    }
                } catch (Exception ex) {
                    Bukkit.getLogger().log(Level.SEVERE,"An error occurred while trying to inform the player about unreviewed plots!", ex);
                }
            }
        });
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)){
            if (event.getItem() != null && event.getItem().equals(CompanionMenu.getMenuItem(event.getPlayer()))){
                event.getPlayer().performCommand("companion");
            } else if (event.getItem() != null && event.getItem().equals(ReviewMenu.getMenuItem(event.getPlayer()))){
                event.getPlayer().performCommand("review");
            }
        }

        // Open/Close iron trap door when right-clicking
        if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (event.getHand() != EquipmentSlot.OFF_HAND) {
                if (!event.getPlayer().isSneaking()){
                    if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.IRON_TRAPDOOR) {
                        RegionContainer regionContainer = PlotSystem.DependencyManager.getWorldGuard().getRegionContainer();
                        RegionQuery query = regionContainer.createQuery();

                        if (query.testBuild(event.getPlayer().getLocation(), PlotSystem.DependencyManager.getWorldGuard().wrapPlayer(event.getPlayer()), DefaultFlag.INTERACT)) {
                            BlockState state = event.getClickedBlock().getState();
                            TrapDoor tp = (TrapDoor) state.getData();

                            if (!tp.isOpen()) {
                                tp.setOpen(true);
                                event.getPlayer().playSound(event.getClickedBlock().getLocation(), "block.iron_trapdoor.open", 1f, 1f);
                            } else {
                                tp.setOpen(false);
                                event.getPlayer().playSound(event.getClickedBlock().getLocation(), "block.iron_trapdoor.close", 1f, 1f);
                            }
                            state.update();
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) throws SQLException {
        if (event.getRightClicked().getType().equals(EntityType.PLAYER)) {
            event.getPlayer().performCommand("plots " + new Builder(event.getRightClicked().getUniqueId()).getName());
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        final World w = event.getPlayer().getWorld();

        Bukkit.getScheduler().scheduleSyncDelayedTask(PlotSystem.getPlugin(), () -> {
            try {
                Builder builder = new Builder(event.getPlayer().getUniqueId());

                if(PlotManager.isPlotWorld(w) && builder.getPlotTypeSetting().isPlayingAlone())
                    PlotManager.getCurrentPlot(builder).getWorld().unloadWorld(false);
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            }
            DefaultPlotGenerator.playerPlotGenerationHistory.remove(event.getPlayer().getUniqueId());

        }, 60L);
    }

    @EventHandler
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) throws SQLException {
        Builder builder = new Builder(event.getPlayer().getUniqueId());

        if(PlotManager.isPlotWorld(event.getPlayer().getWorld())
        && !event.getFrom().getWorld().equals(event.getTo().getWorld())
        && builder.getPlotTypeSetting().isPlayingAlone()
        ) {
            PlotManager.getCurrentPlot(builder).getWorld().unloadWorld(false);
        }
    }

    @EventHandler
    public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent event) throws SQLException {
        Builder builder = new Builder(event.getPlayer().getUniqueId());

        if (PlotManager.isPlotWorld(event.getFrom()) && builder.getPlotTypeSetting().isPlayingAlone() ) {
            PlotManager.getCurrentPlot(builder).getWorld().unloadWorld(false);
        }

        if (PlotManager.isPlotWorld(event.getPlayer().getWorld())) {
            event.getPlayer().getInventory().setItem(8, CompanionMenu.getMenuItem(event.getPlayer()));

            if (event.getPlayer().hasPermission("plotsystem.review")) {
                event.getPlayer().getInventory().setItem(7, ReviewMenu.getMenuItem(event.getPlayer()));
            }
        }
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event){
        if (event.getCurrentItem() != null && event.getCurrentItem().equals(CompanionMenu.getMenuItem((Player) event.getWhoClicked()))){
            event.setCancelled(true);
        }
        if (event.getCurrentItem() != null && event.getCurrentItem().equals(ReviewMenu.getMenuItem(((Player) event.getWhoClicked()).getPlayer()))){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onlPlayerItemDropEvent(PlayerDropItemEvent event){
        if(event.getItemDrop() != null && event.getItemDrop().getItemStack().equals(CompanionMenu.getMenuItem(event.getPlayer()))) {
            event.setCancelled(true);
        }
        if(event.getItemDrop() != null && event.getItemDrop().getItemStack().equals(ReviewMenu.getMenuItem(event.getPlayer()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBlockPlaceEvent(BlockPlaceEvent event) {
        if(event.canBuild()) {
            ItemStack item = event.getItemInHand();

            if(item.isSimilar(SeamlessSandstone)) {
                event.getBlockPlaced().setTypeIdAndData(43, (byte) 9, true);
            } else if(item.isSimilar(SeamlessRedSandstone)) {
                event.getBlockPlaced().setTypeIdAndData(181, (byte) 12, true);
            } else if(item.isSimilar(SeamlessStone)) {
                event.getBlockPlaced().setTypeIdAndData(43, (byte) 8, true);
            } else if(item.isSimilar(SeamlessMushroomStem)) {
                event.getBlockPlaced().setTypeIdAndData(99, (byte) 15, true);
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
        Utils.CustomHead.loadHeadsAsync(new HeadDatabaseAPI());
    }
}
