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

import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorialHologram;
import com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialUtils;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.message.ChatMessageTask;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import static org.bukkit.ChatColor.*;

public class PlotTutorialHologram extends AbstractTutorialHologram {
    public PlotTutorialHologram(Player player, int plotTutorialId, int holoId, String content) {
        this(player, plotTutorialId, holoId, content, -1);
    }

    public PlotTutorialHologram(Player player, int plotTutorialId, int holoId, String content, int readMoreId) {
        super(player, plotTutorialId, holoId, content, readMoreId);
    }

    @Override
    protected String getTitle() {
        return GOLD + BOLD.toString() + LangUtil.getInstance().get(player, LangPaths.Note.TIP).toUpperCase();
    }

    @Override
    protected String getReadMoreActionText() {
        return DARK_GRAY + "[" + GRAY + LangUtil.getInstance().get(player, LangPaths.Note.Action.READ_MORE) + DARK_GRAY + "]";
    }

    @Override
    protected void handleReadMoreClickAction() {
        player.sendMessage(new ChatMessageTask.ClickableTaskMessage(TutorialUtils.CHAT_PREFIX_COMPONENT.append(Component.text(getReadMoreLink(), NamedTextColor.GRAY)),
                Component.text(LangUtil.getInstance().get(player, LangPaths.Note.Action.READ_MORE) + "...", NamedTextColor.GRAY), ClickEvent.openUrl(getReadMoreLink())).getComponent());
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_FRAME_ADD_ITEM, 1, 1.2f);
    }

    @Override
    protected String getMarkAsReadActionText() {
        return DARK_GRAY + "[" + YELLOW + LangUtil.getInstance().get(player, LangPaths.Note.Action.MARK_AS_READ) + " " + READ_EMOJI + DARK_GRAY + "]";
    }

    @Override
    protected String getMarkAsReadClickedActionText() {
        return GREEN + LangUtil.getInstance().get(player, LangPaths.Note.Action.READ) + " " + READ_EMOJI;
    }
}
