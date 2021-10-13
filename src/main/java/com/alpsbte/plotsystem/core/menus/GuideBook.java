package com.alpsbte.plotsystem.core.menus;

import com.alpsbte.plotsystem.utils.items.builder.ItemBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.upperlevel.spigot.book.BookUtil;

import java.util.ArrayList;
import java.util.List;

public class GuideBook {

    public GuideBook(Player player) {
        BookUtil.BookBuilder builder = new BookUtil.BookBuilder(BookUtil.writtenBook().build());
        builder.title("Guide");
        builder.pages(getPages());

        BookUtil.openPlayer(player,builder.build());
    }

    public List<BaseComponent[]> getPages() {
        List<BaseComponent[]> pages = new ArrayList<>();

        // Page One
        pages.add(new BookUtil.PageBuilder()
                .add(BookUtil.TextBuilder.of("Title")
                        .color(ChatColor.DARK_AQUA)
                        .build())
                .newLine()
                .add("Content")
                .build());

        // Page Two
        pages.add(new BookUtil.PageBuilder()
                .add(BookUtil.TextBuilder.of("Title 2")
                        .color(ChatColor.DARK_AQUA)
                        .build())
                .newLine()
                .add("Content")
                .build());
        return pages;
    }
}
