/*
 * The MIT License (MIT)
 *
 *  Copyright © 2021-2022, Alps BTE <bte.atchli@gmail.com>
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

import java.util.List;

public abstract class AbstractPaginatedMenu extends AbstractMenu {
    private List<?> source;
    private final int maxItemsPerPage;
    private int totalItemsAmount;
    private int currentPage = 0;

    public AbstractPaginatedMenu(int rows, int pagedRows, String title, Player menuPlayer) {
        super(rows, title, menuPlayer, false);
        this.maxItemsPerPage = pagedRows * 9;
        reloadMenuAsync();
    }

    /**
     * Collects the source for the inventory items
     *
     * @return item sources
     */
    protected abstract List<?> getSource();

    /**
     * Places paginated items asynchronously in the menu after it is opened
     *
     * @param source paginated item sources
     */
    protected abstract void setPaginatedMenuItemsAsync(List<?> source);

    /**
     * Sets click events for the paginated items placed in the menu after it is opened
     *
     * @param source paginated item sources
     */
    protected abstract void setPaginatedItemClickEventsAsync(List<?> source);

    /**
     * Switch to the next page
     */
    protected void nextPage() {
        if (hasNextPage()) setPage(currentPage + 1);
    }

    /**
     * Switch to the previous page
     */
    protected void previousPage() {
        if (hasPreviousPage()) setPage(currentPage - 1);
    }

    /**
     * Sets the current page to the given index
     *
     * @param index page index
     */
    protected void setPage(int index) {
        currentPage = index;
        reloadMenuAsync(false);
    }

    /**
     * Collects all item sources for the current page
     *
     * @return item sources for the current page
     */
    private List<?> getItemSources(boolean reloadSources) {
        if (reloadSources) source = getSource();
        this.totalItemsAmount = source.size();
        return source.subList(getMinIndex(), Math.min(getMaxIndex(), source.size()));
    }

    /**
     * @return true if there is a next page
     */
    protected boolean hasNextPage() {
        return getMaxIndex() < totalItemsAmount;
    }

    /**
     * @return true if there is a previous page
     */
    protected boolean hasPreviousPage() {
        return getMinIndex() > 0;
    }

    /**
     * @return min slot index for current page
     */
    private int getMinIndex() {
        return currentPage * maxItemsPerPage;
    }

    /**
     * @return max slot index for current page
     */
    private int getMaxIndex() {
        return (currentPage + 1) * maxItemsPerPage;
    }

    /**
     * @param reloadSources if true, reload the source collection for the inventory items
     */
    protected void reloadMenuAsync(boolean reloadSources) {
        getMenu().clear();
        super.reloadMenuAsync();

        Bukkit.getScheduler().runTaskAsynchronously(PlotSystem.getPlugin(), () -> {
            List<?> sources = getItemSources(reloadSources);
            setPaginatedMenuItemsAsync(sources);
            setPaginatedItemClickEventsAsync(sources);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reloadMenuAsync() {
        reloadMenuAsync(true);
    }
}
