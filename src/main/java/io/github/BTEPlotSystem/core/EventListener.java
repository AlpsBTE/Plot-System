package github.BTEPlotSystem.core;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.menus.CompanionMenu;
import github.BTEPlotSystem.core.menus.ReviewMenu;
import github.BTEPlotSystem.core.plots.Plot;
import github.BTEPlotSystem.core.plots.PlotHandler;
import github.BTEPlotSystem.core.plots.PlotManager;
import github.BTEPlotSystem.utils.Builder;
import github.BTEPlotSystem.utils.Utils;
import github.BTEPlotSystem.utils.enums.Status;
import me.arcaniax.hdb.api.DatabaseLoadEvent;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
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
import java.util.List;
import java.util.logging.Level;

public class EventListener extends SpecialBlocks implements Listener {

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event){
        event.setJoinMessage(null);
        event.getPlayer().teleport(Utils.getSpawnPoint());

        if (!event.getPlayer().getInventory().contains(CompanionMenu.getItem())){
            event.getPlayer().getInventory().setItem(8, CompanionMenu.getItem());
        }
        if (event.getPlayer().hasPermission("alpsbte.review")){
            if (!event.getPlayer().getInventory().contains(ReviewMenu.getItem())){
                event.getPlayer().getInventory().setItem(7, ReviewMenu.getItem());
            }
        }

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

        try {
            List<Plot> plots = PlotManager.getPlots(new Builder(event.getPlayer().getUniqueId()), Status.complete, Status.unfinished);

            boolean newMessage = false;
            for(Plot plot : plots) {
                System.out.println(plot.isReviewed() + " | " + plot.getReview().isFeedbackSent());
                if(plot.isReviewed() && !plot.getReview().isFeedbackSent()) {
                    TextComponent tc = new TextComponent();
                    tc.setText(Utils.getInfoMessageFormat("Your plot with the ID §6#" + plot.getID() + " §ahas been reviewed! §6Click Here §ato check your feedback."));
                    tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/feedback " + plot.getID()));
                    tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Feedback").create()));

                    event.getPlayer().spigot().sendMessage(tc);
                    plot.getReview().setFeedbackSent(true);
                    newMessage = true;
                }
            }

            if(newMessage) {
                event.getPlayer().playSound(event.getPlayer().getLocation(), Utils.FinishPlotSound, 1, 1);
            }
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.SEVERE, "An error occurred while trying to inform the player about his plot feedback!", ex);
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) throws SQLException {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)){
            if (event.getItem() != null && event.getItem().equals(CompanionMenu.getItem())){
                new CompanionMenu(event.getPlayer());
            } else if (event.getItem() != null && event.getItem().equals(ReviewMenu.getItem())){
                event.getPlayer().performCommand("review");
            }
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
        if (event.getCurrentItem() != null && event.getCurrentItem().equals(CompanionMenu.getItem())){
            event.setCancelled(true);
        }
        if (event.getCurrentItem() != null && event.getCurrentItem().equals(ReviewMenu.getItem())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onlPlayerItemDropEvent(PlayerDropItemEvent event){
        if(event.getItemDrop() != null && event.getItemDrop().getItemStack().equals(CompanionMenu.getItem())) {
            event.setCancelled(true);
        }
        if(event.getItemDrop() != null && event.getItemDrop().getItemStack().equals(ReviewMenu.getItem())) {
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
