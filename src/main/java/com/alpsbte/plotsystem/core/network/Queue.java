package com.alpsbte.plotsystem.core.network;

import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.core.system.plot.generator.DefaultPlotGenerator;
import com.alpsbte.plotsystem.utils.enums.PlotDifficulty;
import com.alpsbte.plotsystem.utils.enums.Status;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Array;
import java.sql.SQLException;
import java.util.*;

public class Queue {

	public HashMap<UUID, PlotDifficulty> queue = new LinkedHashMap<UUID, PlotDifficulty>();
	public ArrayList<UUID> lock = new ArrayList<>();
	private long time;

	public Queue() {
		List<String> q = FileManager.getList("queue", "Queue");
		if(FileManager.getList("queue", "Queue").size() > 0)
		for(String s : q)
			queue.put(UUID.fromString(s), PlotDifficulty.EASY);
		
		//Bukkit.broadcastMessage("Queue loaded: " + queue.size() + " Players.");
		FileManager.set("queue", "Queue", null);
		
	}
	
	public void tick() {
		if(time %(20*5) == 0)
			lock.clear();

		HashMap<UUID, PlotDifficulty> players = new HashMap<UUID, PlotDifficulty>(queue);
		for(UUID uuid : players.keySet())
		if(!lock.contains(uuid)) {
			lock.add(uuid);
			Bukkit.broadcastMessage("UUID " + time);
			if (Bukkit.getPlayer(uuid) == null)
				queue.remove(uuid);
			else {
				try {
					check(Bukkit.getPlayer(uuid), players.get(uuid));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		time++;
	}
	
	public void check(Player p, PlotDifficulty difficulty) throws SQLException {
		if(p.hasPermission(Permissions.PermPremium)) {
			connect(p, difficulty);
			return;
		}
		
		if(queue.size() == 0) {
			queue.put(p.getUniqueId(), difficulty);
			return;
		}
		
		UUID uuid = null;
		for(UUID u : queue.keySet()) {
			uuid = u;
			break;
		}

		if(Bukkit.getPlayer(uuid) == null){
			queue.remove(uuid);
			return;
		}

		connect(Bukkit.getPlayer(uuid), difficulty);
				
		ArrayList<UUID> l = new ArrayList<UUID>();
		l.addAll(queue.keySet());
		if(l.contains(p.getUniqueId())) {
			sendStatus(p);
			return;
		}else {
			queue.put(p.getUniqueId(), difficulty);
			sendStatus(p);
			return;
		}		
	}
	
	public void sendStatus(Player p) {
		int i = 1;
		
		for(UUID uuid : queue.keySet())
		if(Bukkit.getPlayer(uuid) != null)
		if(Bukkit.getPlayer(uuid).getName().equals(p.getName()))
			break;
		else
			i++;

		if(queue.size() <= 1)
			return;

		p.sendMessage("§7You are currently in position §e" + i + "/" + queue.size() + "§7.");
	}
	
	public void connect(Player p, PlotDifficulty difficulty) throws SQLException {
		ArrayList<UUID> l = new ArrayList<UUID>();
		l.addAll(queue.keySet());
		for(UUID uuid : l) {
			if(Bukkit.getPlayer(uuid) == null)
				queue.remove(uuid);
			
			if(!Bukkit.getPlayer(uuid).isOnline())
				queue.remove(uuid);
		}

		queue.remove(p.getUniqueId());

		if(p == null)
			return;
		if(!p.isOnline())
			return;

		Builder builder = new Builder(p.getUniqueId());
		List<Plot> projects = PlotManager.getPlots(difficulty, Status.unclaimed);
		if(projects.size() == 0) {
			p.sendMessage("§cUnfortunately no projects are available in your category on this server at the moment. Connect to the other building servers (Netherstar), you'll find more projects there.");
			p.sendMessage("");
			return;
		}

		new DefaultPlotGenerator(projects.get(0).getID(), difficulty, builder);
	}
	
}
