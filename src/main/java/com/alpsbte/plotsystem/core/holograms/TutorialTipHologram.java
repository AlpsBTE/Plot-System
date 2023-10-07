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
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.message.PlaceHologramTask;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import me.filoghost.holographicdisplays.api.Position;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.line.TextHologramLine;
import org.bukkit.Bukkit;
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

    public TutorialTipHologram(String id, Position position, String content) {
        this(id, position, content, null);
    }

    public TutorialTipHologram(String id, Position position, String content, ClickAction clickAction) {
        super(id, position, true);
        this.content = content;
        this.clickAction = clickAction;
    }

    @Override
    public ItemStack getItem() {
        return null;
    }

    @Override
    public String getTitle(UUID playerUUID) {
        return GOLD + BOLD.toString() + LangUtil.getInstance().get(Bukkit.getPlayer(playerUUID), LangPaths.Note.TIP).toUpperCase();
    }

    @Override
    public boolean hasViewPermission(UUID uuid) {
        return true;
    }

    @Override
    public List<DataLine<?>> getHeader(UUID playerUUID) {
        return Collections.singletonList(new TextLine(this.getTitle(playerUUID)));
    }

    @Override
    public List<DataLine<?>> getContent(UUID playerUUID) {
        List<DataLine<?>> lines = new ArrayList<>();
        List<String> innerLines = AlpsUtils.createMultilineFromString(content, MAX_HOLOGRAM_LENGTH, HOLOGRAM_LINE_BREAKER);
        innerLines.forEach(innerLine -> lines.add(new TextLine(innerLine)));
        return lines;
    }

    @Override
    public List<DataLine<?>> getFooter(UUID playerUUID) {
        return clickAction == null ? new ArrayList<>() : Arrays.asList(
                new TextLine(EMPTY_TAG),
                new TextLine(DARK_GRAY + "[" + GRAY + LangUtil.getInstance().get(Bukkit.getPlayer(playerUUID), LangPaths.Note.Action.READ_MORE) + DARK_GRAY + "]")
        );
    }

    @Override
    public void reload(UUID playerUUID) {
        super.reload(playerUUID);

        // Set click listener
        Hologram holo = getHologram(playerUUID);
        if (holo == null || clickAction == null) return;
        TextHologramLine line = (TextHologramLine) holo.getLines().get(holo.getLines().size() - 1);
        line.setClickListener((clickEvent) -> clickAction.onClick());
    }
}
