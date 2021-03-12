package github.BTEPlotSystem.core.menus;

import github.BTEPlotSystem.core.plots.Plot;
import github.BTEPlotSystem.utils.ItemBuilder;
import github.BTEPlotSystem.utils.LoreBuilder;
import github.BTEPlotSystem.utils.Review;
import github.BTEPlotSystem.utils.Utils;
import github.BTEPlotSystem.utils.enums.Category;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;
import org.ipvp.canvas.type.ChestMenu;

import java.sql.SQLException;

public class FeedbackMenu {

    private final Menu menu;
    private final Review review;
    private final Plot plot;

    public FeedbackMenu(Player player, int reviewID) throws SQLException {
        this.review = new Review(reviewID);
        this.plot = new Plot(review.getPlotID());
        this.menu = ChestMenu.builder(3).title("Feedback | Plot #" + plot.getID()).build();

        Mask mask = BinaryMask.builder(menu)
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("111111111")
                .pattern("000000000")
                .pattern("111111111")
                .build();
        mask.apply(menu);

        setMenuItems();

        menu.open(player);
    }

    private void setMenuItems() throws SQLException {
        // Points
        menu.getSlot(10).setItem(new ItemBuilder(Material.NETHER_STAR)
            .setName("§b§lScore")
            .setLore(new LoreBuilder()
                .description(
                        "§7Total Points: §f" + plot.getScore(),
                        "",
                        "§7Accuracy: " + Utils.getPointsByColor(review.getRating(Category.ACCURACY)) + "§8/§a5",
                        "§7Block Palette: " + Utils.getPointsByColor(review.getRating(Category.BLOCKPALETTE)) + "§8/§a5",
                        "§7Detailing: " + Utils.getPointsByColor(review.getRating(Category.DETAILING)) + "§8/§a5",
                        "§7Technique: " + Utils.getPointsByColor(review.getRating(Category.TECHNIQUE)) + "§8/§a5",
                        "",
                        plot.wasRejected() ? "§c§lRejected" : "§a§lAccepted"
                )
               .build())
        .build());

        // Feedback
        menu.getSlot(13).setItem(new ItemBuilder(Material.BOOK_AND_QUILL)
            .setName("§b§lFeedback")
            .setLore(new LoreBuilder()
                .description("§f" + plot.getReview().getFeedback())
                .build())
        .build());

        // Reviewer
        menu.getSlot(16).setItem(new ItemBuilder(Material.SKULL_ITEM, 1, (byte) 3)
            .setName("§b§lReviewer")
            .setLore(new LoreBuilder()
                .description("§f" + review.getReviewer().getName())
            .build())
        .build());
    }


}
