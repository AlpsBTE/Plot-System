/*
 * The MIT License (MIT)
 *
 *  Copyright © 2025, Alps BTE <bte.atchli@gmail.com>
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

package com.alpsbte.plotsystem.core.system.tutorial;

import com.alpsbte.alpslib.hologram.DecentHologramDisplay;
import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialUtils;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import eu.decentsoftware.holograms.event.HologramClickEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class AbstractTutorialHologram extends DecentHologramDisplay {
    protected static final String READ_EMOJI = "✅";

    /**
     * This action is executed when the player clicks on the 'mark as read' text on the hologram.
     */
    @FunctionalInterface
    public interface ClickAction {
        void onClick(@NotNull HologramClickEvent clickEvent);
    }

    private final static int MAX_HOLOGRAM_LENGTH = 48; // The maximum length of a line in the hologram
    private final static String HOLOGRAM_LINE_BREAKER = "%newline%";
    private final static String EMPTY_TAG = "&f";

    protected final Player player;
    protected final int holoId;
    protected final String content;
    protected final int readMoreId;

    protected final Tutorial tutorial;

    private final Vector vectorPos;
    private ClickAction markAsReadClickAction;
    private boolean isMarkAsReadClicked = false;

    public AbstractTutorialHologram(Player player, int tutorialId, int holoId, String content, int readMoreId) {
        super("ps-tutorial-" + tutorialId + "-" + holoId, null, true);
        this.holoId = holoId;
        this.player = player;
        this.content = content;
        this.readMoreId = readMoreId;

        tutorial = AbstractTutorial.getActiveTutorial(player.getUniqueId());
        vectorPos = TutorialUtils.getTipPoints(tutorial.getConfig()).get(holoId);
    }

    /**
     * Gets the hologram title in the header
     *
     * @return formatted title
     */
    protected abstract String getTitle();

    /**
     * The text which is displayed on the hologram to read more.
     *
     * @return formatted read more text
     */
    protected abstract String getReadMoreActionText();

    /**
     * This method gets executed after the player has clicked on the 'read more' text on the hologram.
     */
    protected abstract void handleReadMoreClickAction();

    /**
     * The text which is displayed on the hologram to mark it as read.
     *
     * @return formatted text
     */
    protected abstract String getMarkAsReadActionText();

    /**
     * The text which is displayed after the player marked the hologram as read.
     *
     * @return formatted text
     */
    protected abstract String getMarkAsReadClickedActionText();

    @Override
    public void create(Player player) {
        setLocation(new Location(player.getWorld(), vectorPos.getX(), vectorPos.getY(), vectorPos.getZ()));
        super.create(player);
    }

    @Override
    public ItemStack getItem() {
        return null;
    }

    @Override
    public String getTitle(UUID playerUUID) {
        return getTitle();
    }

    @Override
    public boolean hasViewPermission(UUID uuid) {
        return player.getUniqueId().toString().equals(uuid.toString());
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
        List<DataLine<?>> lines = new ArrayList<>();
        lines.add(new TextLine(EMPTY_TAG));
        if (readMoreId != -1) lines.add(new TextLine(getReadMoreActionText()));
        if (markAsReadClickAction != null) lines.add(new TextLine(getMarkAsReadActionText()));
        return lines;
    }

    public void setMarkAsReadClickAction(ClickAction clickAction) {
        markAsReadClickAction = clickAction;
    }

    @Override
    public void reload(UUID playerUUID) {
        super.reload(playerUUID);

        // Add click event for tutorial hologram
        if (readMoreId == -1 && markAsReadClickAction == null) return;
        setClickListener(clickEvent -> {
            if (!isMarkAsReadClicked && markAsReadClickAction != null) {
                HologramLine line = clickEvent.getPage().getLines().getLast();
                line.setText(getMarkAsReadClickedActionText());
                clickEvent.getHologram().update(player);
                markAsReadClickAction.onClick(clickEvent);
                isMarkAsReadClicked = true;
            }
            if (readMoreId != -1) handleReadMoreClickAction();
        });
    }

    protected String getReadMoreLink() {
        return TutorialUtils.getDocumentationLinks(tutorial.getConfig()).get(readMoreId);
    }
}
