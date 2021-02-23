package github.BTEPlotSystem.core.holograms;

import github.BTEPlotSystem.BTEPlotSystem;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class EventHologram extends HolographicDisplay {

    public EventHologram() {
        super("EventHologram");
    }

    @Override
    protected String getTitle() {
        return ("§6§lEVENT-SERVER");
    }

    @Override
    protected void insertLines() {
        getHologram().insertTextLine(0, getTitle());
        getHologram().insertTextLine(1, "§r");

        if(BTEPlotSystem.getPlugin().getNavigatorConfig().getBoolean("servers.event.visible")) {
            List<String> data = getDataLines();
            for(int i = 2; i < data.size() + 2; i++) {
                getHologram().insertTextLine(i, data.get(i - 2));
            }
        } else {
            getHologram().insertTextLine(2, "§2There is currently no event...");
        }
    }

    @Override
    public void updateLeaderboard() {
        if(isPlaced() && getHologram() != null) {
            getHologram().clearLines();
            insertLines();
        }
    }

    @Override
    protected List<String> getDataLines() {
        return Arrays.asList(BTEPlotSystem.getPlugin().getNavigatorConfig().getString("servers.event.type.description").split("/"));
    }

    @Override
    protected ItemStack getItem() {
        return null;
    }
}
