package github.BTEPlotSystem.core.holograms;

import github.BTEPlotSystem.BTEPlotSystem;
import github.BTEPlotSystem.core.system.Builder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ScoreLeaderboard extends HolographicDisplay {

    public ScoreLeaderboard() {
        super("ScoreLeaderboard");
    }

    @Override
    protected String getTitle() {
        return "§b§lSCORE LEADERBOARD";
    }

    @Override
    protected List<String> getDataLines() {
        try {
            return Builder.getBuildersByScore(10);
        } catch (SQLException ex) {
            BTEPlotSystem.getPlugin().getLogger().log(Level.SEVERE, "Could not read data lines.", ex);
        }
        return new ArrayList<>();
    }

    @Override
    protected ItemStack getItem() {
        return new ItemStack(Material.NETHER_STAR);
    }

    @Override
    public void updateLeaderboard() {
        if(isPlaced()) {
            getHologram().clearLines();
            insertLines();
        }
    }
}
