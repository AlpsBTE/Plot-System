package com.alpsbte.plotsystem.core.holograms;

import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorialHologram;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.message.ChatMessageTask;
import com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialUtils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import static org.bukkit.ChatColor.BOLD;
import static org.bukkit.ChatColor.DARK_GRAY;
import static org.bukkit.ChatColor.GOLD;
import static org.bukkit.ChatColor.GRAY;
import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.YELLOW;

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
