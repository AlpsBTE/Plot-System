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

package com.alpsbte.plotsystem.core.holograms;

import com.alpsbte.alpslib.hologram.HolographicDisplay;
import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.message.PlaceHologramTask;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import me.filoghost.holographicdisplays.api.hologram.line.TextHologramLine;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static net.md_5.bungee.api.ChatColor.*;

/**
 * This hologram is used to display tutorial tips to the player.
 * @see PlaceHologramTask
 */
public class TutorialTipHologram extends HolographicDisplay {

    /**
     * This action is executed when the player clicks on the 'read more' text on the hologram.
     */
    @FunctionalInterface
    public interface ClickAction {
        void onClick();
    }

    private final static int MAX_HOLOGRAM_LENGTH = 48; // The maximum length of a line in the hologram
    private final static String HOLOGRAM_LINE_BREAKER = "%newline%";

    private final String content;
    private final ClickAction clickAction;
    private final Player player;

    public TutorialTipHologram(Player player, String id, String content) {
        this(player, id, content, null);
    }

    public TutorialTipHologram(Player player, String id, String content, ClickAction clickAction) {
        super(id);
        this.content = content;
        this.player = player;
        this.clickAction = clickAction;
    }

    @Override
    public ItemStack getItem() {
        return null;
    }

    @Override
    public String getTitle() {
        return GOLD + BOLD.toString() + LangUtil.getInstance().get(player, LangPaths.Note.TIP).toUpperCase();
    }

    @Override
    public List<DataLine<?>> getHeader() {
        return Collections.singletonList(new TextLine(this.getTitle()));
    }

    @Override
    public List<DataLine<?>> getContent() {
        List<DataLine<?>> lines = new ArrayList<>();
        List<String> innerLines = AlpsUtils.createMultilineFromString(content, MAX_HOLOGRAM_LENGTH, HOLOGRAM_LINE_BREAKER);
        innerLines.forEach(innerLine -> lines.add(new TextLine(innerLine)));
        return lines;
    }

    @Override
    public List<DataLine<?>> getFooter() {
        return clickAction == null ? new ArrayList<>() : Arrays.asList(
                new TextLine(EMPTY_TAG),
                new TextLine(DARK_GRAY + "[" + GRAY + LangUtil.getInstance().get(player, LangPaths.Note.Action.READ_MORE) + DARK_GRAY + "]")
        );
    }

    @Override
    public void reload() {
        super.reload();

        // Set click listener
        if (getHologram() == null || clickAction == null) return;
        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
            TextHologramLine line = (TextHologramLine) getHologram().getLines().get(getHologram().getLines().size() - 1);
            line.setClickListener((clickEvent) -> clickAction.onClick());
        });
    }
}
