package com.alpsbte.plotsystem.core.network;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.ArrayList;

public class LockedEvents implements Listener{
	public static boolean physics = true;
	public static ArrayList<Player> playerInventoryOpen = new ArrayList<Player>();

	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent e){
		e.setCancelled(true);
	}

	@EventHandler
	public void onOpen(InventoryOpenEvent e){
		Player p = (Player) e.getPlayer();
		playerInventoryOpen.remove(p);
		playerInventoryOpen.add(p);
	}

	@EventHandler
	public void onClose(InventoryCloseEvent e){
		Player p = (Player) e.getPlayer();

		playerInventoryOpen.remove(p);
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();

		if(!e.getPlayer().getLocation().getWorld().getName().equals(Utils.getSpawnLocation().getWorld().getName()) && p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR) {
			p.setGameMode(GameMode.CREATIVE);		
			p.setFlying(true);
		}
		
		if(p.getLocation().getY() < Settings.SPAWN_MIN_HEIGHT && p.getWorld().getName().equals(Utils.getSpawnLocation().getWorld().getName())){
			if(JumpPlates.pressurePlates.containsKey(p)){
				if(JumpPlates.pressurePlates.get(p).equals("Beginner"))
					p.teleport(Settings.SPAWN_BEGINNER);
				else if(JumpPlates.pressurePlates.get(p).equals("Advanced"))
					p.teleport(Settings.SPAWN_ADVANCED);
				else if(JumpPlates.pressurePlates.get(p).equals("Professional"))
					p.teleport(Settings.SPAWN_PROFESSIONAL);
				else
					p.teleport(Utils.getSpawnLocation());

				JumpPlates.pressurePlates.remove(p);
			}else
				p.teleport(Utils.getSpawnLocation());
		}

	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e){
		e.setDeathMessage(null);
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent e){
		if(e.getCause() == DamageCause.ENTITY_EXPLOSION)
			e.setCancelled(true);
		if(e.getCause() != DamageCause.ENTITY_ATTACK & e.getCause() != DamageCause.VOID){
		e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onSpawn(EntitySpawnEvent e){
		e.setCancelled(true);
	}
	
	@EventHandler
	public void FireIgnite(BlockIgniteEvent e){
		if(e.getCause() != IgniteCause.FLINT_AND_STEEL){
		e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onExplode(EntityExplodeEvent e) {
		e.blockList().clear();
	}
	
	@EventHandler
	public void bucketEmpty(PlayerBucketEmptyEvent e) {
		if(!e.getPlayer().hasPermission(Permissions.PermAdmin)) {
			e.setCancelled(true);
			return;
		}
	}
	
	@EventHandler
	public void onEnter(PlayerPortalEvent e){
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e){
		if(e.getWhoClicked().getGameMode() != GameMode.CREATIVE)
		try{
		if(e.getClickedInventory().getType() == InventoryType.PLAYER | e.getAction() == InventoryAction.HOTBAR_SWAP){
			e.setCancelled(true);
		}
		}catch(NullPointerException ex){}
	}
	
	@EventHandler
	public void onDrop(PlayerDropItemEvent e){
		if(!e.getPlayer().hasPermission(Permissions.PermAdmin))
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onPickup(EntityPickupItemEvent e){
		if(e.getEntity() instanceof Player)		
		if(!((Player)e.getEntity()).hasPermission(Permissions.PermAdmin))
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onDamage(HangingBreakByEntityEvent e){
		if(e.getRemover() instanceof Player){
			Player p = (Player) e.getRemover();
			
			if((p.getGameMode() != GameMode.CREATIVE &! p.hasPermission(Permissions.PermAdmin))){
				e.setCancelled(true);
			}
		}else{
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onDamageEntity(EntityDamageByEntityEvent e){
		if(e.getDamager() instanceof Player){
			Player p = (Player) e.getDamager();
			
			if(!p.hasPermission(Permissions.PermAdmin)){
				e.setCancelled(true);
			}
		}else{
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInteractEntity(PlayerInteractAtEntityEvent e){
		if((e.getPlayer().getGameMode() != GameMode.CREATIVE &! e.getPlayer().hasPermission(Permissions.PermAdmin))){
			e.setCancelled(true);
			
			if(e.getRightClicked() instanceof ItemFrame){
				ItemFrame frame = (ItemFrame) e.getRightClicked();
				
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PlotSystem.getPlugin(), new Runnable() {
					Rotation rotation = frame.getRotation();
					@Override
					public void run() {
						frame.setRotation(rotation);
					}
				},1);
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onBedEnter(PlayerBedEnterEvent e) {
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent e){
		e.setCancelled(true);
	}
}
