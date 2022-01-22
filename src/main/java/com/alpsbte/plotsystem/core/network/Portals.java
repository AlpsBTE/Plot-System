package com.alpsbte.plotsystem.core.network;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Status;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class Portals {

	public HashMap<Location, PlotDifficulty> portalDifficulty = new HashMap<Location, PlotDifficulty>();
	public HashMap<Player, Integer> playerTimeInPortal = new HashMap<Player, Integer>();
	public ArrayList<UUID> lock = new ArrayList<>();
	private long time;

	public Portals() {
		portalDifficulty.put(Settings.PORTAL_BEGINNER, PlotDifficulty.EASY);
		portalDifficulty.put(Settings.PORTAL_ADVANCED, PlotDifficulty.MEDIUM);
		portalDifficulty.put(Settings.PORTAL_PROFESSIONAL, PlotDifficulty.HARD);
	}
	
	public void tick() throws SQLException {
		if(time %(20) == 0)
			lock.clear();

		for(Player p : Bukkit.getOnlinePlayers()) {
			if(!p.getLocation().getWorld().getName().equals(Utils.getSpawnLocation().getWorld().getName()))
				continue;
			
			for(Location location : portalDifficulty.keySet()) {
				Location playerXZ = p.getLocation().clone();
				Location locXZ = location.clone();
				playerXZ.setY(0);
				locXZ.setY(0);

				if(!p.getLocation().getWorld().getName().equals(Utils.getSpawnLocation().getWorld().getName()))
					continue;
				
				double distance = playerXZ.distanceSquared(locXZ);
				if(distance < 1) {
					Builder builder = new Builder(p.getUniqueId());

					if(PlotManager.getPlots(builder, Status.unfinished).size() == 0){
						List<Plot> projects = PlotManager.getPlots(Status.unclaimed);
						if(projects.size() == 0) {
							p.sendMessage("§cUnfortunately there are no projects avaiable at the moment. Go on the other building servers (Netherstar), you'll find more there.");
							continue;
						}

						PlotDifficulty difficulty = portalDifficulty.get(location);
						
						if(difficulty == PlotDifficulty.HARD && ! p.hasPermission(Permissions.PermProfessional) &&! Permissions.isTeamMember(p)) {
							p.sendMessage("§cOnly professionals are able to use this portal. Level up by completing projects to get this rank.");
							continue;
						}
						
						if(difficulty == PlotDifficulty.MEDIUM && ! p.hasPermission(Permissions.PermAdvanced) &&! Permissions.isTeamMember(p)) {
							p.sendMessage("§cOnly advanced users are able to use this portal. Level up by completing projects to get this rank.");
							continue;
						}

						if(!lock.contains(p.getUniqueId())) {
							PlotSystem.queue.check(p, difficulty);
							lock.add(p.getUniqueId());
						}
						
					}else {
						List<Plot> plotsWithDifficulty = PlotManager.getPlots(builder, portalDifficulty.get(location), Status.unfinished);
						List<Plot> plotsWithoutDifficulty = PlotManager.getPlots(builder, Status.unfinished);

						if(plotsWithDifficulty.size() > 0)
							plotsWithDifficulty.get(0).getWorld().teleportPlayer(p);
						else if(plotsWithoutDifficulty.size() > 0)
							plotsWithoutDifficulty.get(0).getWorld().teleportPlayer(p);
					}				
				}
				
				if(distance < 30*30) 
					for(int i = 0; i < 5; i++)
						for(double y = location.getY(); y < location.getY() + 30; y += 1) {
								Location loc = location.clone();
								loc.setY(y);
								loc.add(Math.cos((time+y*15)/10) / 2, 0, Math.sin((time+y*15)/10) / 2);
								p.spawnParticle(Particle.PORTAL, loc,0, 0, 0, 0);
							
							}
				
			}
		}
		time++;
	}
}
