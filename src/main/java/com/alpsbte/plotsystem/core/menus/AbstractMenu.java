package com.alpsbte.plotsystem.core.menus;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.mask.Mask;
import org.ipvp.canvas.type.ChestMenu;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractMenu {
    private final Menu menu;
    private final Player menuPlayer;
    private final String title;
    private final MenuBackground background;

    protected AbstractMenu(int rows, String title, Player menuPlayer) {
        this(rows, MenuBackground.NONE, title, menuPlayer, true);
    }

    protected AbstractMenu(int rows, String title, Player menuPlayer, boolean reload) {
        this(rows, MenuBackground.NONE, title, menuPlayer, reload);
    }

    protected AbstractMenu(int rows, @NotNull MenuBackground background, String title, Player menuPlayer) {
        this(rows, background, title, menuPlayer, true);
    }

    protected AbstractMenu(int rows, @NotNull MenuBackground background, String title, Player menuPlayer, boolean reload) {
        this.title = title;
        this.background = background;
        this.menuPlayer = menuPlayer;
        this.menu = ChestMenu.builder(rows).title(displayTitle()).redraw(true).build();

        if (reload) reloadMenuAsync();
    }

    private Component displayTitle() {
        return background.apply(Component.text(title));
    }

    protected ItemStack borderItem() {
        return background.isActive() ? ItemStack.empty() : MenuItems.borderItem();
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
        // paper can only retitle an open window from a legacy string...
        getMenuPlayer().getOpenInventory().setTitle(LegacyComponentSerializer.legacySection().serialize(displayTitle()));
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
