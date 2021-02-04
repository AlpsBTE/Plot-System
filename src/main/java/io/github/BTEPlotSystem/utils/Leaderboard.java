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
import org.bukkit.inventory.ItemStack;

public class Leaderboard {

    private Hologram hologram;
    private TextLine textLine[] = new TextLine[14];
    private ItemLine itemLine;

    public Leaderboard(){
        hologram = HologramsAPI.createHologram(BTEPlotSystem.getPlugin(),new Location(Bukkit.getWorlds().get(0),682.5,102,1209.5));
        itemLine = hologram.appendItemLine(new ItemStack(Material.NETHER_STAR));

        insertText();

    }

    private void insertText(){
        textLine[0] = hologram.insertTextLine(0,"Score Leaderboard");
        textLine[1] = hologram.insertTextLine(0,"---------------");

        for (int i = 0; i>10;i++){
            textLine[i+2] = hologram.insertTextLine(i+2,"#"+(i+1)+" Name");
        }

        textLine[13] = hologram.insertTextLine(0,"---------------");
    }

}
