package github.BTEPlotSystem.core.menus;

import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.utils.ItemBuilder;
import github.BTEPlotSystem.utils.LoreBuilder;
import github.BTEPlotSystem.core.system.Review;
import github.BTEPlotSystem.utils.Utils;
import github.BTEPlotSystem.utils.enums.Category;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.sql.SQLException;
import java.util.logging.Level;

public class FeedbackMenu extends AbstractMenu {

    private final Review review;
    private final Plot plot;

    public FeedbackMenu(Player player, int reviewID) throws SQLException {
        super(3, "Feedback | Review #" + reviewID, player);
        this.review = new Review(reviewID);
        this.plot = new Plot(review.getPlotID());

        Mask mask = BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("111111111")
                .pattern("000000000")
                .pattern("111111111")
                .build();
        mask.apply(getMenu());

        addMenuItems();

        getMenu().open(getMenuPlayer());
    }

    @Override
    protected void addMenuItems() {
        // Add Points Item
        try {
            getMenu().getSlot(10).setItem(new ItemBuilder(Material.NETHER_STAR)
                    .setName("§b§lScore")
                    .setLore(new LoreBuilder()
                            .addLines("Total Points: §f" + plot.getScore(),
                                    "",
                                    "§7Accuracy: " + Utils.getPointsByColor(review.getRating(Category.ACCURACY)) + "§8/§a5",
                                    "§7Block Palette: " + Utils.getPointsByColor(review.getRating(Category.BLOCKPALETTE)) + "§8/§a5",
                                    "§7Detailing: " + Utils.getPointsByColor(review.getRating(Category.DETAILING)) + "§8/§a5",
                                    "§7Technique: " + Utils.getPointsByColor(review.getRating(Category.TECHNIQUE)) + "§8/§a5",
                                    "",
                                    plot.isRejected() ? "§c§lRejected" : "§a§lAccepted")
                            .build())
                    .build());
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            getMenu().getSlot(10).setItem(errorItem());
        }

        // Add Feedback Item
        try {
            getMenu().getSlot(13).setItem(new ItemBuilder(Material.BOOK_AND_QUILL)
                    .setName("§b§lFeedback")
                    .setLore(new LoreBuilder()
                            .addLine(plot.getReview().getFeedback()).build())
                    .build());
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            getMenu().getSlot(13).setItem(errorItem());
        }

        // Add Reviewer Item
        try {
            getMenu().getSlot(16).setItem(new ItemBuilder(Material.SKULL_ITEM, 1, (byte) 3)
                    .setName("§b§lReviewer")
                    .setLore(new LoreBuilder()
                            .addLine(review.getReviewer().getName()).build())
                    .build());
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            getMenu().getSlot(13).setItem(errorItem());
        }
    }

    @Override
    protected void setItemClickEvents() {}
}
