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

package com.alpsbte.plotsystem.core.system.tutorial.tasks.message;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.Tutorial;
import com.alpsbte.plotsystem.core.holograms.TutorialTipHologram;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.AbstractTask;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.sk89q.worldedit.Vector;
import me.filoghost.holographicdisplays.api.Position;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

import static com.alpsbte.plotsystem.utils.Utils.TutorialUtils.getDocumentationLinks;
import static com.alpsbte.plotsystem.utils.Utils.TutorialUtils.getTipPoints;
import static net.md_5.bungee.api.ChatColor.GRAY;

public class PlaceHologramTask extends AbstractTask {
    private final int tipId;
    private final int tutorialId;
    private final World tutorialWorld;
    private final TutorialTipHologram hologram;

    public PlaceHologramTask(Player player, int tipId, String content, int readMoreLinkId) {
        super(player);
        this.tipId = tipId;

        Tutorial tutorial = AbstractTutorial.getTutorialByPlayer(player.getUniqueId());
        this.tutorialId = tutorial.getId();
        this.tutorialWorld = tutorial.getCurrentWorld();

        if (readMoreLinkId != -1) {
            String readMoreLink = getDocumentationLinks(tutorialId).get(readMoreLinkId);
            hologram = new TutorialTipHologram(player, String.valueOf(tipId), content, () -> {
                player.spigot().sendMessage(new ChatMessageTask.ClickableTaskMessage(ChatMessageTask.TASK_PREFIX + readMoreLink,
                        GRAY + LangUtil.getInstance().get(player, LangPaths.Note.Action.READ_MORE) + "...", readMoreLink, ClickEvent.Action.OPEN_URL).getComponent());
                player.playSound(player.getLocation(), Sound.ENTITY_ITEMFRAME_ADD_ITEM, 1, 1.2f);
            });
        } else hologram = new TutorialTipHologram(player, String.valueOf(tipId), content);
    }

    @Override
    public void performTask() {
        Vector tipVector = getTipPoints(tutorialId).get(tipId);
        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () ->
                hologram.create(Position.of(tutorialWorld.getName(), tipVector.getX(), tipVector.getY(), tipVector.getZ())));

        setTaskDone();
    }

    public TutorialTipHologram getHologram() {
        return hologram;
    }
}
