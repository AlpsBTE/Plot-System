package github.BTEPlotSystem.core.menus.wip;

import github.BTEPlotSystem.core.menus.AbstractMenu;
import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

public class ReviewPlotMenu extends AbstractMenu {

    public ReviewPlotMenu(Player player, Plot plot) {
        super(6, "Review Plot #" + plot.getID(), player);

        Mask mask = BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("111101111")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("110111011")
                .build();
        mask.apply(getMenu());
    }

    @Override
    protected void addMenuItems() {
        for(int i = 9; i <= 40; i++) {

        }
    }

    @Override
    protected void setItemClickEvents() {

    }
}
