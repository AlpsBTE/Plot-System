package github.BTEPlotSystem.utils;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.ItemLine;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import github.BTEPlotSystem.BTEPlotSystem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public class Leaderboard {

    private final FileConfiguration config = BTEPlotSystem.getPlugin().getConfig();

    private Hologram hologram;
    private TextLine textLine[] = new TextLine[14];
    private ItemLine itemLine;

    public Leaderboard(){
        createHologram();
        insertText();

    }

    private void insertText(){
        itemLine = hologram.insertItemLine(0,new ItemStack(Material.NETHER_STAR));

        textLine[0] = hologram.insertTextLine(1,"§b§lSCORE LEADERBOARD");
        textLine[1] = hologram.insertTextLine(2,"§7---------------");

        for (int i = 2; i<=11;i++){
            textLine[i] = hologram.insertTextLine(i+1,"§e#"+(i-1)+" §aName §7- §b69420");
        }

        textLine[13] = hologram.insertTextLine(13,"§7---------------");
    }

    private void createHologram(){
        String world = config.getString("leaderboard.world");
        double x = Double.parseDouble(config.getString("leaderboard.x"));
        double y = Double.parseDouble(config.getString("leaderboard.y"));
        double z = Double.parseDouble(config.getString("leaderboard.z"));

        Location location = new Location(Bukkit.getWorld(world),x,y+4,z);
        hologram = HologramsAPI.createHologram(BTEPlotSystem.getPlugin(),location);
    }

}
