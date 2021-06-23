package github.BTEPlotSystem.core.menus;

import github.BTEPlotSystem.core.system.Builder;
import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.utils.ItemBuilder;
import github.BTEPlotSystem.utils.LoreBuilder;
import github.BTEPlotSystem.utils.Utils;
import jdk.internal.jline.internal.Log;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class PlotMemberMenu extends AbstractMenu {

    private Plot plot;

    public PlotMemberMenu(Plot plot, Player menuPlayer) {
        super(3, "Manage Members | Plot # " + plot.getID(), menuPlayer);
        this.plot = plot;
        Mask mask = BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("111111111")
                .pattern("000000000")
                .pattern("111111111")
                .build();
        mask.apply(getMenu());

        try {
            addMenuItems();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "SQL Error, while trying to open PlotMemberMenu");
        }
        setItemClickEvents();
        getMenu().open(menuPlayer.getPlayer());
    }

    @Override
    protected void addMenuItems() throws SQLException {
        // Plot Owner Item
        getMenu().getSlot(10)
                .setItem(new ItemBuilder(Material.SKULL_ITEM, 1)
                        .setName("§6§lOWNER").setLore(new LoreBuilder()
                                .addLine("description").build())
                        .build());

        // Add Member Button
        ItemStack whitePlus = Utils.getItemHead("9237");
        getMenu().getSlot(16)
                .setItem(new ItemBuilder(whitePlus)
                        .setName("§6§lAdd Member to plot").setLore(new LoreBuilder()
                                .addLine("description").build())
                        .build());

        // Member List
        //TODO: Get Plot Members Method
        List<Builder> builders = plot.getPlotMembers();
        for (int i = 12; i < 15; i++) {
            if (builders.size() >= (i-11)) {
                Builder builder = builders.get(i-11);
                getMenu().getSlot(i)
                        .setItem(new ItemBuilder(Utils.getPlayerHead(builder.getUUID()))
                                .setName("§2" + builder.getName() + " - Member")
                                .build());
            } else {
                getMenu().getSlot(i)
                        .setItem(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 13).setName("§2Empty Member Slot").build());
            }
        }
    }

    @Override
    protected void setItemClickEvents() {
        // Add Member Button
        getMenu().getSlot(16).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            // TODO: Open Anvil UI
        });
    }
}