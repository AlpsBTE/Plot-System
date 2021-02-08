package github.BTEPlotSystem.core;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class EventListener extends SpecialBlocks implements Listener {

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event){
        Bukkit.broadcastMessage("§7[§6+§7] > " + event.getPlayer().getName());
        event.setJoinMessage(null);

        if (!event.getPlayer().getInventory().contains(Companion.getItem())){
            event.getPlayer().getInventory().setItem(0, Companion.getItem());
        }
    }

    @EventHandler
    public void onPlayerLeaveEvent(PlayerQuitEvent event){
        Bukkit.broadcastMessage("§7[§c-§7] > " + event.getPlayer().getName());
        event.setQuitMessage(null);
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event){
        try {
            if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)){
                if (event.getItem().equals(Companion.getItem())){
                    new Companion(event.getPlayer());
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event){
        if (event.getCurrentItem().equals(Companion.getItem())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onlPlayerItemDropEvent(PlayerDropItemEvent event){
        try {
            if(event.getItemDrop().getItemStack().equals(Companion.getItem())) {
                event.setCancelled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
}
