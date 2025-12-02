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

    protected AbstractPaginatedMenu(int rows, int pagedRows, String title, Player menuPlayer) {
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
