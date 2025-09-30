package com.alpsbte.plotsystem.utils;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.List;

import static com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialUtils.TEXT_HIGHLIGHT_END;
import static com.alpsbte.plotsystem.core.system.tutorial.utils.TutorialUtils.TEXT_HIGHLIGHT_START;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class PlotMemberInvitation {
    public static final List<PlotMemberInvitation> invitationsList = new ArrayList<>();

    public final Player invitee;
    public final Plot plot;

    private final BukkitScheduler scheduler = PlotSystem.getPlugin().getServer().getScheduler();
    private int taskID;

    public PlotMemberInvitation(Player invitee, Plot plot) {
        this.invitee = invitee;
        this.plot = plot;

        // Check if player has already been invited
        for (PlotMemberInvitation item : invitationsList) {
            if (item.invitee == invitee) {
                plot.getPlotOwner().getPlayer().sendMessage(Utils.ChatUtils.getAlertFormat(AlpsUtils.deserialize(LangUtil.getInstance().get(invitee,
                        LangPaths.Message.Error.PLAYER_ALREADY_INVITED, TEXT_HIGHLIGHT_START + invitee.getName() + TEXT_HIGHLIGHT_END))));
                return;
            }
        }

        // Construct and send messages
        invitee.sendMessage(empty());
        invitee.sendMessage(Utils.ChatUtils.getInfoFormat(AlpsUtils.deserialize(LangUtil.getInstance().get(invitee,
                LangPaths.Message.Info.PLAYER_INVITE_TO_SENT, TEXT_HIGHLIGHT_START + plot.getPlotOwner().getName() + TEXT_HIGHLIGHT_END))));
        invitee.sendMessage(getInviteAcceptComponent(invitee).append(text(" ").append(getInviteRejectComponent(invitee))));
        invitee.sendMessage(empty());
        invitee.playSound(invitee.getLocation(), Utils.SoundUtils.NOTIFICATION_SOUND, 1f, 1f);

        plot.getPlotOwner().getPlayer().sendMessage(Utils.ChatUtils.getInfoFormat(AlpsUtils.deserialize(LangUtil.getInstance().get(plot.getPlotOwner().getPlayer(),
                LangPaths.Message.Info.PLAYER_INVITE_SENT, TEXT_HIGHLIGHT_START + invitee.getName() + TEXT_HIGHLIGHT_END))));

        // Add invitation to the static list
        invitationsList.add(this);

        // Schedule expiry in 30 seconds
        PlotMemberInvitation invitation = this;
        taskID = scheduler.scheduleSyncDelayedTask(PlotSystem.getPlugin(), () -> {
            invitationsList.remove(invitation);
            invitee.sendMessage(Utils.ChatUtils.getAlertFormat(AlpsUtils.deserialize(LangUtil.getInstance().get(invitee,
                    LangPaths.Message.Error.PLAYER_INVITE_EXPIRED, TEXT_HIGHLIGHT_START + plot.getPlotOwner().getName() + TEXT_HIGHLIGHT_END))));
            plot.getPlotOwner().getPlayer().sendMessage(Utils.ChatUtils.getAlertFormat(AlpsUtils.deserialize(LangUtil.getInstance().get(plot.getPlotOwner().getPlayer(),
                    LangPaths.Message.Error.PLAYER_INVITE_TO_EXPIRED, TEXT_HIGHLIGHT_START + invitee.getName() + TEXT_HIGHLIGHT_END))));
        }, 20 * 30);
    }

    public void acceptInvite() {
        Builder builder = Builder.byUUID(invitee.getUniqueId());
        if (builder.getFreeSlot() != null) {
            plot.addPlotMember(Builder.byUUID(invitee.getUniqueId()));

            // Messages Receiver
            invitee.sendMessage(Utils.ChatUtils.getInfoFormat(AlpsUtils.deserialize(LangUtil.getInstance().get(invitee,
                    LangPaths.Message.Info.PLAYER_INVITE_ACCEPTED, TEXT_HIGHLIGHT_START + plot.getPlotOwner().getName() + TEXT_HIGHLIGHT_END))));

            // Messages Sender
            plot.getPlotOwner().getPlayer().sendMessage(Utils.ChatUtils.getInfoFormat(AlpsUtils.deserialize(LangUtil.getInstance().get(plot.getPlotOwner().getPlayer(),
                    LangPaths.Message.Info.PLAYER_INVITE_TO_ACCEPTED, TEXT_HIGHLIGHT_START + invitee.getName() + TEXT_HIGHLIGHT_END))));
            plot.getPlotOwner().getPlayer().playSound(plot.getPlotOwner().getPlayer(), Utils.SoundUtils.NOTIFICATION_SOUND, 1f, 1f);
            scheduler.cancelTask(taskID);
        } else {
            invitee.sendMessage(Utils.ChatUtils.getAlertFormat(LangUtil.getInstance().get(invitee, LangPaths.Message.Error.ALL_SLOTS_OCCUPIED)));
        }
    }

    public void rejectInvite() {
        invitee.sendMessage(Utils.ChatUtils.getInfoFormat(AlpsUtils.deserialize(LangUtil.getInstance().get(invitee, LangPaths.Message.Info.PLAYER_INVITE_REJECTED,
                TEXT_HIGHLIGHT_START + plot.getPlotOwner().getName() + TEXT_HIGHLIGHT_END))));
        plot.getPlotOwner().getPlayer().sendMessage(Utils.ChatUtils.getAlertFormat(AlpsUtils.deserialize(LangUtil.getInstance().get(plot.getPlotOwner().getPlayer(),
                LangPaths.Message.Error.PLAYER_INVITE_TO_REJECTED, TEXT_HIGHLIGHT_START + invitee.getName() + TEXT_HIGHLIGHT_END))));
        plot.getPlotOwner().getPlayer().playSound(plot.getPlotOwner().getPlayer(), Utils.SoundUtils.NOTIFICATION_SOUND, 1f, 1f);
        scheduler.cancelTask(taskID);
    }

    private static Component getInviteAcceptComponent(Player player) {
        return text("[", DARK_GRAY, BOLD)
                .append(text(LangUtil.getInstance().get(player, LangPaths.Note.Action.ACCEPT), GREEN))
                .append(text("]", DARK_GRAY))
                .clickEvent(ClickEvent.runCommand("/plot invite accept"))
                .hoverEvent(HoverEvent.showText(text(LangUtil.getInstance().get(player,
                        LangPaths.Note.Action.CLICK_TO_PROCEED), GRAY)));
    }

    private static Component getInviteRejectComponent(Player player) {
        return text("[", DARK_GRAY, BOLD)
                .append(text(LangUtil.getInstance().get(player, LangPaths.Note.Action.REJECT), RED))
                .append(text("]", DARK_GRAY))
                .clickEvent(ClickEvent.runCommand("/plot invite reject"))
                .hoverEvent(HoverEvent.showText(text(LangUtil.getInstance().get(player,
                        LangPaths.Note.Action.CLICK_TO_PROCEED), GRAY)));
    }
}
