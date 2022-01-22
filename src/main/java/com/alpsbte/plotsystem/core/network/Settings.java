package com.alpsbte.plotsystem.core.network;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Settings {

    public static Location SPAWN_BEGINNER = new Location( Bukkit.getWorld("world"), 9948.5, 107, 10000.5, 90, 0);
    public static Location SPAWN_ADVANCED = new Location( Bukkit.getWorld("world"), 9864.5, 133, 10000.5, 90, 0);
    public static Location SPAWN_PROFESSIONAL = new Location( Bukkit.getWorld("world"), 9781.5, 164, 10000.5, 90, 0);

    public static Location PORTAL_BEGINNER = new Location( Bukkit.getWorld("world"), 9930.5, 108, 10000.5, 90, 0);
    public static Location PORTAL_ADVANCED = new Location( Bukkit.getWorld("world"), 9848.5, 134, 10000.5, 90, 0);
    public static Location PORTAL_PROFESSIONAL = new Location( Bukkit.getWorld("world"), 9766.5,165, 10000.5, 90, 0);

    public static int SPAWN_MIN_HEIGHT = 50;

}
