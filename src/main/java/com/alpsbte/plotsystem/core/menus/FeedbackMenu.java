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

import com.alpsbte.alpslib.utils.head.AlpsHeadUtils;
import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.database.DatabaseConnection;
import com.alpsbte.plotsystem.core.system.Review;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Category;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.sql.ResultSet;
import java.sql.SQLException;

import static net.kyori.adventure.text.Component.text;

public class FeedbackMenu extends AbstractMenu {

    private Review review = null;
    private final Plot plot;

    public FeedbackMenu(Player player, int plotID) throws SQLException {
        super(3, LangUtil.getInstance().get(player, LangPaths.MenuTitle.FEEDBACK, String.valueOf(plotID)), player);
        this.plot = new Plot(plotID);
    }

    @Override
    protected void setPreviewItems() {
        getMenu().getSlot(16).setItem(MenuItems.loadingItem(Material.PLAYER_HEAD, getMenuPlayer()));
        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {
        // Get review id from plot
        try (ResultSet rs = DatabaseConnection.createStatement("SELECT review_id FROM plotsystem_plots WHERE id = ?")
                .setValue(plot.getID()).executeQuery()) {

            if (rs.next()) this.review = new Review(rs.getInt(1));
            DatabaseConnection.closeResultSet(rs);
        } catch (SQLException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
        }

        // Set score item
        try {
            getMenu().getSlot(10).setItem(new ItemBuilder(Material.NETHER_STAR)
                    .setName(Component.text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.SCORE), NamedTextColor.AQUA).decoration(TextDecoration.BOLD, true))
                    .setLore(new LoreBuilder() //TODO: use components
                            .addLines(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Plot.TOTAL_SCORE) + ": §f" + plot.getTotalScore(),
                                    "",
                                    "§7" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.Criteria.ACCURACY) + ": " + Utils.ItemUtils.getColorByPoints(review.getRating(Category.ACCURACY)) + "§8/§a5",
                                    "§7" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.Criteria.BLOCK_PALETTE) + ": " + Utils.ItemUtils.getColorByPoints(review.getRating(Category.BLOCKPALETTE)) + "§8/§a5",
                                    "§7" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.Criteria.DETAILING) + ": " + Utils.ItemUtils.getColorByPoints(review.getRating(Category.DETAILING)) + "§8/§a5",
                                    "§7" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.Criteria.TECHNIQUE) + ": " + Utils.ItemUtils.getColorByPoints(review.getRating(Category.TECHNIQUE)) + "§8/§a5",
                                    "",
                                    plot.isRejected() ? "§c§l" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.REJECTED) : "§a§l" + LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.ACCEPTED))
                            .build())
                    .build());
        } catch (SQLException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
            getMenu().getSlot(10).setItem(MenuItems.errorItem(getMenuPlayer()));
        }

        // Set feedback text item
        try {
            getMenu().getSlot(13).setItem(new ItemBuilder(Material.WRITABLE_BOOK)
                    .setName(Component.text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.FEEDBACK), NamedTextColor.AQUA).decoration(TextDecoration.BOLD, true))
                    .setLore(new LoreBuilder() //TODO: use components
                            .addLines(plot.getReview().getFeedback())
                            .build())
                    .build());
        } catch (SQLException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
            getMenu().getSlot(13).setItem(MenuItems.errorItem(getMenuPlayer()));
        }

        // Set reviewer item
        try {
            getMenu().getSlot(16).setItem(new ItemBuilder(AlpsHeadUtils.getPlayerHead(review.getReviewer().getUUID()))
                    .setName(Component.text(LangUtil.getInstance().get(getMenuPlayer(), LangPaths.Review.REVIEWER), NamedTextColor.AQUA).decoration(TextDecoration.BOLD, true))
                    .setLore(new LoreBuilder()
                            .addLine(Component.text(review.getReviewer().getName())).build())
                    .build());
        } catch (SQLException ex) {
            PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
            getMenu().getSlot(13).setItem(MenuItems.errorItem(getMenuPlayer()));
        }
    }

    @Override
    protected void setItemClickEventsAsync() {}

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, 1).setName(Component.empty()).build())
                .pattern("111111111")
                .pattern("000000000")
                .pattern("111111111")
                .build();
    }
}
