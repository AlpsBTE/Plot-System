/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

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

    public AbstractMenu(int rows, String title, Player menuPlayer) {
        this(rows, title, menuPlayer, true);
    }

    public AbstractMenu(int rows, String title, Player menuPlayer, boolean reload) {
        this.title = title;
        this.menuPlayer = menuPlayer;
        this.menu = ChestMenu.builder(rows).title(text(title)).redraw(true).build();

        if (reload) reloadMenuAsync();
    }

    /**
     * Places items asynchronously in the menu after it is opened
     */
    protected abstract void setMenuItemsAsync();

    /**
     * Sets click events for the items placed in the menu async after it is opened
     */
    protected abstract void setItemClickEventsAsync();

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
