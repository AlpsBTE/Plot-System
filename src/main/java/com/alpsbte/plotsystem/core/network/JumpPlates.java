package com.alpsbte.plotsystem.core.network;

import com.alpsbte.plotsystem.PlotSystem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;

public class JumpPlates implements Listener {

	public ArrayList<Player> lock = new ArrayList<Player>();
	public static HashMap<Player, String> pressurePlates = new HashMap<Player, String>();

	@EventHandler
	public void onPressurePlate(PlayerMoveEvent e) {
		Block b = e.getPlayer().getLocation().getBlock();
		Player p = e.getPlayer();

		if(lock.contains(p))
			return;

		if(b.getType() == Material.GOLD_PLATE){
			Location loc = b.getLocation();
			loc.setY(loc.getY() - 2);
			
			double pushY = 0;
			double pushX = 0;

			lock.add(p);
			

			p.playSound(p.getLocation(), Sound.ENTITY_ENDERDRAGON_FLAP, 1.0F, 1.0F);

			pressurePlates.remove(p);
			if(loc.getBlock().getType() == Material.IRON_BLOCK) {
				pushY = 1.2;
				pushX = -10;
				p.setVelocity(new Vector(0,1,0));
				pressurePlates.put(p, "Beginner");
			}else if(loc.getBlock().getType() == Material.GOLD_BLOCK) {
				pushY = 2.5;
				pushX = -10;
				p.setVelocity(new Vector(-1,1,0));
				pressurePlates.put(p, "Advanced");
			}else if(loc.getBlock().getType() == Material.DIAMOND_BLOCK) {
				pushY = 3.0;
				pushX = -10;
				p.setVelocity(new Vector(-1,1,0));
				pressurePlates.put(p, "Professional");
			}else
				return;

			final double pX = pushX, pY = pushY;

			Bukkit.getScheduler().scheduleSyncDelayedTask(PlotSystem.getPlugin(), new Runnable() {
				@Override
				public void run() {
					p.setVelocity(new Vector(pX,pY,0));
					lock.remove(p);
				}
			}, 15);
		}
	}
	
}
