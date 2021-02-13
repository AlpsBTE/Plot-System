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

import java.math.BigInteger;
import java.util.List;

public class Leaderboard {

    private final FileConfiguration config = BTEPlotSystem.getPlugin().getConfig();

    private Hologram hologram;
    private TextLine textLine[];
    private ItemLine itemLine;

    private int runnable;

    public Leaderboard(String title, Material item, List<String> data){
        createHologram();
        insertText(title,item,data);


        //Runs every 30 min
        /*runnable = BTEPlotSystem.getPlugin().getServer().getScheduler().scheduleSyncRepeatingTask(BTEPlotSystem.getPlugin(), new Runnable() {
            @Override
            public void run() {
                updateHologram();
            }
        }, 0,20*60*30);*/
    }

    private void insertText(String title, Material item, List<String> data){
        itemLine = hologram.insertItemLine(0,new ItemStack(item));

        textLine = new TextLine[data.size()+4];

        textLine[0] = hologram.insertTextLine(1,"§b§l"+title);
        textLine[1] = hologram.insertTextLine(2,"§7---------------");

        for (int i = 2; i<data.size() + 2;i++){
            textLine[i] = hologram.insertTextLine(i+1,"§e#"+(i-1)+" §a"+data.get(i-2).split(",")[0] + " §7- §b"+ data.get(i-2).split(",")[1]);
        }

        textLine[data.size()+3] = hologram.insertTextLine(data.size()+3,"§7---------------");
    }

    private void createHologram(){
        String world = config.getString("leaderboard.world");
        double x = Double.parseDouble(config.getString("leaderboard.x"));
        double y = Double.parseDouble(config.getString("leaderboard.y"));
        double z = Double.parseDouble(config.getString("leaderboard.z"));

        Location location = new Location(Bukkit.getWorld(world),x,y+4,z);
        hologram = HologramsAPI.createHologram(BTEPlotSystem.getPlugin(),location);
    }

    public void updateHologram(List<String> data){
        for (int i = 0; i < data.size(); i++) {
            hologram.removeLine(i+3);
            hologram.insertTextLine(i+3,"§e#"+(i+1)+" §a"+data.get(i).split(",")[0] + " §7- §b"+ data.get(i).split(",")[1]);
        }
    }
}
