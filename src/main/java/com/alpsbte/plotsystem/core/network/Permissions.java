package com.alpsbte.plotsystem.core.network;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;


public class Permissions {
	public static String PermOwner = "server.owner";
	public static String PermAdmin = "server.admin";
	public static String PermModerator = "server.moderator";
	public static String PermSupporter = "server.supporter";
	public static String PermPremium = "server.premium";
	public static String PermYoutuber = "server.youtuber";
	public static String PermBuilder = "server.builder";
	public static String PermDeveloper = "server.developer";
	public static String PermDonator = "server.donator";
	public static String PermAdvanced = "server.advanced";
	public static String PermProfessional = "server.professional";
	
	public static boolean isMember(Player p) {
		return (
				 !p.hasPermission(Permissions.PermPremium)
				&!p.hasPermission(Permissions.PermAdmin) 
				&!p.hasPermission(Permissions.PermDeveloper) 
				&!p.hasPermission(Permissions.PermSupporter) 
				&!p.hasPermission(Permissions.PermOwner) 
				&!p.hasPermission(Permissions.PermBuilder)
				&!p.hasPermission(Permissions.PermYoutuber)
				&!p.hasPermission(Permissions.PermModerator)
		);
	}
	
	public static boolean isTeamMember(Player p) {
		return (
				 p.hasPermission(Permissions.PermAdmin) 
				|p.hasPermission(Permissions.PermDeveloper) 
				|p.hasPermission(Permissions.PermSupporter) 
				|p.hasPermission(Permissions.PermOwner) 
				|p.hasPermission(Permissions.PermBuilder)
				|p.hasPermission(Permissions.PermYoutuber)
				|p.hasPermission(Permissions.PermModerator)
		);
	}
	
	public static String getPrefixColorString(Player p){
		if(p.hasPermission(PermOwner)){
			return "§4";
		}else if(p.hasPermission(PermAdmin)){
			return "§c";
		}else if(p.hasPermission(PermModerator)){
			return "§3";
		}else if(p.hasPermission(PermDeveloper)){
			return "§b";
		}else if(p.hasPermission(PermSupporter)){
			return "§9";
		}else if(p.hasPermission(PermBuilder)){
			return "§1";
		}else if(p.hasPermission(PermYoutuber)){
			return "§5";
		}else if(p.hasPermission(PermPremium)){
			return "§6";
		}else{
			return "§a";
		}
	}
	
	public static ChatColor getPrefixChatColor(Player p){
		if(p.hasPermission(PermOwner)){
			return ChatColor.DARK_RED;
		}else if(p.hasPermission(PermAdmin)){
			return ChatColor.RED;
		}else if(p.hasPermission(PermModerator)){
			return ChatColor.DARK_AQUA;
		}else if(p.hasPermission(PermDeveloper)){
			return ChatColor.AQUA;
		}else if(p.hasPermission(PermSupporter)){
			return ChatColor.BLUE;
		}else if(p.hasPermission(PermBuilder)){
			return ChatColor.DARK_BLUE;
		}else if(p.hasPermission(PermYoutuber)){
			return ChatColor.DARK_PURPLE;
		}else if(p.hasPermission(PermPremium)){
			return ChatColor.GOLD;
		}else{
			return ChatColor.GREEN;
		}
	}
	
	public static Color getPrefixColor(Player p){
		if(p.hasPermission(PermOwner)){
			return Color.MAROON;
		}else if(p.hasPermission(PermAdmin)){
			return Color.RED;
		}else if(p.hasPermission(PermModerator)){
			return Color.TEAL;
		}else if(p.hasPermission(PermDeveloper)){
			return Color.AQUA;
		}else if(p.hasPermission(PermSupporter)){
			return Color.BLUE;
		}else if(p.hasPermission(PermBuilder)){
			return Color.NAVY;
		}else if(p.hasPermission(PermYoutuber)){
			return Color.PURPLE;
		}else if(p.hasPermission(PermPremium)){
			return Color.ORANGE;
		}else{
			return Color.LIME;
		}
	}
}
