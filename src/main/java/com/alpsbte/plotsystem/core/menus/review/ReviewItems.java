package com.alpsbte.plotsystem.core.menus.review;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.BaseItems;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class ReviewItems {
    public static ItemStack getReviewInfoItem(Player player) {
        String points = LangUtil.getInstance().get(player, LangPaths.MenuTitle.REVIEW_POINTS);

        return new ItemBuilder(BaseItems.REVIEW_INFO.getItem())
                .setName(text(LangUtil.getInstance().get(player, LangPaths.MenuTitle.INFORMATION), AQUA).decoration(BOLD, true))
                .setLore(new LoreBuilder()
                        .addLines(true, LangUtil.getInstance().get(player, LangPaths.MenuDescription.INFORMATION))
                        .emptyLine()
                        .addLines(text(points + " <= 0: ", WHITE).append(text(LangUtil.getInstance().get(player, LangPaths.Review.ABANDONED), RED)),
                                text(points + " <= 8: ", WHITE).append(text(LangUtil.getInstance().get(player, LangPaths.Review.REJECTED), YELLOW)),
                                text(points + " > 8: ", WHITE).append(text(LangUtil.getInstance().get(player, LangPaths.Review.ACCEPTED), GREEN)))
                        .build())
                .build();
    }

    public static ItemStack getPlotInfoItem(Player player, Plot plot) {
        String plotOwner;
        String city;
        String country;
        String difficulty;
        Player plotOwnerPlayer;

        plotOwner = plot.getPlotOwner().getName();
        city = plot.getCityProject().getName(player);
        country = plot.getCityProject().getCountry().getName(player);
        difficulty = plot.getDifficulty().name().charAt(0) + plot.getDifficulty().name().substring(1).toLowerCase();

        plotOwnerPlayer = plot.getPlotOwner().getPlayer();
        
        return new ItemBuilder(BaseItems.REVIEW_INFO_PLOT.getItem())
                .setName(text(LangUtil.getInstance().get(player, LangPaths.Review.REVIEW_PLOT))
                        .color(AQUA)
                        .decoration(BOLD, true))
                .setLore(new LoreBuilder()
                        .addLine(text(LangUtil.getInstance().get(player, LangPaths.Plot.ID) + ": ", GRAY).append(text(plot.getID(), WHITE)))
                        .emptyLine()
                        .addLines(text(LangUtil.getInstance().get(player, LangPaths.Plot.OWNER) + ": ", GRAY).append(text(plotOwner, WHITE)),
                                text(LangUtil.getInstance().get(player, LangPaths.Plot.CITY) + ": ", GRAY).append(text(city, WHITE)),
                                text(LangUtil.getInstance().get(player, LangPaths.Plot.COUNTRY) + ": ", GRAY).append(text(country, WHITE)),
                                text(LangUtil.getInstance().get(player, LangPaths.Plot.DIFFICULTY) + ": ", GRAY).append(text(difficulty, WHITE)))
                        .emptyLine()
                        .addLine(text(LangUtil.getInstance().get(player, LangPaths.Review.PLAYER_LANGUAGE) + ": ", GRAY).append(text(LangUtil.getInstance().get(plotOwnerPlayer, "lang.name"), WHITE)))
                        .build())
                .build();
    }
}
