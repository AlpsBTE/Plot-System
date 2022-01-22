package com.alpsbte.plotsystem.core.network;

import com.alpsbte.plotsystem.PlotSystem;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import java.util.ArrayList;

public class DoubleJump implements Listener {
	public static ArrayList<Player> cooldown = new ArrayList<Player>();
	
	@EventHandler
	public void onPlayerToggleFlight(PlayerToggleFlightEvent e) {
		Player p = e.getPlayer();
		
		if(cooldown.contains(p) & p.getGameMode() != GameMode.CREATIVE){
			p.setAllowFlight(false);
			p.setFlying(false);
			e.setCancelled(true);
			return;
		}
		
		if(p.isFlying()){
			return;
		}
		
		try{
		if(p.hasPermission(Permissions.PermPremium)){
		if(p.getGameMode() == GameMode.CREATIVE) {
			p.setFlySpeed(0.1F);
			p.setAllowFlight(true);
			e.setCancelled(false);
			p.setFlying(true);
		}else {
			p.setAllowFlight(false);
			e.setCancelled(true);
			p.setFlySpeed(0.1F);
			p.setFlying(false);
						
			p.setVelocity(p.getLocation().getDirection().multiply(1.5).setY(1));
			p.playSound(p.getLocation(), Sound.ENTITY_ENDERDRAGON_FLAP, 1, 1);
		}	
		}else{
			if(p.getGameMode() == GameMode.CREATIVE) {
				p.setAllowFlight(true);
				e.setCancelled(false);
				p.setFlying(true);
			}else {
				p.setAllowFlight(false);
				e.setCancelled(true);
				p.setFlying(false);
			}
		}
		}catch(Exception ex){}
		
		cooldown.add(p);
		for(int i = 0; i <= 20; i++){
			giveExp(p, i);
		}
	}
	
	public void giveExp(Player p, int delay){
		Bukkit.getScheduler().scheduleSyncDelayedTask(PlotSystem.getPlugin(), new Runnable() {
			
			@Override
			public void run() {				
				if(delay >= 20){
					p.setFoodLevel(20);
					cooldown.remove(p);
				}else{
					p.setFoodLevel(delay);
				}
			}
		},delay);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		
		if(cooldown.contains(p) & p.getGameMode() != GameMode.CREATIVE){
			return;
		}
		
		if(p.hasPermission(Permissions.PermPremium)){
		if((p.getLocation().subtract(0, 1, 0).getBlock().getType() != Material.AIR)&!p.isFlying()){
			p.setAllowFlight(true);
			p.setFlySpeed(0.1F);
		}
		}		
	}
	
	@EventHandler
	public void onGamemode(PlayerGameModeChangeEvent e) {
		Player p = e.getPlayer();
		
		if(cooldown.contains(p) & p.getGameMode() != GameMode.CREATIVE){
			e.setCancelled(true);
			return;
		}
		
		if(p.getGameMode() == GameMode.CREATIVE) {
			p.setAllowFlight(true);
		}else{
			p.setAllowFlight(false);
		}
	}
}
