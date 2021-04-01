package github.BTEPlotSystem.core.menus;

import github.BTEPlotSystem.utils.MenuItems;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.type.ChestMenu;

public abstract class AbstractMenu extends MenuItems {

    private final Menu menu;
    private final Player menuPlayer;

    public AbstractMenu(int rows, String title, Player menuPlayer) {
        this.menuPlayer = menuPlayer;
        this.menu = ChestMenu.builder(rows).title(title).redraw(true).build();
    }

    protected abstract void addMenuItems();

    protected abstract void setItemClickEvents();

    protected Menu getMenu() {
        return menu;
    }

    protected Player getMenuPlayer() {
        return menuPlayer;
    }
}
