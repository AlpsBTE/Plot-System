package com.alpsbte.plotsystem.core.menus;

import com.alpsbte.plotsystem.PlotSystem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.mask.Mask;
import org.ipvp.canvas.type.ChestMenu;

import static net.kyori.adventure.text.Component.text;

public abstract class AbstractMenu {
    private final Menu menu;
    private final Player menuPlayer;
    private final String title;

    protected AbstractMenu(int rows, String title, Player menuPlayer) {
        this(rows, title, menuPlayer, true);
    }

    protected AbstractMenu(int rows, String title, Player menuPlayer, boolean reload) {
        this.title = title;
        this.menuPlayer = menuPlayer;
        this.menu = ChestMenu.builder(rows).title(text(title)).redraw(true).build();

        if (reload) reloadMenuAsync();
    }

    /**
     * Places items asynchronously in the menu after it is opened
     */
    protected void setMenuItemsAsync() {
        // Default just does nothing, for use-cases which doesn't need it.
    }

    /**
     * Sets click events for the items placed in the menu async after it is opened
     */
    protected void setItemClickEventsAsync() {
        // Default just does nothing, for use-cases which doesn't need it.
    }

    /**
     * Places pre-defined items in the menu before it is opened
     *
     * @return Pre-defined mask
     * @see <a href=https://github.com/IPVP-MC/canvas#masks</a>
     */
    protected abstract Mask getMask();

    /**
     * Places items synchronously in the menu and opens it afterward
     * NOTE: This method gets called before class is loaded!
     */
    protected void setPreviewItems() {
        if (getMask() != null) getMask().apply(getMenu());
        getMenu().open(getMenuPlayer());
        getMenuPlayer().getOpenInventory().setTitle(title);
    }

    /**
     * Reloads all menu items and click events in the menu asynchronously
     * {@link #setPreviewItems()}.{@link #setMenuItemsAsync()}.{@link #setItemClickEventsAsync()}
     */
    protected void reloadMenuAsync() {
        setPreviewItems();
        Bukkit.getScheduler().runTaskAsynchronously(PlotSystem.getPlugin(), () -> {
            setMenuItemsAsync();
            setItemClickEventsAsync();
        });
    }

    /**
     * @return Inventory
     */
    protected Menu getMenu() {
        return menu;
    }

    /**
     * @return Inventory player
     */
    protected Player getMenuPlayer() {
        return menuPlayer;
    }
}
