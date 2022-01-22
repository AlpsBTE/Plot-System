package com.alpsbte.plotsystem.core.network;

import com.alpsbte.plotsystem.PlotSystem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileManager {
	private static HashMap<String, File> files = new HashMap<String, File>();
	
	public static File getFile(String file){
	    return getFile("", file);
	}
	
	public static File getFile(String dir, String file){
		if(!files.containsKey(dir + file))
			files.put(dir + file, new File(PlotSystem.getPlugin().getDataFolder() + dir, file + ".yml"));
		
	    return files.get(dir + file);
	}	
	   
    public static FileConfiguration getFileConfiguration(String file){
        return getFileConfiguration("", file);
    }
    
    public static FileConfiguration getFileConfiguration(String dir, String file){
        return YamlConfiguration.loadConfiguration(getFile(dir, file));
    }
	
	
	public static boolean deleteFile(String file){
		return getFile(file).delete();
	}
	
	public static boolean deleteFile(String dir, String file){
		return getFile(dir, file).delete();
	}
	
	
	public static boolean fileExists(String file){
		return getFile(file).exists();
	}
	
	public static boolean fileExists(String dir, String file){
		return getFile(dir, file).exists();
	}
	
	
	public static void set(String file, String path, Object value){
		set("", file, path, value);
	}
	
	public static void set(String dir, String file, String path, Object value){
		FileConfiguration cfg = getFileConfiguration(dir, file);
		cfg.set(path, value);
		try {
			cfg.save(getFile(dir, file));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static Object get(String file, String path){
		FileConfiguration cfg = getFileConfiguration(file);
		return cfg.get(path);
	}
	
	public static Object get(String dir, String file, String path){
		FileConfiguration cfg = getFileConfiguration(dir, file);
		return cfg.get(path);
	}
	
	
	public static String getString(String file, String path){
		FileConfiguration cfg = getFileConfiguration(file);
		String s = cfg.getString(path);
		if(s != null)
			s = s.replace("&", "§");
		return s;
	}
	
	public static String getString(String dir, String file, String path){
		FileConfiguration cfg = getFileConfiguration(dir, file);
		String s = cfg.getString(path);
		if(s != null)
			s = s.replace("&", "§");
		return s;
	}
	
	
	public static Integer getInt(String file, String path){
		FileConfiguration cfg = getFileConfiguration(file);
		return cfg.getInt(path);
	}
	
	public static Integer getInt(String dir, String file, String path){
		FileConfiguration cfg = getFileConfiguration(dir, file);
		return cfg.getInt(path);
	}
	
	
	public static Boolean getBoolean(String file, String path){
		FileConfiguration cfg = getFileConfiguration(file);
		return cfg.getBoolean(path);
	}
	
	public static Boolean getBoolean(String dir, String file, String path){
		FileConfiguration cfg = getFileConfiguration(dir, file);
		return cfg.getBoolean(path);
	}
	
	
	public static List<String> getList(String file, String path){
		return getList("", file, path);		
	}
	
	public static List<String> getList(String dir, String file, String path){
		FileConfiguration cfg = getFileConfiguration(dir, file);
		List<String> list = cfg.getStringList(path);
		if(list == null) return new ArrayList<String>();
		else return list;
	}
	
	public static Location getLocation(String file, String place){
		return getLocation("", file, place);
	}
	
	public static Location getLocation(String dir, String file, String place){
        FileConfiguration cfg = getFileConfiguration(dir, file);
       
        double x = cfg.getDouble(place + ".X");
        int y = cfg.getInt(place + ".Y");
        double z = cfg.getDouble(place + ".Z");
        World world = Bukkit.getWorld(cfg.getString(place + ".World"));
        
        if(cfg.getInt(place + ".Yaw") != 0 || cfg.getInt(place + ".Pitch") != 0) {
	        int yaw = cfg.getInt(place + ".Yaw");
	        int pitch = cfg.getInt(place + ".Pitch");
	        
	        return new Location(world, x, y, z, yaw, pitch);
        }
       
        return new Location(world, x, y, z);
    }
}
