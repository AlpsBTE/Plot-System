package com.alpsbte.plotsystem.core.holograms;

import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorialHologram;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.message.ChatMessageTask;
import com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialUtils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.kyori.adventure.text.Component.text;

public class PlotTutorialHologram extends AbstractTutorialHologram {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();
    public PlotTutorialHologram(Player player, int plotTutorialId, int holoId, String content) {
        this(player, plotTutorialId, holoId, content, -1);
    }

    public PlotTutorialHologram(Player player, int plotTutorialId, int holoId, String content, int readMoreId) {
        super(player, plotTutorialId, holoId, content, readMoreId);
    }

    @Override
    protected String getTitle() {
        return serialize(text(LangUtil.getInstance().get(player, LangPaths.Note.TIP).toUpperCase(), GOLD).decorate(BOLD));
    }

    @Override
    protected String getReadMoreActionText() {
        return serialize(text("[", DARK_GRAY).append(text(LangUtil.getInstance().get(player, LangPaths.Note.Action.READ_MORE), GRAY))
                .append(text("]", DARK_GRAY)));
    }

    @Override
    protected void handleReadMoreClickAction() {
        player.sendMessage(new ChatMessageTask.ClickableTaskMessage(TutorialUtils.CHAT_PREFIX_COMPONENT.append(Component.text(getReadMoreLink(), NamedTextColor.GRAY)),
                Component.text(LangUtil.getInstance().get(player, LangPaths.Note.Action.READ_MORE) + "...", NamedTextColor.GRAY), ClickEvent.openUrl(getReadMoreLink())).getComponent());
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_FRAME_ADD_ITEM, 1, 1.2f);
    }

    @Override
    protected String getMarkAsReadActionText() {
        return serialize(text("[", DARK_GRAY).append(text(LangUtil.getInstance().get(player, LangPaths.Note.Action.MARK_AS_READ) + " " + READ_EMOJI, YELLOW))
                .append(text("]", DARK_GRAY)));
    }

    @Override
    protected String getMarkAsReadClickedActionText() {
        return serialize(text(LangUtil.getInstance().get(player, LangPaths.Note.Action.READ) + " " + READ_EMOJI, GREEN));
    }

    private static String serialize(Component component) {
        return LEGACY_SERIALIZER.serialize(component);
    }
}
